package com.dinoexpert.core

import java.io.File
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class QuizEngineTest {
    private val bankText = File("../app/src/main/assets/questions_en.json").readText()
    private val levels = QuestionBank.parse(bankText)
    private val engine = QuizEngine(levels)
    private fun start() = engine.start(PlayerProgress(), 1, Random(42))
    private fun correct(p: PlayerProgress) = engine.answer(p, engine.question(p.session!!).correctAnswerId)

    @Test fun bundledBankHasTenValidOriginalQuestions() {
        val questions = levels.flatMap { it.questions }
        assertEquals(10, questions.size)
        assertTrue(QuestionBank.validate(questions, 1..1).isEmpty())
        assertTrue(questions.all { it.choices.size == 4 })
    }

    @Test fun wrongAnswerRetainsQuestionAndOrderAndNextIsBlocked() {
        val p = start()
        val session = p.session!!
        val wrong = session.choiceOrder.first { it != engine.question(session).correctAnswerId }
        val retry = engine.answer(p, wrong)
        assertEquals(session.questionId, retry.session!!.questionId)
        assertEquals(session.choiceOrder, retry.session!!.choiceOrder)
        assertEquals(setOf(wrong), retry.session!!.wrongAnswerIds)
        assertEquals(0, retry.session!!.questionIndex)
        assertEquals(retry, engine.answer(retry, wrong))
        assertEquals(retry, engine.next(retry))
    }

    @Test fun allThreeWrongChoicesStillAllowCorrectAnswer() {
        var p = start()
        val q = engine.question(p.session!!)
        q.choices.filter { it.id != q.correctAnswerId }.forEach { p = engine.answer(p, it.id) }
        assertEquals(3, p.session!!.wrongAnswerIds.size)
        assertFalse(p.session!!.answeredCorrectly)
        p = engine.answer(p, q.correctAnswerId)
        assertTrue(p.session!!.answeredCorrectly)
        assertEquals(0, p.session!!.questionIndex)
    }

    @Test fun correctAnswerHasExplanationAndNeverAdvancesOrScoresTwice() {
        val p = correct(start())
        val q = engine.question(p.session!!)
        assertTrue(q.explanation.isNotBlank())
        assertEquals(0, p.session!!.questionIndex)
        q.choices.forEach { assertEquals(p, engine.answer(p, it.id)) }
        val next = engine.next(p, Random(7))
        assertEquals(1, next.session!!.questionIndex)
        assertFalse(next.session!!.answeredCorrectly)
        assertEquals(next, engine.next(next))
    }

    @Test fun onlyTenCorrectQuestionsAndExplicitNextCompleteLevel() {
        var p = start()
        repeat(10) { index ->
            assertEquals(index, p.session!!.questionIndex)
            assertTrue(p.completedLevels.isEmpty())
            assertEquals(p, engine.next(p))
            p = correct(p)
            assertTrue(p.completedLevels.isEmpty())
            p = engine.next(p, Random(index))
        }
        assertNull(p.session)
        assertEquals(setOf(1), p.completedLevels)
        assertEquals(p, engine.next(p))
        assertEquals(p, engine.answer(p, "anything"))
    }

    @Test fun lockedAndNonexistentLevelsCannotStart() {
        val p = PlayerProgress()
        assertEquals(p, engine.start(p, 0))
        assertEquals(p, engine.start(p, 2))
        val level2 = QuizLevel(2, levels[0].questions.map { q -> q.copy(id = "two-${q.id}", level = 2) })
        val twoLevelEngine = QuizEngine(levels + level2)
        assertEquals(p, twoLevelEngine.start(p, 2))
        var completed = twoLevelEngine.start(p, 1)
        repeat(10) {
            completed = twoLevelEngine.answer(completed, twoLevelEngine.question(completed.session!!).correctAnswerId)
            completed = twoLevelEngine.next(completed)
        }
        assertEquals(2, completed.highestUnlockedLevel)
        assertEquals(2, twoLevelEngine.start(completed, 2).session!!.level)
    }

    @Test fun replayKeepsCompletionAndStartsFresh() {
        var p = start()
        repeat(10) { p = engine.next(correct(p)) }
        val replay = engine.start(p, 1)
        assertEquals(setOf(1), replay.completedLevels)
        assertEquals(0, replay.session!!.questionIndex)
        assertTrue(replay.session!!.wrongAnswerIds.isEmpty())
        assertFalse(replay.session!!.answeredCorrectly)
    }

    @Test fun savesRestoreWrongChoicesOrderSettingsAndCorrectFeedback() {
        var p = start().copy(settings = GameSettings(false, true))
        val wrong = p.session!!.choiceOrder.first { it != engine.question(p.session!!).correctAnswerId }
        p = engine.answer(p, wrong)
        assertEquals(p, ProgressCodec.decode(ProgressCodec.encode(p), engine))
        p = correct(p)
        val restored = ProgressCodec.decode(ProgressCodec.encode(p), engine)
        assertEquals(p, restored)
        assertEquals(p, engine.answer(restored, engine.question(p.session!!).correctAnswerId))
        assertEquals(1, engine.next(restored).session!!.questionIndex)
    }

    @Test fun completedSaveRestoresForReplay() {
        var p = start()
        repeat(10) { p = engine.next(correct(p)) }
        assertEquals(p, ProgressCodec.decode(ProgressCodec.encode(p), engine))
    }

    @Test fun invalidAnswersAreIgnored() {
        val p = start()
        assertEquals(p, engine.answer(p, "not-a-choice"))
        assertEquals(PlayerProgress(), engine.answer(PlayerProgress(), "anything"))
    }

    @Test fun missingCorruptOrNewerSaveFallsBackSafely() {
        assertEquals(PlayerProgress(), ProgressCodec.decode(null, engine))
        assertEquals(PlayerProgress(), ProgressCodec.decode("broken json", engine))
        assertEquals(PlayerProgress(), ProgressCodec.decode("{\"version\":99}", engine))
    }

    @Test fun unsafeSessionIsDiscardedWithoutDiscardingSettings() {
        val p = start().copy(settings = GameSettings(false, true))
        val s = p.session!!
        val invalid = listOf(
            s.copy(level = 5), s.copy(questionIndex = 99), s.copy(questionId = "missing"),
            s.copy(choiceOrder = listOf("unknown", "unknown", "unknown", "unknown")),
            s.copy(wrongAnswerIds = setOf(engine.question(s).correctAnswerId)),
        )
        invalid.forEach {
            val restored = engine.restore(p.copy(session = it, highestUnlockedLevel = 99))
            assertNull(restored.session)
            assertEquals(1, restored.highestUnlockedLevel)
            assertEquals(p.settings, restored.settings)
        }
    }

    @Test fun validatorsRejectMalformedContent() {
        val questions = levels[0].questions
        val q = questions.first()
        val invalid = q.copy(
            id = "", level = 9, difficulty = "", text = "", choices = listOf(q.choices[0], q.choices[0]),
            correctAnswerId = "missing", explanation = "", topic = "", source = "", referenceUrl = "http://invalid",
        )
        assertTrue(QuestionBank.validate(listOf(invalid), 1..1).size >= 10)
        assertTrue(QuestionBank.validate(questions + q, 1..1).any { "unique" in it })
        assertTrue(QuestionBank.validate(questions.dropLast(1), 1..1).isNotEmpty())
    }

    @Test fun shufflingCanChangeInitialOrderButNeverOnRetry() {
        val orders = (1..20).map { engine.start(PlayerProgress(), 1, Random(it)).session!!.choiceOrder }.toSet()
        assertTrue(orders.size > 1)
        assertTrue(orders.all { it.toSet() == levels[0].questions[0].choices.map { c -> c.id }.toSet() })
    }
}
