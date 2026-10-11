package com.dinoexpert.core

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ProgressCodec {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    fun encode(progress: PlayerProgress): String = json.encodeToString(progress)
    fun decode(contents: String?, engine: QuizEngine): PlayerProgress {
        if (contents == null) return PlayerProgress()
        return runCatching { engine.restore(json.decodeFromString<PlayerProgress>(contents)) }
            .getOrElse { PlayerProgress() }
    }
}
