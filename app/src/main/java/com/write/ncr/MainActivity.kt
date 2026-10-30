package com.write.ncr

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale

private data class Plant(
    val id: String,
    val name: String,
    val emoji: String,
    val hours: Long,
    val rarity: String,
    val description: String
)

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("was_planted_data", Context.MODE_PRIVATE)
    }

    private val plants = listOf(
        Plant(
            "flower", "زهرة القمر", "🌸", 2,
            "شائعة", "زهرة صغيرة تنمو بهدوء تحت ضوء القمر."
        ),
        Plant(
            "mushroom", "فطر الأحلام", "🍄", 4,
            "غير شائع", "فطر غامض يعيش في أعماق الغابات."
        ),
        Plant(
            "crystal", "النبتة البلورية", "💎", 6,
            "نادرة", "نبتة تتشكل أوراقها من البلورات."
        ),
        Plant(
            "ancient", "شجرة الحكمة", "🌳", 8,
            "نادرة", "شجرة قديمة تحمل أسراراً لا يعرفها أحد."
        ),
        Plant(
            "moon", "زهرة النجوم", "🌺", 12,
            "نادرة جداً", "زهرة لا تتفتح إلا بعد رحلة طويلة."
        ),
        Plant(
            "giant", "العملاق الأخضر", "🌲", 24,
            "أسطورية", "شجرة هائلة تتجاوز حدود الغابة."
        ),
        Plant(
            "mythical", "شجرة الخلود", "🌳", 36,
            "شديدة الندرة", "كائن أسطوري لا يظهر إلا للمثابرين."
        ),
        Plant(
            "fungus_king", "ملك الفطر", "🍄", 18,
            "أسطورية", "فطر نادر يستطيع إنشاء مملكة كاملة."
        )
    )

    private lateinit var plant: Plant

    private lateinit var root: LinearLayout
    private lateinit var plantIcon: TextView
    private lateinit var plantName: TextView
    private lateinit var plantDescription: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var timerText: TextView
    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var collectionText: TextView
    private lateinit var totalText: TextView

    private var progressMs = 0L
    private var startedAt = 0L
    private var active = false
    private var discovered = false

    private val handler = Handler(Looper.getMainLooper())

    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            if (active) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val targetMs: Long
        get() = plant.hours * 60L * 60L * 1000L

    private fun currentProgress(): Long {
        return if (active) {
            progressMs + (System.currentTimeMillis() - startedAt)
        } else {
            progressMs
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(9, 17, 15)
        window.navigationBarColor = Color.rgb(9, 17, 15)

        loadState()
        buildInterface()
        refresh()
    }

    private fun loadState() {
        var id = prefs.getString("plant_id", null)

        if (id == null) {
            plant = plants.random()
            prefs.edit().putString("plant_id", plant.id).apply()
        } else {
            plant = plants.find { it.id == id } ?: plants.random()
        }

        progressMs = prefs.getLong("progress", 0L)
        startedAt = prefs.getLong("started_at", 0L)
        active = prefs.getBoolean("active", false)
        discovered = prefs.getBoolean("discovered", false)
    }

    private fun saveState() {
        prefs.edit()
            .putLong("progress", progressMs)
            .putLong("started_at", startedAt)
            .putBoolean("active", active)
            .putBoolean("discovered", discovered)
            .putString("plant_id", plant.id)
            .apply()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun background(
        color: Int,
        radius: Int,
        strokeColor: Int = Color.TRANSPARENT
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (strokeColor != Color.TRANSPARENT) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            typeface = if (bold) {
                Typeface.create("sans-serif", Typeface.BOLD)
            } else {
                Typeface.create("sans-serif", Typeface.NORMAL)
            }
            setPadding(dp(5), dp(5), dp(5), dp(5))
        }
    }

    private fun makeButton(value: String): Button {
        return Button(this).apply {
            text = value
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = background(
                Color.rgb(45, 105, 75),
                16
            )
            minHeight = dp(54)
            stateListAnimator = null
        }
    }

    private fun makeCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = background(
                Color.rgb(19, 30, 26),
                22,
                Color.rgb(42, 66, 54)
            )
            layoutParams = LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                setMargins(0, dp(8), 0, dp(8))
            }
        }
    }

    private fun addSpace(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun buildInterface() {

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(9, 17, 15))
            fillViewport = true
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(30))
        }

        scroll.addView(root)
        setContentView(scroll)

        val header = text(
            "WAS PLANTED",
            25f,
            Color.rgb(220, 238, 216),
            true
        )
        root.addView(header)

        root.addView(
            text(
                "WHAT WAS PLANTED?",
                11f,
                Color.rgb(125, 164, 137),
                true
            )
        )

        addSpace(root, 18)

        val hero = makeCard()

        hero.addView(
            text(
                "YOUR MYSTERIOUS GARDEN",
                11f,
                Color.rgb(137, 180, 147),
                true
            )
        )

        addSpace(hero, 15)

        plantIcon = text(
            "🌱",
            86f,
            Color.WHITE
        )

        hero.addView(
            plantIcon,
            LinearLayout.LayoutParams(-1, dp(135))
        )

        addSpace(hero, 8)

        plantName = text(
            "بذرتك المجهولة",
            23f,
            Color.rgb(225, 239, 219),
            true
        )
        hero.addView(plantName)

        plantDescription = text(
            "هناك شيء ينمو هنا...\nلكن ماذا سيكون؟",
            13f,
            Color.rgb(154, 177, 158)
        )
        hero.addView(plantDescription)

        addSpace(hero, 15)

        statusText = text(
            "في انتظار بداية الرحلة",
            13f,
            Color.rgb(174, 205, 174)
        )
        hero.addView(statusText)

        root.addView(hero)

        addSpace(root, 12)

        val growthCard = makeCard()

        growthCard.addView(
            text(
                "رحلة النمو",
                19f,
                Color.rgb(223, 238, 218),
                true
            )
        )

        addSpace(growthCard, 15)

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 1000
            progress = 0
            progressTintList = ColorStateList.valueOf(
                Color.rgb(120, 204, 139)
            )
            progressBackgroundTintList = ColorStateList.valueOf(
                Color.rgb(43, 58, 48)
            )
            progressDrawable?.let {
                it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            }
        }

        growthCard.addView(
            progressBar,
            LinearLayout.LayoutParams(-1, dp(12))
        )

        addSpace(growthCard, 12)

        progressText = text(
            "0%",
            16f,
            Color.rgb(151, 214, 158),
            true
        )
        growthCard.addView(progressText)

        addSpace(growthCard, 8)

        timerText = text(
            "00:00:00",
            34f,
            Color.rgb(230, 240, 224),
            true
        )
        growthCard.addView(timerText)

        growthCard.addView(
            text(
                "وقت الدراسة المتراكم",
                12f,
                Color.rgb(142, 165, 145)
            )
        )

        addSpace(growthCard, 12)

        root.addView(growthCard)

        addSpace(root, 12)

        val controlCard = makeCard()

        controlCard.addView(
            text(
                "وقت العناية بالبذرة",
                18f,
                Color.rgb(223, 238, 218),
                true
            )
        )

        addSpace(controlCard, 12)

        startButton = makeButton("ابدأ الدراسة 🌱")

        controlCard.addView(
            startButton,
            LinearLayout.LayoutParams(-1, dp(55))
        )

        startButton.setOnClickListener {
            handleStudyButton()
        }

        addSpace(controlCard, 8)

        controlCard.addView(
            text(
                "يمكنك إيقاف المؤقت واستكماله لاحقاً.\nالنمو يعتمد على وقت الدراسة الفعلي المسجل.",
                12f,
                Color.rgb(142, 165, 145)
            )
        )

        root.addView(controlCard)

        addSpace(root, 12)

        val collectionCard = makeCard()

        collectionCard.addView(
            text(
                "🌿 حديقتك السرية",
                20f,
                Color.rgb(225, 239, 219),
                true
            )
        )

        addSpace(collectionCard, 10)

        collectionText = text(
            "لم تكتشف أي كائن حتى الآن",
            13f,
            Color.rgb(154, 177, 158)
        )

        collectionCard.addView(collectionText)

        addSpace(collectionCard, 10)

        totalText = text(
            "الاكتشافات: 0",
            14f,
            Color.rgb(151, 214, 158),
            true
        )

        collectionCard.addView(totalText)

        root.addView(collectionCard)

        addSpace(root, 18)

        root.addView(
            text(
                "EVERY SEED HAS A SECRET",
                11f,
                Color.rgb(91, 128, 99),
                true
            )
        )

        root.addView(
            text(
                "ازرع وقتك... واكتشف ما سيولد منه.",
                12f,
                Color.rgb(117, 146, 121)
            )
        )
    }

    private fun handleStudyButton() {

        if (discovered) {
            plantNewSeed()
            return
        }

        if (active) {
            progressMs = currentProgress()
            active = false
            startedAt = 0L
            handler.removeCallbacks(ticker)
            saveState()
            refresh()
        } else {
            active = true
            startedAt = System.currentTimeMillis()
            saveState()
            handler.removeCallbacks(ticker)
            handler.post(ticker)
            refresh()
        }
    }

    private fun plantNewSeed() {

        plant = plants.random()

        progressMs = 0L
        startedAt = 0L
        active = false
        discovered = false

        saveState()
        refresh()
    }

    private fun finishPlant() {

        if (discovered) return

        progressMs = targetMs
        active = false
        startedAt = 0L
        discovered = true

        val old = prefs.getStringSet(
            "collection",
            emptySet()
        ) ?: emptySet()

        val updated = HashSet(old)
        updated.add(plant.id)

        prefs.edit()
            .putStringSet("collection", updated)
            .apply()

        saveState()
        handler.removeCallbacks(ticker)
    }

    private fun formatTime(milliseconds: Long): String {

        val seconds = milliseconds / 1000

        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return String.format(
            Locale.US,
            "%02d:%02d:%02d",
            hours,
            minutes,
            secs
        )
    }

    private fun refresh() {

        var current = currentProgress()

        if (!discovered && current >= targetMs) {
            finishPlant()
            current = targetMs
        }

        val percentage = (
            current.toDouble() /
                targetMs.toDouble() * 100.0
            ).toInt().coerceIn(0, 100)

        progressBar.progress = percentage * 10

        progressText.text = "$percentage%"

        timerText.text = formatTime(current)

        if (discovered) {

            plantIcon.text = plant.emoji
            plantName.text = plant.name

            plantDescription.text =
                "${plant.description}\n\n" +
                "الندرة: ${plant.rarity}"

            statusText.text = "✨ اكتمل النمو! لقد اكتشفت سراً جديداً."

            startButton.text = "ازرع بذرة جديدة 🌱"

        } else {

            plantIcon.text = when {
                percentage < 15 -> "🌰"
                percentage < 35 -> "🌱"
                percentage < 60 -> "🌿"
                percentage < 85 -> "🍃"
                else -> "✨"
            }

            plantName.text = "؟؟؟"

            plantDescription.text =
                "بذرتك لا تزال مجهولة.\n" +
                "لا أحد يعرف ماذا ستصبح."

            statusText.text = when {
                active -> "🌱 تنمو بذرتك الآن..."
                percentage > 0 -> "⏸ النمو متوقف مؤقتاً"
                else -> "🌰 بذرة تنتظر أول جلسة دراسة"
            }

            startButton.text = if (active) {
                "إيقاف الدراسة مؤقتاً ⏸"
            } else {
                "ابدأ الدراسة 🌱"
            }
        }

        updateCollection()
    }

    private fun updateCollection() {

        val collection = prefs.getStringSet(
            "collection",
            emptySet()
        ) ?: emptySet()

        val discoveredPlants = plants.filter {
            collection.contains(it.id)
        }

        totalText.text =
            "الاكتشافات: ${discoveredPlants.size} / ${plants.size}"

        collectionText.text = if (discoveredPlants.isEmpty()) {

            "حديقتك فارغة حالياً...\nابدأ رحلتك لاكتشاف أول كائن."

        } else {

            discoveredPlants.joinToString("\n\n") {
                "${it.emoji}  ${it.name}  ·  ${it.rarity}"
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::root.isInitialized) {
            refresh()

            handler.removeCallbacks(ticker)

            if (active) {
                handler.post(ticker)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveState()
        handler.removeCallbacks(ticker)
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        super.onDestroy()
    }
}