package com.mmdvm.qsovision.service

import android.app.*
import android.content.*
import android.media.*
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.*
import com.mmdvm.qsovision.MainActivity
import android.webkit.WebView
import kotlinx.coroutines.flow.MutableStateFlow

/** Owns the official browser player independently of the Activity's lifetime. */
class HoselinePlaybackService : Service() {
    companion object {
        const val PLAY = "qso.PLAY"
        const val STOP = "qso.STOP"
        val status = MutableStateFlow("Audio stopped")
        var isRunning = false
            private set
    }
    private var player: WebView? = null
    private var tg = 46020
    private lateinit var session: MediaSession
    private lateinit var audio: AudioManager
    private var request: AudioFocusRequest? = null
    private var wake: PowerManager.WakeLock? = null
    private var resumeOnFocus = false
    private var failed = false
    private val focus = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> if (resumeOnFocus) { resumeOnFocus = false; loadPlayer() }
            AudioManager.AUDIOFOCUS_LOSS -> { resumeOnFocus = false; stopSelf() }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                resumeOnFocus = true; releasePlayer(); update("Audio interrupted", PlaybackState.STATE_PAUSED)
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        isRunning = true
        audio = getSystemService(AudioManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("playback", "Talkgroup audio", NotificationManager.IMPORTANCE_LOW))
        }
        session = MediaSession(this, "QsoVisionAudio").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onStop() { stopSelf() }
                override fun onPause() { stopSelf() }
            })
            isActive = true
        }
        startForeground(91, notification("Connecting audio"))
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP) { stopSelf(); return START_NOT_STICKY }
        val next = intent?.getIntExtra("tg", tg) ?: tg
        if (next !in 1..16777215) { stopSelf(); return START_NOT_STICKY }
        if (player != null && tg == next) return START_NOT_STICKY
        tg = next
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
        val granted = if (Build.VERSION.SDK_INT >= 26) {
            val r = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(attributes)
                .setOnAudioFocusChangeListener(focus).setWillPauseWhenDucked(true).build()
            request = r; audio.requestAudioFocus(r)
        } else {
            @Suppress("DEPRECATION")
            audio.requestAudioFocus(focus, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        if (granted == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) loadPlayer()
        else { failed = true; status.value = "Audio focus unavailable • Monitor to retry"; stopSelf() }
        return START_NOT_STICKY
    }
    @android.annotation.SuppressLint("WakelockTimeout")
    private fun loadPlayer() {
        releasePlayer()
        failed = false
        wake = getSystemService(PowerManager::class.java).newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, "QsoVision:audio").apply { acquire() }
        update("Loading Hoseline • TG $tg", PlaybackState.STATE_BUFFERING)
        val web = WebView(applicationContext).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            loadUrl("https://hose.brandmeister.network/?subscribe=$tg")
        }
        player = web
    }
    // Active MediaSession notifications are exempt from POST_NOTIFICATIONS.
    @android.annotation.SuppressLint("NotificationPermission")
    private fun update(text: String, state: Int) {
        status.value = text
        session.setMetadata(MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE, "TG $tg")
            .putString(MediaMetadata.METADATA_KEY_ARTIST, "QSO Vision • Hoseline").build())
        session.setPlaybackState(PlaybackState.Builder().setActions(PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP)
            .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f).build())
        getSystemService(NotificationManager::class.java).notify(91, notification(text))
    }
    private fun notification(text: String): Notification {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val open = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java), flags)
        val stop = PendingIntent.getService(this, 2, Intent(this, javaClass).setAction(STOP), flags)
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, "playback") else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder.setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("QSO Vision • TG $tg")
            .setContentText(text).setContentIntent(open).setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Stop", stop).build())
            .setStyle(Notification.MediaStyle().setMediaSession(session.sessionToken).setShowActionsInCompactView(0)).build()
    }
    private fun releasePlayer() {
        player?.let { it.stopLoading(); it.destroy() }; player = null
        wake?.let { if (it.isHeld) it.release() }; wake = null
    }
    override fun onDestroy() {
        isRunning = false
        resumeOnFocus = false; releasePlayer()
        if (Build.VERSION.SDK_INT >= 26) request?.let { audio.abandonAudioFocusRequest(it) }
        else { @Suppress("DEPRECATION") audio.abandonAudioFocus(focus) }
        session.release(); if (!failed) status.value = "Audio stopped • Unmute to listen"
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
