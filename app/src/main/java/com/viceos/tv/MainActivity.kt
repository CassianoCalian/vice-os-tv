package com.viceos.tv

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

/** CALIAN TV 1.0 — a lightweight, remote-first Android TV launcher. */
class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clock: TextView
    private lateinit var heroBackground: ImageView
    private lateinit var appsStrip: HorizontalScrollView
    private lateinit var featureStrip: HorizontalScrollView
    private lateinit var channelStrip: HorizontalScrollView
    private var scale = 1f
    private var installedApps: List<ResolveInfo> = emptyList()
    private var wallpaperAnimator: ObjectAnimator? = null

    private val white = Color.WHITE
    private val muted = Color.rgb(199, 202, 222)
    private val blue = Color.rgb(51, 144, 255)
    private val purple = Color.rgb(154, 69, 255)

    private fun u(n: Number) = (n.toFloat() * scale).roundToInt()

    private fun text(value: String, size: Float, bold: Boolean = false, color: Int = white) = TextView(this).apply {
        text = value
        setTextColor(color)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, size * scale)
        if (bold) setTypeface(Typeface.create("sans-serif", Typeface.BOLD))
        gravity = Gravity.CENTER_VERTICAL
        includeFontPadding = false
    }

    private fun rounded(fill: Int, radius: Float = 18f, stroke: Int? = null, strokeWidth: Int = 2): GradientDrawable =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = u(radius).toFloat()
            if (stroke != null) setStroke(u(strokeWidth).coerceAtLeast(1), stroke)
        }

    private fun gradient(colors: IntArray, radius: Float = 18f) =
        GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors).apply { cornerRadius = u(radius).toFloat() }

    private fun put(container: FrameLayout, view: View, x: Int, y: Int, width: Int, height: Int) {
        val lp = FrameLayout.LayoutParams(if (width == -1) -1 else u(width), if (height == -1) -1 else u(height))
        lp.leftMargin = u(x)
        lp.topMargin = u(y)
        container.addView(view, lp)
    }

    private fun focusStyle(view: View, base: Int = Color.argb(200, 13, 20, 42), radius: Float = 17f,
                           selected: Int = Color.argb(235, 73, 33, 176), large: Boolean = false) {
        view.isFocusable = true
        view.isFocusableInTouchMode = false
        view.background = rounded(base, radius, Color.argb(75, 117, 147, 255), 1)
        view.setOnFocusChangeListener { v, focused ->
            v.background = if (focused) rounded(selected, radius, Color.rgb(66, 168, 255), 3)
                           else rounded(base, radius, Color.argb(75, 117, 147, 255), 1)
            v.animate().cancel()
            v.animate().scaleX(if (focused) 1.045f else 1f)
                .scaleY(if (focused) 1.045f else 1f)
                .translationZ(if (focused) u(12).toFloat() else 0f)
                .setDuration(if (large) 175 else 140).setInterpolator(DecelerateInterpolator()).start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        val dm = resources.displayMetrics
        scale = min(dm.widthPixels / 1600f, dm.heightPixels / 900f).coerceAtLeast(.40f)
        installedApps = findApps()

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(3, 6, 18))
            clipChildren = false
            clipToPadding = false
        }
        setContentView(root)

        heroBackground = ImageView(this).apply {
            setImageResource(R.drawable.calian_hero)
            scaleType = ImageView.ScaleType.CENTER_CROP
            alpha = .95f
        }
        put(root, heroBackground, 0, 0, 1600, 552)
        put(root, View(this).apply {
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.TRANSPARENT, Color.argb(15, 3, 6, 18), Color.rgb(3, 6, 18)))
        }, 0, 410, 1600, 205)

        buildHeader(root)
        buildMenu(root)
        buildHero(root)
        buildApplications(root)
        buildHighlights(root)
        buildChannels(root)

        // Start focused on the first real shortcut, just like a TV launcher.
        appsStrip.post { (appsStrip.getChildAt(0) as? LinearLayout)?.getChildAt(0)?.requestFocus() }
        startClock()
        startWallpaperMotion()
    }

    private fun buildHeader(root: FrameLayout) {
        val mark = text("▶", 43f, true, Color.rgb(51, 190, 252))
        put(root, mark, 47, 27, 57, 57)
        put(root, text("CALIAN", 38f, true), 102, 28, 170, 53)
        put(root, text("TV", 38f, true, purple), 269, 28, 78, 53)
        put(root, text("◉", 24f, true, muted), 1315, 32, 45, 46)
        clock = text("", 23f, true)
        clock.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        put(root, clock, 1362, 28, 202, 51)
    }

    private fun buildMenu(root: FrameLayout) {
        val options = listOf(
            Triple("⌂", "Início", "home"),
            Triple("▣", "TV ao Vivo", "live"),
            Triple("▸", "Filmes", "films"),
            Triple("▤", "Séries", "series"),
            Triple("⊞", "Canais", "channels"),
            Triple("☆", "Favoritos", "favorites"),
            Triple("⚙", "Configurações", "settings")
        )
        options.forEachIndexed { i, option ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(u(12), 0, u(8), 0)
            }
            val symbol = text(option.first, 30f, true)
            symbol.gravity = Gravity.CENTER
            row.addView(symbol, LinearLayout.LayoutParams(u(43), -1))
            row.addView(text(option.second, 20f, i == 0), LinearLayout.LayoutParams(-1, -1))
            focusStyle(row,
                if (i == 0) Color.argb(150, 56, 40, 160) else Color.TRANSPARENT,
                27f, Color.argb(225, 69, 47, 186))
            row.setOnClickListener { onMenu(option.third) }
            put(root, row, 34, 108 + i * 51, 237, 47)
        }
    }

    private fun buildHero(root: FrameLayout) {
        put(root, text("EM DESTAQUE", 17f, true, muted), 325, 104, 190, 29)
        put(root, text("GRANDES", 69f, true), 325, 125, 500, 77)
        put(root, text("HISTÓRIAS", 70f, true, Color.rgb(160, 71, 255)), 325, 188, 545, 79)
        val subtitle = text("Filmes, séries e muito mais,\nno seu tempo.", 23f)
        subtitle.gravity = Gravity.CENTER_VERTICAL
        put(root, subtitle, 326, 275, 455, 69)
        val play = text("▶   Assistir agora", 20f, true).apply { gravity = Gravity.CENTER }
        focusStyle(play, Color.argb(180, 17, 29, 64), 34f, Color.argb(230, 53, 57, 156))
        play.setOnClickListener { openByKeywords(listOf("globoplay", "netflix", "prime video", "youtube", "max"), "Nenhum app de vídeo compatível encontrado") }
        put(root, play, 325, 365, 222, 49)
    }

    private fun heading(root: FrameLayout, value: String, y: Int) {
        put(root, text(value, 20f, true), 44, y, 520, 30)
    }

    private fun newStrip(width: Int, height: Int): Pair<HorizontalScrollView, LinearLayout> {
        val strip = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            isFillViewport = false
            clipToPadding = false
            clipChildren = false
            isFocusable = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        val items = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            clipChildren = false
            clipToPadding = false
            setPadding(u(6), u(6), u(6), u(6))
        }
        strip.addView(items, ViewGroup.LayoutParams(-2, u(height)))
        return strip to items
    }

    private fun findApps(): List<ResolveInfo> {
        val pm = packageManager
        val tv = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val normal = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val found = pm.queryIntentActivities(tv, 0) + pm.queryIntentActivities(normal, 0)
        val priority = listOf("netflix", "youtube", "disney", "prime video", "amazon", "globoplay", "max", "paramount", "spotify", "boosteroid", "chrome", "pluto")
        return found.distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != packageName }
            .sortedWith(compareBy<ResolveInfo> { app ->
                val name = app.loadLabel(pm).toString().lowercase(Locale.ROOT)
                val pkg = app.activityInfo.packageName.lowercase(Locale.ROOT)
                val idx = priority.indexOfFirst { name.contains(it) || pkg.contains(it.replace(" ", "")) }
                if (idx < 0) 100 else idx
            }.thenBy { it.loadLabel(pm).toString().lowercase(Locale.ROOT) })
    }

    private fun buildApplications(root: FrameLayout) {
        heading(root, "Seus aplicativos", 470)
        val (strip, line) = newStrip(1540, 113)
        appsStrip = strip
        val live = appTile("TV ao Vivo", null, true)
        live.setOnClickListener { openByKeywords(listOf("globoplay", "pluto", "tivimate", "iptv", "tv"), "Nenhum aplicativo de TV ao vivo instalado") }
        line.addView(live, LinearLayout.LayoutParams(u(143), u(96)).apply { rightMargin = u(13) })
        installedApps.forEach { app ->
            val view = appTile(app.loadLabel(packageManager).toString(), app, false)
            view.setOnClickListener { launchPackage(app.activityInfo.packageName, app.activityInfo.name) }
            line.addView(view, LinearLayout.LayoutParams(u(143), u(96)).apply { rightMargin = u(13) })
        }
        if (installedApps.isEmpty()) {
            val info = text("Abra Configurações para instalar aplicativos", 19f, false, muted)
            line.addView(info, LinearLayout.LayoutParams(u(500), u(96)))
        }
        put(root, strip, 35, 506, 1540, 118)
    }

    private fun appTile(label: String, app: ResolveInfo?, live: Boolean): View {
        val cell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(u(8), u(8), u(8), u(6))
        }
        if (app != null) {
            val icon = ImageView(this).apply {
                setImageDrawable(app.loadIcon(packageManager))
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            cell.addView(icon, LinearLayout.LayoutParams(u(48), u(53)))
        } else {
            val icon = text("▣", 39f, true)
            icon.gravity = Gravity.CENTER
            cell.addView(icon, LinearLayout.LayoutParams(u(61), u(55)))
        }
        val title = text(label, 16f, true).apply {
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        cell.addView(title, LinearLayout.LayoutParams(-1, u(26)))
        focusStyle(cell, if (live) Color.rgb(91, 40, 213) else Color.argb(220, 11, 21, 49),
            14f, Color.rgb(75, 44, 199), true)
        return cell
    }

    private fun buildHighlights(root: FrameLayout) {
        heading(root, "Em alta agora", 622)
        val (strip, line) = newStrip(1550, 151)
        featureStrip = strip
        val posters = listOf(
            Triple(R.drawable.featured_1, listOf("max", "hbo"), "Abra o Max para explorar o catálogo"),
            Triple(R.drawable.featured_2, listOf("netflix"), "Abra a Netflix para explorar o catálogo"),
            Triple(R.drawable.featured_3, listOf("globoplay"), "Abra o Globoplay para explorar o catálogo"),
            Triple(R.drawable.featured_4, listOf("espn", "disney"), "Abra o Disney+ para explorar esportes")
        )
        posters.forEach { (drawable, keys, error) ->
            val poster = FrameLayout(this)
            focusStyle(poster, Color.argb(180, 11, 21, 46), 15f)
            val img = ImageView(this).apply {
                setImageResource(drawable)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            poster.addView(img, FrameLayout.LayoutParams(-1, -1).apply { setMargins(u(3), u(3), u(3), u(3)) })
            poster.setOnClickListener { openByKeywords(keys, error) }
            line.addView(poster, LinearLayout.LayoutParams(u(370), u(135)).apply { rightMargin = u(15) })
        }
        put(root, strip, 37, 657, 1537, 160)
    }

    private fun buildChannels(root: FrameLayout) {
        heading(root, "Canais em destaque", 800)
        val (strip, line) = newStrip(1540, 79)
        channelStrip = strip
        val keys = listOf("globoplay", "sbt", "bandplay", "record", "espn", "sportv", "cnn", "tnt", "discovery", "max")
        repeat(10) { i ->
            val icon = ImageView(this).apply {
                setImageResource(resources.getIdentifier("channel_${i+1}", "drawable", packageName))
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            focusStyle(icon, Color.argb(180, 12, 24, 49), 13f)
            icon.setOnClickListener { openByKeywords(listOf(keys[i]), "Este canal requer um aplicativo compatível instalado") }
            line.addView(icon, LinearLayout.LayoutParams(u(144), u(68)).apply { rightMargin = u(14) })
        }
        put(root, strip, 37, 835, 1537, 79)
    }

    private fun onMenu(where: String) {
        when (where) {
            "home" -> appsStrip.smoothScrollTo(0, 0)
            "live" -> openByKeywords(listOf("globoplay", "pluto", "tivimate", "iptv", "tv"), "Nenhum aplicativo de TV ao vivo instalado")
            "films" -> openByKeywords(listOf("netflix", "prime video", "globoplay", "max", "disney"), "Instale um aplicativo de filmes")
            "series" -> openByKeywords(listOf("netflix", "max", "disney", "globoplay"), "Instale um aplicativo de séries")
            "channels" -> { channelStrip.smoothScrollTo(0, 0); (channelStrip.getChildAt(0) as? LinearLayout)?.getChildAt(0)?.requestFocus() }
            "favorites" -> { (appsStrip.getChildAt(0) as? LinearLayout)?.getChildAt(0)?.requestFocus(); Toast.makeText(this, "Escolha um aplicativo na faixa principal", Toast.LENGTH_SHORT).show() }
            "settings" -> try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (_: Exception) { Toast.makeText(this, "Configurações indisponíveis", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun openByKeywords(keys: List<String>, message: String) {
        val pm = packageManager
        val match = keys.asSequence().mapNotNull { key ->
            installedApps.firstOrNull { app ->
                app.loadLabel(pm).toString().contains(key, ignoreCase = true) ||
                app.activityInfo.packageName.contains(key.replace(" ", ""), ignoreCase = true)
            }
        }.firstOrNull()
        if (match == null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        else launchPackage(match.activityInfo.packageName, match.activityInfo.name)
    }

    private fun launchPackage(pkg: String, activityName: String) {
        try {
            val launch = packageManager.getLeanbackLaunchIntentForPackage(pkg)
                ?: packageManager.getLaunchIntentForPackage(pkg)
                ?: Intent().setClassName(pkg, activityName)
            startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi possível abrir este aplicativo", Toast.LENGTH_SHORT).show()
        }
    }

    private val timeUpdater = object : Runnable {
        override fun run() {
            if (::clock.isInitialized) {
                clock.text = SimpleDateFormat("HH:mm  ·  EEE, dd MMM", Locale("pt", "BR")).format(Date())
            }
            handler.postDelayed(this, 30000L)
        }
    }
    private fun startClock() = handler.post(timeUpdater).let { Unit }

    private fun startWallpaperMotion() {
        wallpaperAnimator = ObjectAnimator.ofFloat(heroBackground, "translationX", 0f, -u(10).toFloat(), 0f).apply {
            duration = 26000
            repeatCount = ValueAnimator.INFINITE
            interpolator = android.view.animation.LinearInterpolator()
            start()
        }
    }

    override fun onDestroy() {
        wallpaperAnimator?.cancel()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
