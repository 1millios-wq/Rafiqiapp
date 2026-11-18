package com.rafiqi.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                RafiqiMainScreen()
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RafiqiMainScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("RafiqiPrefs", Context.MODE_PRIVATE) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var gold by remember { mutableIntStateOf(prefs.getInt("gold", 50)) }
    var food by remember { mutableIntStateOf(prefs.getInt("food", 20)) }
    var houses by remember { mutableIntStateOf(prefs.getInt("houses", 1)) }
    var libraries by remember { mutableIntStateOf(prefs.getInt("libraries", 0)) }
    var mosques by remember { mutableIntStateOf(prefs.getInt("mosques", 0)) }

    var showSettingsAuth by remember { mutableStateOf(false) }
    var showSettingsPanel by remember { mutableStateOf(false) }
    var showAppDrawer by remember { mutableStateOf(false) }

    fun addReward(g: Int, f: Int) {
        gold += g
        food += f
        prefs.edit().putInt("gold", gold).putInt("food", food).apply()
        Toast.makeText(context, "أحسنت! ربحت +$g ذهب و +$f طعام لمدينتك", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("رفيقي - نظام التطوير الذكي", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    IconButton(onClick = { showAppDrawer = true }) {
                        Icon(Icons.Default.Apps, contentDescription = "التطبيقات")
                    }
                    IconButton(onClick = { showSettingsAuth = true }) {
                        Icon(Icons.Default.Lock, contentDescription = "الإعدادات")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val tabs = listOf("الدين", "الجسم", "الدراسة", "الشخصية", "العادات")
                val icons = listOf(Icons.Default.Star, Icons.Default.Favorite, Icons.Default.Book, Icons.Default.Person, Icons.Default.Games)
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🏙️ مدينتي: المستوى ${houses + libraries + mosques}", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("💰 $gold ذهب | 🍞 $food طعام", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("المباني: 🏠 منازل ($houses) | 🕌 مساجد ($mosques) | 📚 مكتبات ($libraries)", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            if (gold >= 30 && food >= 10) {
                                gold -= 30; food -= 10; houses++
                                prefs.edit().putInt("gold", gold).putInt("food", food).putInt("houses", houses).apply()
                            } else Toast.makeText(context, "تحتاج 30 ذهب و10 طعام", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.weight(1f)) { Text("بناء منزل (30💰)", fontSize = 11.sp) }

                        Button(onClick = {
                            if (gold >= 50) {
                                gold -= 50; mosques++
                                prefs.edit().putInt("gold", gold).putInt("mosques", mosques).apply()
                            } else Toast.makeText(context, "تحتاج 50 ذهب", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.weight(1f)) { Text("بناء مسجد (50💰)", fontSize = 11.sp) }

                        Button(onClick = {
                            if (gold >= 40) {
                                gold -= 40; libraries++
                                prefs.edit().putInt("gold", gold).putInt("libraries", libraries).apply()
                            } else Toast.makeText(context, "تحتاج 40 ذهب", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.weight(1f)) { Text("بناء مكتبة (40💰)", fontSize = 11.sp) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (selectedTab) {
                    0 -> {
                        item { SectionHeader("🕌 القسم الديني والقرآن والسنن") }
                        item { TaskCard("قراءة ورد القرآن اليومي (صفحتين)", "مكافأة: +20 ذهب و +10 طعام") { addReward(20, 10) } }
                        item { TaskCard("أذكار الصباح والمساء والسنن الرواتب", "مكافأة: +15 ذهب و +10 طعام") { addReward(15, 10) } }
                        item { TaskCard("قراءة حديث من الأربعين النووية", "مكافأة: +15 ذهب") { addReward(15, 0) } }
                        item { AiAssistantCard(prefs, "اسأل رفيقي عن تفسير آية أو شرح حديث أو مرجع ديني:") }
                    }
                    1 -> {
                        item { SectionHeader("💪 قسم الصحة والرياضة والغذاء") }
                        item { TaskCard("تمرين رياضي لمدة 20 دقيقة", "مكافأة: +25 ذهب و +15 طعام") { addReward(25, 15) } }
                        item { TaskCard("شرب 2 لتر ماء وتناول وجبة صحية", "مكافأة: +10 ذهب و +20 طعام") { addReward(10, 20) } }
                        item { AiAssistantCard(prefs, "اطلب من رفيقي خطة تمرين منزلية أو نصيحة غذائية:") }
                    }
                    2 -> {
                        item { SectionHeader("📚 قسم الدراسة والكتب المرجعية") }
                        item { TaskCard("جلسة تركيز دراسي (بومودورو 25 دقيقة)", "مكافأة: +30 ذهب") { addReward(30, 0) } }
                        item { TaskCard("مراجعة ملخصات الدروس السابقة", "مكافأة: +20 ذهب") { addReward(20, 0) } }
                        item { AiAssistantCard(prefs, "الصق فقرة من كتابك المرجعي أو اسأل عن درس لم تفهمه:") }
                    }
                    3 -> {
                        item { SectionHeader("🧠 بناء الشخصية والمخطط اليومي الذكي") }
                        item { DailyPlannerCard(prefs) { addReward(15, 10) } }
                        item { AiAssistantCard(prefs, "اكتب فكرة أو كتاباً تريد تلخيصه لتطوير شخصيتك:") }
                    }
                    4 -> {
                        item { SectionHeader("🎮 العادات والألعاب الذهنية المفيدة") }
                        item { MathGameCard { addReward(15, 5) } }
                        item { TaskCard("الالتزام بعادة إيجابية وترك الهاتف قبل النوم", "مكافأة: +20 ذهب") { addReward(20, 0) } }
                    }
                }
            }
        }
    }
    if (showSettingsAuth) {
        var passInput by remember { mutableStateOf("") }
        val savedPass = prefs.getString("admin_pass", "1234") ?: "1234"
        AlertDialog(
            onDismissRequest = { showSettingsAuth = false },
            title = { Text("أدخل كلمة سر الإعدادات (الافتراضية 1234)") },
            text = {
                OutlinedTextField(
                    value = passInput,
                    onValueChange = { passInput = it },
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("كلمة السر") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (passInput == savedPass) {
                        showSettingsAuth = false
                        showSettingsPanel = true
                    } else Toast.makeText(context, "كلمة السر خاطئة!", Toast.LENGTH_SHORT).show()
                }) { Text("دخول") }
            }
        )
    }

    if (showSettingsPanel) {
        SettingsDialog(prefs = prefs, onDismiss = { showSettingsPanel = false })
    }

    if (showAppDrawer) {
        AppDrawerDialog(prefs = prefs, onDismiss = { showAppDrawer = false })
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
}

@Composable
fun TaskCard(title: String, rewardText: String, onComplete: () -> Unit) {
    var done by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(rewardText, fontSize = 12.sp, color = Color(0xFF4ADE80))
            }
            Button(
                onClick = { if (!done) { done = true; onComplete() } },
                enabled = !done
            ) { Text(if (done) "تم ✓" else "إنجاز") }
        }
    }
}

@Composable
fun AiAssistantCard(prefs: android.content.SharedPreferences, hint: String) {
    var prompt by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("🤖 المساعد الذكي (Gemini AI)", fontWeight = FontWeight.Bold, color = Color(0xFFA855F7))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(hint, fontSize = 12.sp) }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = {
                    val apiKey = prefs.getString("gemini_key", "") ?: ""
                    if (apiKey.isBlank()) {
                        response = "⚠️ يرجى وضع مفتاح Gemini API في الإعدادات أولاً."
                        return@Button
                    }
                    loading = true
                    scope.launch {
                        try {
                            val model = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)
                            val res = model.generateContent("أجب باللغة العربية باختصار وفائدة كمدرب شخصي ذكي: $prompt")
                            response = res.text ?: "لم يتم استلام رد."
                        } catch (e: Exception) {
                            response = "خطأ في الاتصال (تأكد من تشغيل VPN وصحة المفتاح): ${e.localizedMessage}"
                        } finally { loading = false }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "جاري التفكير..." else "اسأل الذكاء الاصطناعي") }
            if (response.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(response, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}
@Composable
fun DailyPlannerCard(prefs: android.content.SharedPreferences, onGenerated: () -> Unit) {
    var mood by remember { mutableStateOf("") }
    var doneToday by remember { mutableStateOf("") }
    var tomorrowTasks by remember { mutableStateOf("") }
    var planResult by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("📅 صانع الجدول اليومي الذكي", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
            OutlinedTextField(value = mood, onValueChange = { mood = it }, label = { Text("كيف مزاجك وطاقتك اليوم؟") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = doneToday, onValueChange = { doneToday = it }, label = { Text("ماذا أخذت أو أنجزت اليوم؟") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tomorrowTasks, onValueChange = { tomorrowTasks = it }, label = { Text("ماذا لديك غداً من دروس ومهام؟") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val apiKey = prefs.getString("gemini_key", "") ?: ""
                    if (apiKey.isBlank()) {
                        planResult = "⚠️ ضع مفتاح Gemini API في الإعدادات أولاً."
                        return@Button
                    }
                    loading = true
                    scope.launch {
                        try {
                            val model = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)
                            val fullPrompt = "بناءً على حالتي المزاجية ($mood)، وما أنجزته اليوم ($doneToday)، والتزاماتي غداً ($tomorrowTasks)، اصنع لي جدولاً عملياً ومتوازناً للغد يشمل الدين والدراسة والرياضة والراحة."
                            val res = model.generateContent(fullPrompt)
                            planResult = res.text ?: ""
                            onGenerated()
                        } catch (e: Exception) {
                            planResult = "تأكد من تشغيل VPN وصحة المفتاح."
                        } finally { loading = false }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "جاري بناء جدولك..." else "اصنع جدولي للغد (+15💰)") }
            if (planResult.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(planResult, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun MathGameCard(onWin: () -> Unit) {
    var a by remember { mutableIntStateOf((12..45).random()) }
    var b by remember { mutableIntStateOf((11..39).random()) }
    var answer by remember { mutableStateOf("") }
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("🧮 تحدي الحساب الذهني السريع", fontWeight = FontWeight.Bold)
            Text("كم ناتج: $a + $b ؟", fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = answer, onValueChange = { answer = it }, modifier = Modifier.weight(1f), label = { Text("الإجابة") })
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (answer.toIntOrNull() == a + b) {
                        onWin()
                        a = (12..45).random(); b = (11..39).random(); answer = ""
                    } else Toast.makeText(context, "حاول مرة أخرى!", Toast.LENGTH_SHORT).show()
                }) { Text("تحقق") }
            }
        }
    }
}

@Composable
fun SettingsDialog(prefs: android.content.SharedPreferences, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf(prefs.getString("gemini_key", "") ?: "") }
    var newPass by remember { mutableStateOf(prefs.getString("admin_pass", "1234") ?: "1234") }
    var isRestDay by remember { mutableStateOf(prefs.getBoolean("is_rest_day", false)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚙️ إعدادات رفيقي والحماية") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("تفعيل يوم الراحة (فتح كل شيء)")
                    Switch(checked = isRestDay, onCheckedChange = { isRestDay = it })
                }
                OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("مفتاح Gemini API المجاني") })
                OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("تغيير كلمة سر الإعدادات") })
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }, modifier = Modifier.fillMaxWidth()) { Text("1. تفعيل خدمة مراقبة يوتيوب والقفل") }
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }, modifier = Modifier.fillMaxWidth()) { Text("2. تعيين رفيقي كواجهة أساسية (Launcher)") }
            }
        },
        confirmButton = {
            Button(onClick = {
                prefs.edit()
                    .putString("gemini_key", apiKey.trim())
                    .putString("admin_pass", newPass.trim())
                    .putBoolean("is_rest_day", isRestDay)
                    .apply()
                onDismiss()
            }) { Text("حفظ") }
        }
    )
}

@Composable
fun AppDrawerDialog(prefs: android.content.SharedPreferences, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val apps = remember {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0).map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .distinctBy { it.first }
            .sortedBy { it.second }
    }
    var blockedSet by remember { mutableStateOf(prefs.getStringSet("blocked_apps", emptySet())?.toMutableSet() ?: mutableSetOf()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📱 التطبيقات (اضغط لفتح / القفل للحظر)") },
        text = {
            LazyColumn(modifier = Modifier.height(350.dp)) {
                items(apps) { (pkg, name) ->
                    val isBlocked = blockedSet.contains(pkg)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f).clickable {
                                val launch = pm.getLaunchIntentForPackage(pkg)
                                if (launch != null) context.startActivity(launch)
                            },
                            color = if (isBlocked) Color.Red else Color.White
                        )
                        IconButton(onClick = {
                            val newSet = blockedSet.toMutableSet()
                            if (isBlocked) newSet.remove(pkg) else newSet.add(pkg)
                            blockedSet = newSet
                            prefs.edit().putStringSet("blocked_apps", newSet).apply()
                        }) {
                            Icon(
                                if (isBlocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "حظر",
                                tint = if (isBlocked) Color.Red else Color.Green
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("إغلاق") } }
    )
}
