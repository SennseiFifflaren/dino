package com.dinoexpert.core

import kotlin.random.Random

/** Pure transitions: no Android, lifecycle, persistence, sound or UI dependencies. */
class QuizEngine(val levels: List<QuizLevel>) {
    init {
        require(levels.isNotEmpty())
        require(levels.map { it.number } == (1..levels.size).toList())
        require(QuestionBank.validate(levels.flatMap { it.questions }, 1..levels.size).isEmpty())
    }

    fun question(session: QuizSession): QuizQuestion =
        levels.first { it.number == session.level }.questions[session.questionIndex]

    fun start(progress: PlayerProgress, level: Int, random: Random = Random.Default): PlayerProgress {
        if (level !in 1..levels.size || level > progress.highestUnlockedLevel) return progress
        val parked = parkCurrent(progress, level) - level
        return progress.copy(session = newSession(level, 0, random), parkedSessions = parked, celebrationLevel = null)
    }

    /** Opening another level preserves unfinished adventures, including in-progress replays. */
    fun openLevel(progress: PlayerProgress, level: Int, random: Random = Random.Default): PlayerProgress {
        if (level !in 1..levels.size || level > progress.highestUnlockedLevel) return progress
        if (progress.session?.level == level) return progress.copy(celebrationLevel = null)
        val session = progress.parkedSessions[level] ?: newSession(level, 0, random)
        return progress.copy(session = session, parkedSessions = parkCurrent(progress, level) - level, celebrationLevel = null)
    }

    fun continueAdventure(progress: PlayerProgress): PlayerProgress = openLevel(
        progress, progress.session?.level ?: progress.parkedSessions.keys.minOrNull() ?: progress.highestUnlockedLevel,
    )

    fun allLevelsCompleted(progress: PlayerProgress): Boolean = levels.all { it.number in progress.completedLevels }

    private fun parkCurrent(progress: PlayerProgress, destination: Int): Map<Int, QuizSession> {
        val session = progress.session ?: return progress.parkedSessions
        return if (session.level == destination) progress.parkedSessions else progress.parkedSessions + (session.level to session)
    }

    fun answer(progress: PlayerProgress, answerId: String): PlayerProgress {
        val session = progress.session ?: return progress
        if (session.answeredCorrectly || answerId !in session.choiceOrder || answerId in session.wrongAnswerIds) {
            return progress
        }
        return progress.copy(session = if (question(session).correctAnswerId == answerId) {
            session.copy(answeredCorrectly = true)
        } else {
            session.copy(wrongAnswerIds = session.wrongAnswerIds + answerId)
        })
    }

    /** Only the explicit Next action advances. Calling it on an unanswered question has no effect. */
    fun next(progress: PlayerProgress, random: Random = Random.Default): PlayerProgress {
        val session = progress.session ?: return progress
        if (!session.answeredCorrectly) return progress
        val level = levels.first { it.number == session.level }
        if (session.questionIndex + 1 < level.questions.size) {
            return progress.copy(session = newSession(session.level, session.questionIndex + 1, random))
        }
        return progress.copy(
            completedLevels = progress.completedLevels + session.level,
            highestUnlockedLevel = maxOf(progress.highestUnlockedLevel, (session.level + 1).coerceAtMost(levels.size)),
            session = null,
            parkedSessions = progress.parkedSessions - session.level,
            celebrationLevel = session.level,
        )
    }

    /** Treat saved state as untrusted. Retain valid settings/completions and discard unsafe sessions. */
    fun restore(saved: PlayerProgress): PlayerProgress {
        if (saved.version != 1) return PlayerProgress()
        val completed = (1..levels.size).takeWhile { it in saved.completedLevels }.toSet()
        val unlocked = (completed.size + 1).coerceAtMost(levels.size)
        fun validSession(s: QuizSession): Boolean {
            val level = levels.firstOrNull { it.number == s.level }
            val q = level?.questions?.getOrNull(s.questionIndex)
            return q != null && s.level <= unlocked && s.questionId == q.id &&
                s.choiceOrder.size == 4 && s.choiceOrder.toSet() == q.choices.map { it.id }.toSet() &&
                s.wrongAnswerIds.all { id -> id in s.choiceOrder && id != q.correctAnswerId }
        }
        val session = saved.session?.takeIf(::validSession)
        val parked = saved.parkedSessions.filter { (level, s) -> level == s.level && level != session?.level && validSession(s) }
        return saved.copy(
            completedLevels = completed, highestUnlockedLevel = unlocked, session = session,
            parkedSessions = parked, celebrationLevel = saved.celebrationLevel?.takeIf { it in completed },
        )
    }

    private fun newSession(level: Int, index: Int, random: Random): QuizSession {
        val q = levels.first { it.number == level }.questions[index]
        return QuizSession(level, index, q.id, q.choices.map { it.id }.shuffled(random))
    }
}
