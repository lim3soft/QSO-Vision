package com.mmdvm.qsovision

import android.annotation.SuppressLint
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.SystemClock
import android.content.Intent
import android.content.res.ColorStateList
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Typeface
import android.view.*
import android.webkit.*
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mmdvm.qsovision.ui.QsoViewModel
import com.mmdvm.qsovision.ui.ScreenDimPolicy
import com.mmdvm.qsovision.ui.ActivityWaveView
import com.mmdvm.qsovision.service.HoselinePlaybackService
import com.mmdvm.qsovision.util.BrowserSupport
import com.mmdvm.qsovision.util.BrandMeisterConnectivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private val vm by viewModels<QsoViewModel>()
    private val prefs by lazy { getSharedPreferences("monitor", MODE_PRIVATE) }
    private lateinit var page: FrameLayout
    private lateinit var playerHost: FrameLayout
    private lateinit var input: EditText
    private lateinit var headline: TextView
    private lateinit var details: TextView
    private lateinit var clock: TextView
    private lateinit var wave: ActivityWaveView
    private lateinit var history: LinearLayout
    private lateinit var audioStatus: TextView
    private lateinit var muteButton: MaterialButton
    private lateinit var historyButton: MaterialButton
    private lateinit var compactToggle: MaterialButton
    private lateinit var selector: LinearLayout
    private var compactMode = false
    private val dimPolicy = ScreenDimPolicy { SystemClock.elapsedRealtime() }
    private lateinit var screenButton: MaterialButton
    private val dimTimes = intArrayOf(0, 30, 60, 300, -1)
    private val dimLabels = arrayOf("Always on", "30 seconds", "1 minute", "5 minutes", "Smart • 10 seconds of silence")
    private var lastHeard: WebView? = null
    private data class Heard(val call: String, val country: String, val tg: Int, val age: String, val duration: String, val active: Boolean)
    private val heardHistory = ArrayDeque<Heard>()
    private var liveHeard: Heard? = null
    private var renderedHistory = ""
    private var metadataFailure: String? = null
    private var metadataStarted = SystemClock.elapsedRealtime()
    private var muted = false
    private var historyLimit = 10
    private lateinit var metadataStatus: TextView
    private val metadataScript by lazy { assets.open("lastheard.js").bufferedReader().use { it.readText() } }

    /**
     * Scale the dashboard for compact phones.  Android density alone is not
     * enough: two phones can report the same density while having very
     * different usable widths. Keep normal devices unchanged and only reduce
     * spacing/control sizes when the available width is genuinely small.
     */
    private val layoutScale: Float
        get() {
            val metrics = resources.displayMetrics
            val widthDp = metrics.widthPixels / metrics.density
            return (widthDp / 360f).coerceIn(0.72f, 1f)
        }
    private fun dp(value: Int) = (value * resources.displayMetrics.density * layoutScale).roundToInt()
    // Use physical density, not scaledDensity, so the phone's accessibility
    // font-size preference cannot make the dashboard clip on compact screens.
    private fun textPx(value: Float) = value * resources.displayMetrics.density * layoutScale
    private fun isLandscape() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    private fun label(value: String, size: Float = 14f, color: Int = Color.WHITE) =
        TextView(this).apply { text = value; setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textPx(size)); setTextColor(color); setPadding(dp(10), dp(6), dp(10), dp(6)) }
    private fun button(value: String, action: () -> Unit) = MaterialButton(this).apply {
        text = value; isAllCaps = false; setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textPx(13f))
        cornerRadius = dp(16); strokeWidth = dp(1)
        strokeColor = ColorStateList.valueOf(0xff2b91ae.toInt())
        backgroundTintList = ColorStateList.valueOf(0xff12374b.toInt())
        rippleColor = ColorStateList.valueOf(0x5534e4ff)
        setTextColor(0xffb8f5ff.toInt()); setTypeface(typeface, Typeface.BOLD)
        minHeight = dp(52); minimumWidth = dp(64)
        setPadding(dp(14), dp(4), dp(14), dp(4))
        setOnClickListener { action() }
    }
    private fun iconButton(description: String, drawable: Int, action: () -> Unit) = button("", action).apply {
        setIconResource(drawable); iconSize = dp(24); iconPadding = 0
        iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        iconTint = ColorStateList.valueOf(0xffb8f5ff.toInt())
        minWidth = dp(48); minimumWidth = dp(48); minHeight = dp(48)
        insetTop = 0; insetBottom = 0; setPadding(dp(12), 0, dp(12), 0)
        describeIcon(this, description)
    }
    private fun compactButton(value: String, description: String, action: () -> Unit) = button(value, action).apply {
        minWidth = dp(42); minimumWidth = dp(42); minHeight = dp(42); minimumHeight = dp(42)
        setPadding(dp(6), 0, dp(6), 0)
        describeIcon(this, description)
    }
    private fun describeIcon(view: MaterialButton, description: String) {
        view.contentDescription = description
        androidx.appcompat.widget.TooltipCompat.setTooltipText(view, description)
    }
    private fun updateMuteIcon(state: String = HoselinePlaybackService.status.value) {
        val stopped = muted || state.startsWith("Audio stopped") || state.contains("unavailable") || state.contains("rejected")
        muteButton.setIconResource(if (stopped) R.drawable.ic_volume_off else R.drawable.ic_volume_on)
        describeIcon(muteButton, if (muted) "Unmute audio" else if (stopped) "Retry audio" else "Mute audio")
    }
    private fun panel() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), dp(12), dp(12), dp(12))
        background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(0xff133953.toInt(), 0xff0d2438.toInt())).apply {
            cornerRadius = dp(20).toFloat(); setStroke(dp(1), 0xff226583.toInt())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rememberDisplayProfile()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        vm.select(prefs.getInt("tg", 46020).coerceIn(1, 16777215))
        muted = prefs.getBoolean("muted", false)
        historyLimit = prefs.getInt("historyLimit", 10).let { if (it in intArrayOf(10, 20, 50, 100)) it else 10 }
        compactMode = prefs.getBoolean("compactMode", false)
        dimPolicy.modeSeconds = prefs.getInt("dimSeconds", 0)
        dimPolicy.reset()
        page = FrameLayout(this)
        ViewCompat.setOnApplyWindowInsetsListener(page) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        // The attached player stays behind the opaque dashboard.
        playerHost = FrameLayout(this)
        page.addView(playerHost, FrameLayout.LayoutParams(-1, -1))
        val browser = BrowserSupport.create(this)
        metadataFailure = browser.error
        lastHeard = browser.view?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    if (message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                        android.util.Log.w("QsoMetadata", "Web page: ${message.message()}")
                        if (message.message().contains("SyntaxError")) metadataFailure =
                            "BrandMeister script unsupported by this browser • update Chrome / Android System WebView to its latest compatible version"
                    }
                    return true
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: android.net.http.SslError) {
                    handler.cancel()
                    metadataFailure = "Secure metadata connection rejected (${error.primaryError}). Check device date and update Chrome / WebView."
                    android.util.Log.w("QsoMetadata", metadataFailure!!)
                }
                override fun onPageFinished(view: WebView, url: String) {
                    pollLastHeard()
                }
                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    if (request.isForMainFrame && ::metadataStatus.isInitialized) {
                        liveHeard = null
                        metadataFailure = "Metadata unavailable: ${error.description} • Monitor to retry"
                        metadataStatus.text = metadataFailure
                    }
                }
            }
            // Render BrandMeister behind the native dashboard; only extracted
            // matching fields are shown to the user.
            alpha = 0.01f
        }
        lastHeard?.let { playerHost.addView(it, FrameLayout.LayoutParams(-1, -1)) }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(0xff06111f.toInt())
        }
        val titleView = label("◉ QSO VISION", 26f, Color.CYAN)
        val authorView = label("by 9W3SPY", 13f, 0xff93b8c8.toInt())
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            // Keep the author beside the title instead of consuming a
            // separate far-right header area on wide screens.
            addView(titleView, LinearLayout.LayoutParams(-2, -2))
            addView(authorView, LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(10) })
        }
        column.addView(header)
        selector = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        input = EditText(this).apply { inputType = 2; setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textPx(16f)); setPadding(dp(4), 0, dp(4), 0); setSingleLine(); setText(vm.selected.value.toString()); hint = "TG"; contentDescription = "Talkgroup number" }
        selector.addView(input, LinearLayout.LayoutParams(0, dp(56), 1f))
        fun addControl(control: MaterialButton) = selector.addView(control,
            LinearLayout.LayoutParams(dp(48), dp(48)).apply { marginStart = dp(4) })
        addControl(iconButton("Monitor talkgroup", R.drawable.ic_monitor) { selectTalkgroup() }.apply {
            backgroundTintList = ColorStateList.valueOf(0xff16cee6.toInt())
            iconTint = ColorStateList.valueOf(0xff041723.toInt())
        })
        muteButton = iconButton("Mute audio", R.drawable.ic_volume_on) {
            muted = if (!HoselinePlaybackService.isRunning) false else !muted
            prefs.edit().putBoolean("muted", muted).apply()
            syncAudio()
        }
        addControl(muteButton)
        val dimIndex = dimTimes.indexOf(dimPolicy.modeSeconds).takeIf { it >= 0 } ?: 0
        screenButton = iconButton("Screen dimming • ${dimLabels[dimIndex]}", R.drawable.ic_dim) { chooseScreenMode() }
        addControl(screenButton)
        addControl(iconButton("Rotate screen", R.drawable.ic_rotate) { rotateScreen() })
        addControl(iconButton("Exit app", R.drawable.ic_exit) { exitApp() })
        column.addView(selector)
        val card = panel()
        headline = label("", 68f).apply { gravity = Gravity.CENTER; maxLines = 1; setTypeface(typeface, Typeface.BOLD) }
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(headline,
            textPx(18f).roundToInt(), textPx(72f).roundToInt(), 2, android.util.TypedValue.COMPLEX_UNIT_PX)
        card.addView(headline, LinearLayout.LayoutParams(-1, dp(130)))
        details = label("").apply { gravity = Gravity.CENTER }; card.addView(details)
        clock = label("", 24f, Color.CYAN).apply { gravity = Gravity.CENTER }; card.addView(clock)
        wave = ActivityWaveView(this); card.addView(wave, LinearLayout.LayoutParams(-1, dp(60)))
        val cardContainer = FrameLayout(this)
        cardContainer.addView(card, FrameLayout.LayoutParams(-1, -1))
        compactToggle = compactButton("☰", "Expand QSO controls") { toggleCompactMode() }.apply {
            backgroundTintList = ColorStateList.valueOf(0xff12374b.toInt())
            elevation = dp(4).toFloat()
        }
        cardContainer.addView(compactToggle, FrameLayout.LayoutParams(dp(46), dp(46), Gravity.TOP or Gravity.END).apply {
            topMargin = dp(6); marginEnd = dp(6)
        })
        compactToggle.bringToFront()
        column.addView(cardContainer)
        metadataStatus = label(metadataFailure ?: "Connecting to BrandMeister", 12f, Color.CYAN)
        column.addView(metadataStatus)
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val network = BrandMeisterConnectivity.check()
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                if (!isDestroyed && metadataFailure == null) {
                    metadataStatus.text = network.message()
                    if (!network.ok) metadataStatus.setTextColor(0xffffb74d.toInt())
                }
            }
        }
        audioStatus = label(""); column.addView(audioStatus)
        metadataStatus.contentDescription = "Connection status. Tap for browser compatibility information"
        metadataStatus.setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("Embedded browser")
                .setMessage("${lastHeard?.settings?.userAgentString ?: "No usable system WebView"}\n\nGoogle Play Services is not required. Audio and callsigns need a working, sufficiently recent embedded WebView.\n\nOn phones without Google services, use your manufacturer's system/browser updates or supported app store. An installed standalone browser is not necessarily a WebView provider. The OS decides which providers are allowed; do not install arbitrary WebView APKs.\n\nOn Android 7, the factory browser may be too old for the current BrandMeister page. Enable/update the supported WebView (or Chrome if it is the provider) and reopen QSO Vision. No external audio player is used.")
                .setPositiveButton("OK", null)
                .setNeutralButton("Device apps") { _, _ ->
                    try { startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)) }
                    catch (_: android.content.ActivityNotFoundException) { Toast.makeText(this, "Open device Settings, Apps, and check your WebView provider", Toast.LENGTH_LONG).show() }
                }.show()
        }
        val historyCard = panel()
        val historyHeader = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        historyHeader.addView(label("RECENT QSO HISTORY", 16f, Color.CYAN), LinearLayout.LayoutParams(0, -2, 1f))
        historyButton = compactButton(historyLimit.toString(), "History records: $historyLimit. Tap to change") { chooseHistorySize() }.apply {
            describeIcon(this, "History records: $historyLimit. Tap to change")
        }
        historyHeader.addView(historyButton)
        historyCard.addView(historyHeader)
        history = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        historyCard.addView(history)
        column.addView(historyCard)
        if (isLandscape()) {
            // Landscape is deliberately a two-pane dashboard: live QSO gets 70%,
            // while the history remains a readable top-to-bottom list.
            val panes = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 1f }
            column.removeAllViews()
            val currentPane = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                selector.gravity = Gravity.CENTER_VERTICAL or Gravity.END
                setPadding(0, 0, dp(6), 0)
                addView(selector, LinearLayout.LayoutParams(-1, dp(56)))
                addView(cardContainer, LinearLayout.LayoutParams(-1, 0, 1f))
                addView(metadataStatus, LinearLayout.LayoutParams(-1, -2))
                addView(audioStatus, LinearLayout.LayoutParams(-1, -2))
            }
            panes.setPadding(dp(6), dp(4), dp(6), dp(4))
            panes.addView(currentPane, LinearLayout.LayoutParams(0, -1, 0.7f).apply { marginEnd = dp(4) })
            val historyPane = ScrollView(this).apply { isFillViewport = true; addView(historyCard) }
            panes.addView(historyPane, LinearLayout.LayoutParams(0, -1, 0.3f).apply { marginStart = dp(4) })
            val landscape = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, 0); setBackgroundColor(0xff06111f.toInt()) }
            landscape.addView(header, LinearLayout.LayoutParams(-1, dp(52)))
            landscape.addView(panes, LinearLayout.LayoutParams(-1, 0, 1f))
            page.addView(landscape, FrameLayout.LayoutParams(-1, -1))
        } else {
            column.removeView(historyCard)
            val historyScroll = ScrollView(this).apply { isFillViewport = true; addView(historyCard) }
            val portraitRoot = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(0xff06111f.toInt()) }
            portraitRoot.addView(column, LinearLayout.LayoutParams(-1, -2))
            portraitRoot.addView(historyScroll, LinearLayout.LayoutParams(-1, 0, 1f))
            page.addView(portraitRoot, FrameLayout.LayoutParams(-1, -1))
        }
        setContentView(page)
        applyCompactMode()
        applySystemBars()
        syncAudio()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    while (true) { render(); delay(1000) }
                }
                launch { while (true) { pollLastHeard(); delay(1000) } }
                launch { HoselinePlaybackService.status.collect { state ->
                    audioStatus.text = if (muted) "Audio muted • callsign monitoring continues" else state
                    updateMuteIcon(state)
                } }
            }
        }
        lastHeard?.loadUrl("https://brandmeister.network/#/lh")
    }

    private fun applySystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (isLandscape()) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    private fun format(pattern: String, time: Long = System.currentTimeMillis()) =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(time))

    private fun render() {
        val active = liveHeard
        headline.text = active?.call ?: "TG ${vm.selected.value}"
        headline.setTextColor(if (active != null) 0xffff8a18.toInt() else Color.WHITE)
        headline.setShadowLayer(if (active != null) dp(5).toFloat() else 0f, 0f, 0f, 0xffff6500.toInt())
        details.text = if (active != null) "CURRENTLY ON AIR\n${active.country} • ${active.duration}" else "Waiting QSO"
        clock.text = format("HH:mm:ss • EEE, dd MMM yyyy")
        wave.active = active != null
        updateBrightness()
        renderHistory()
    }

    private fun renderHistory() {
        val signature = "${vm.selected.value}:$historyLimit:$heardHistory"
        if (signature == renderedHistory) return
        renderedHistory = signature
        history.removeAllViews()
        if (heardHistory.isEmpty()) history.addView(label("Waiting for BrandMeister Last Heard activity for TG ${vm.selected.value}..."))
        heardHistory.take(historyLimit).forEach { e -> history.addView(label("${e.country} ${e.call} • TG ${e.tg}\nAge ${e.age} • Duration ${e.duration}${if (e.active) " • ON AIR" else ""}", 15f, if (e.active) 0xffff6a00.toInt() else Color.WHITE)) }
    }

    private fun pollLastHeard() {
        if (!::metadataStatus.isInitialized) return
        val tg = vm.selected.value
        lastHeard?.evaluateJavascript("$metadataScript($tg)") { raw ->
            if (tg != vm.selected.value || isDestroyed) return@evaluateJavascript
            try {
                val result = JSONObject(raw ?: "{}")
                val sourceStatus = result.optString("status", "Waiting for metadata")
                metadataStatus.text = metadataFailure ?: if (sourceStatus == "Loading BrandMeister" && SystemClock.elapsedRealtime() - metadataStarted > 40_000)
                    "Metadata page not ready • check network / Browser compatibility, then Monitor to retry" else sourceStatus
                val rows = result.optJSONArray("rows") ?: JSONArray()
                val entries = ArrayList<Heard>()
                for (i in 0 until rows.length()) {
                    val r = rows.getJSONObject(i)
                    if (r.optInt("tg") != tg) continue
                    val code = r.optString("country").uppercase(Locale.ROOT)
                    val country = if (code.length == 2 && code.all { it in 'A'..'Z' }) {
                        code.map { String(Character.toChars(0x1F1E6 + it.code - 'A'.code)) }.joinToString("") +
                            " " + Locale("", code).displayCountry
                    } else "🌐"
                    entries.add(Heard(r.optString("call"), country, tg, r.optString("age"), r.optString("duration"), r.optBoolean("active")))
                }
                liveHeard = entries.firstOrNull { it.active }
                // Replace the source snapshot, avoiding duplicates as relative ages change.
                if (entries.isNotEmpty()) {
                    heardHistory.clear()
                    entries.filter { !it.active }.take(100).forEach { heardHistory.addLast(it) }
                }
                android.util.Log.d("QsoMetadata", "TG=$tg rows=${entries.size} active=${liveHeard?.call ?: "idle"}")
                render()
            } catch (e: Exception) {
                liveHeard = null
                metadataStatus.text = "Metadata parse error: ${e.message}"
            }
        }
    }

    private fun syncAudio() {
        updateMuteIcon()
        if (muted) {
            stopService(Intent(this, HoselinePlaybackService::class.java))
            audioStatus.text = "Audio muted • callsign monitoring continues"
        } else {
            ContextCompat.startForegroundService(this, Intent(this, HoselinePlaybackService::class.java)
                .setAction(HoselinePlaybackService.PLAY).putExtra("tg", vm.selected.value))
        }
    }

    private fun chooseScreenMode() {
        MaterialAlertDialogBuilder(this).setTitle("Screen brightness")
            .setSingleChoiceItems(dimLabels, dimTimes.indexOf(dimPolicy.modeSeconds).takeIf { it >= 0 } ?: 0) { dialog, which ->
                dimPolicy.modeSeconds = dimTimes[which]; dimPolicy.reset()
                prefs.edit().putInt("dimSeconds", dimPolicy.modeSeconds).apply()
                describeIcon(screenButton, "Screen dimming • ${dimLabels[which]}")
                updateBrightness(); dialog.dismiss()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun chooseHistorySize() {
        val options = intArrayOf(10, 20, 50, 100)
        PopupMenu(this, historyButton).apply {
            options.forEach { size ->
                menu.add("$size records").setOnMenuItemClickListener {
                    historyLimit = size
                    prefs.edit().putInt("historyLimit", historyLimit).apply()
                    historyButton.text = historyLimit.toString()
                    describeIcon(historyButton, "History records: $historyLimit. Tap to change")
                    renderedHistory = ""
                    renderHistory()
                    true
                }
            }
        }.show()
    }

    private fun toggleCompactMode() {
        compactMode = !compactMode
        prefs.edit().putBoolean("compactMode", compactMode).apply()
        applyCompactMode()
    }

    /** Changes only native visibility; it never reloads the metadata WebView. */
    private fun applyCompactMode() {
        if (!::selector.isInitialized || !::compactToggle.isInitialized) return
        selector.visibility = if (compactMode) View.GONE else View.VISIBLE
        // Keep the trigger visually stable in both states; changing its glyph
        // made it look like it had vanished on some landscape render passes.
        compactToggle.text = "☰"
        describeIcon(compactToggle, if (compactMode) "Expand QSO controls" else "Collapse QSO controls")
        compactToggle.bringToFront()
    }

    private fun rotateScreen() {
        runCatching { requestedOrientation = if (isLandscape()) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            .onFailure { android.util.Log.w("QsoVision", "Unable to rotate screen", it) }
    }

    private fun rememberDisplayProfile() {
        if (!prefs.getBoolean("displayProfileSaved", false)) {
            val metrics = resources.displayMetrics
            prefs.edit().putBoolean("displayProfileSaved", true)
                .putInt("displayWidth", metrics.widthPixels).putInt("displayHeight", metrics.heightPixels)
                .putInt("displayDpi", metrics.densityDpi).apply()
        }
    }

    private fun updateBrightness() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        val brightness = when {
            dimPolicy.dimmed(liveHeard != null) -> 0.05f
            else -> WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
        if (window.attributes.screenBrightness != brightness) window.attributes = window.attributes.apply { screenBrightness = brightness }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) { dimPolicy.touch(); updateBrightness() }
        return super.dispatchTouchEvent(event)
    }


    private fun selectTalkgroup() {
        val tg = input.text.toString().trim().toIntOrNull()
        if (tg == null || tg !in 1..16777215) { input.error = "Enter a talkgroup from 1 to 16777215"; return }
        heardHistory.clear(); liveHeard = null
        if (lastHeard != null) metadataFailure = null
        metadataStarted = SystemClock.elapsedRealtime()
        vm.select(tg)
        prefs.edit().putInt("tg", tg).apply()
        syncAudio()
        lastHeard?.reload()
        metadataStatus.text = metadataFailure ?: "Connecting to BrandMeister • TG $tg"
        render()
    }

    private fun exitApp() {
        MaterialAlertDialogBuilder(this).setTitle("Exit QSO Vision?").setNegativeButton("Cancel", null)
            .setPositiveButton("Exit") { _, _ -> stopService(Intent(this, HoselinePlaybackService::class.java)); finishAndRemoveTask() }.show()
    }
    override fun onStop() {
        wave.active = false
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.apply { screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE }
        super.onStop()
    }
    override fun onStart() { super.onStart(); window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); dimPolicy.reset() }
    override fun onDestroy() { lastHeard?.let { playerHost.removeView(it); it.stopLoading(); it.destroy() }; lastHeard = null; super.onDestroy() }
}
