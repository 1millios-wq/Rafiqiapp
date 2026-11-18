package com.rafiqi.app

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RafiqiAccessibilityService : AccessibilityService() {

    private var lastCheckedTitle = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val prefs = getSharedPreferences("RafiqiPrefs", Context.MODE_PRIVATE)
        val isRestDay = prefs.getBoolean("is_rest_day", false)
        if (isRestDay) return

        val packageName = event?.packageName?.toString() ?: return

        // 1. فحص التطبيقات المحظورة نهائياً أو مؤقتاً
        val blockedApps = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()
        if (blockedApps.contains(packageName)) {
            goHome()
            return
        }

        // 2. مراقبة يوتيوب والسماح بالمحتوى المفيد فقط
        if (packageName == "com.google.android.youtube") {
            val rootNode = rootInActiveWindow ?: return
            val videoTitle = findYouTubeTitle(rootNode)
            if (videoTitle.isNotEmpty() && videoTitle != lastCheckedTitle) {
                lastCheckedTitle = videoTitle
                if (!isEducationalOrUseful(videoTitle)) {
                    goHome()
                }
            }
        }
    }

    private fun findYouTubeTitle(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val viewId = node.viewIdResourceName ?: ""
        if (viewId.contains("title") && !node.text.isNullOrBlank()) {
            return node.text.toString()
        }
        for (i in 0 until node.childCount) {
            val res = findYouTubeTitle(node.getChild(i))
            if (res.isNotEmpty()) return res
        }
        return ""
    }

    private fun isEducationalOrUseful(title: String): Boolean {
        val keywords = listOf(
            "قرآن", "تفسير", "حديث", "فقه", "سيرة", "دين", "اسلام", "صلاة", "ذكر",
            "درس", "شرح", "محاضرة", "فيزياء", "كيمياء", "رياضيات", "برمجة", "تعلم", "لغة", "انجليزي", "دراسة",
            "رياضة", "تمرين", "عضلات", "صحة", "تغذية", "تطوير", "كتاب", "ملخص", "عادات",
            "quran", "lecture", "study", "tutorial", "course", "workout", "fitness", "math", "physics", "science"
        )
        return keywords.any { title.contains(it, ignoreCase = true) }
    }

    private fun goHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
