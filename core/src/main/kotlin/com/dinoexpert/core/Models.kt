package com.dinoexpert.core

import kotlinx.serialization.Serializable

@Serializable
data class AnswerChoice(val id: String, val text: String)

@Serializable
data class QuizQuestion(
    val id: String,
    val level: Int,
    val difficulty: String,
    val text: String,
    val choices: List<AnswerChoice>,
    val correctAnswerId: String,
    val explanation: String,
    val hint: String? = null,
    val topic: String,
    val source: String,
    val referenceUrl: String,
)

data class QuizLevel(val number: Int, val questions: List<QuizQuestion>)

@Serializable
data class QuizSession(
    val level: Int,
    val questionIndex: Int,
    val questionId: String,
    val choiceOrder: List<String>,
    val wrongAnswerIds: Set<String> = emptySet(),
    val answeredCorrectly: Boolean = false,
)

@Serializable
data class GameSettings(
    val soundEnabled: Boolean = true,
    val reducedAnimation: Boolean = false,
    val voiceEnabled: Boolean = true,
)

@Serializable
data class PlayerProgress(
    val version: Int = 1,
    val highestUnlockedLevel: Int = 1,
    val completedLevels: Set<Int> = emptySet(),
    val session: QuizSession? = null,
    val settings: GameSettings = GameSettings(),
    val parkedSessions: Map<Int, QuizSession> = emptyMap(),
    val celebrationLevel: Int? = null,
)
