package com.dinoexpert.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import com.dinoexpert.R
import com.dinoexpert.ui.SoundCue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Main-thread controller for local assets only. Queue callbacks are generation-guarded. */
class GameAudio(context: Context) {
    private val context = context.applicationContext
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    private val manager = context.getSystemService(AudioManager::class.java)
    private val sounds = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build(),
    ).build()
    private val loaded = mutableSetOf<Int>()
    private val soundIds = mutableMapOf<SoundCue, Int>()
    private val effectStreams = ArrayDeque<Int>()
    private var player: MediaPlayer? = null
    private var generation = 0L
    private var released = false
    private val queue = ArrayDeque<String>()
    private val mutableSpeaking = MutableStateFlow(false)
    val speaking = mutableSpeaking.asStateFlow()
    private val mutableError = MutableStateFlow(false)
    val playbackError = mutableError.asStateFlow()
    private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(attributes).setOnAudioFocusChangeListener { change ->
            if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) { stop(); stopEffects() }
        }.build()

    init {
        sounds.setOnLoadCompleteListener { _, id, status -> if (status == 0) loaded += id }
        soundIds[SoundCue.Correct] = sounds.load(context, R.raw.correct, 1)
        soundIds[SoundCue.Retry] = sounds.load(context, R.raw.retry, 1)
        soundIds[SoundCue.Complete] = sounds.load(context, R.raw.complete, 1)
    }

    fun effect(cue: SoundCue) {
        if (released) return
        val id = soundIds[cue] ?: return
        if (id in loaded) {
            val stream = sounds.play(id, .35f, .35f, 1, 0, 1f)
            if (stream != 0) {
                effectStreams.addLast(stream)
                if (effectStreams.size > 2) effectStreams.removeFirst()
            }
        }
    }

    fun stopEffects() {
        if (released) return
        effectStreams.forEach(sounds::stop)
        effectStreams.clear()
    }

    fun speak(clips: List<String>) {
        stop()
        if (released || clips.isEmpty()) return
        mutableError.value = false
        if (manager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mutableError.value = true
            return
        }
        queue.addAll(clips)
        mutableSpeaking.value = true
        playNext(generation)
    }

    private fun playNext(token: Long) {
        if (token != generation || released) return
        player?.release()
        player = null
        if (queue.isEmpty()) {
            mutableSpeaking.value = false
            manager.abandonAudioFocusRequest(focus)
            return
        }
        val path = queue.removeFirst()
        try {
            val current = MediaPlayer()
            player = current
            current.setAudioAttributes(attributes)
            context.assets.openFd(path).use { current.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            current.setOnPreparedListener { if (token == generation && player === current) current.start() }
            current.setOnCompletionListener { if (token == generation && player === current) playNext(token) }
            current.setOnErrorListener { _, _, _ ->
                if (token == generation) { stop(); mutableError.value = true }
                true
            }
            current.prepareAsync()
        } catch (_: Exception) {
            stop()
            mutableError.value = true
        }
    }

    fun stop() {
        generation++
        queue.clear()
        player?.release()
        player = null
        mutableSpeaking.value = false
        manager.abandonAudioFocusRequest(focus)
    }

    fun release() {
        if (released) return
        stop()
        stopEffects()
        released = true
        sounds.release()
    }
}
