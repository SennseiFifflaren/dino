package com.dinoexpert.core

import java.io.File
import java.security.MessageDigest
import kotlin.random.Random
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class NarrationPlanTest {
    private val assets = File("../app/src/main/assets")
    private val levels = QuestionBank.parse(File(assets, "questions_en.json").readText())
    private val manifest = Json.parseToJsonElement(File(assets, "audio/voice/manifest.json").readText()).jsonObject
    private val clips = manifest.getValue("clips").jsonObject
    private fun session(q: QuizQuestion, index: Int = 0) = QuizSession(q.level, index, q.id, q.choices.map { it.id }.shuffled(Random(19)))

    @Test fun everyQuestionReadsExactlyFourChoicesInDisplayedOrderWithLetters() {
        levels.flatMap { it.questions }.forEach { q ->
            val s = session(q)
            val plan = NarrationPlan.question(q, s)
            assertEquals(9, plan.size)
            assertEquals(NarrationPlan.path(q.id, "question"), plan.first())
            s.choiceOrder.forEachIndexed { index, id ->
                assertEquals(NarrationPlan.shared("option_${'a' + index}"), plan[1 + index * 2])
                assertEquals(NarrationPlan.path(q.id, id), plan[2 + index * 2])
            }
            assertFalse(plan.any { "explanation" in it })
        }
    }

    @Test fun incorrectFeedbackNeverRevealsTheCorrectAnswerOrExplanation() {
        levels.flatMap { it.questions }.forEach { q ->
            val s = session(q).copy(wrongAnswerIds = setOf(q.choices.first { it.id != q.correctAnswerId }.id))
            val plan = NarrationPlan.current(q, s)
            assertEquals(NarrationPlan.shared("retry"), plan.first())
            assertFalse(plan.contains(NarrationPlan.path(q.id, q.correctAnswerId)))
            assertFalse(plan.any { "explanation" in it })
        }
    }

    @Test fun correctFeedbackReadsOnlyTheCorrectChoiceAndExplanation() {
        levels.flatMap { it.questions }.forEach { q ->
            val s = session(q).copy(answeredCorrectly = true)
            assertEquals(listOf(NarrationPlan.shared("correct"), NarrationPlan.path(q.id, q.correctAnswerId),
                NarrationPlan.path(q.id, "explanation")), NarrationPlan.current(q, s))
            assertEquals(0, s.questionIndex) // Narration cannot advance gameplay.
        }
    }

    @Test fun allBundledVoiceFilesHaveValidHeadersHashesAndMatchingSourceText() {
        assertEquals(360, clips.size)
        clips.forEach { (path, record) ->
            val file = File(assets, "audio/voice/$path")
            val bytes = file.readBytes()
            assertEquals("OggS", bytes.take(4).toByteArray().toString(Charsets.US_ASCII))
            val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            assertEquals(record.jsonObject.getValue("sha256").jsonPrimitive.content, hash)
            assertTrue(record.jsonObject.getValue("duration").jsonPrimitive.double > .25)
        }
        levels.flatMap { it.questions }.forEach { q ->
            fun text(name: String) = clips.getValue("${q.id}/$name.ogg").jsonObject.getValue("text").jsonPrimitive.content
            assertEquals(q.text, text("question"))
            assertEquals(q.explanation, text("explanation"))
            assertEquals(q.hint, text("hint"))
            q.choices.forEach { assertEquals(it.text, text(it.id)) }
            NarrationPlan.question(q, session(q)).forEach { path -> assertTrue(clips.containsKey(path.removePrefix("audio/voice/"))) }
        }
    }

    @Test fun narrationDoesNotChangeOrderAcrossRetriesAndRejectsAMismatchedQuestion() {
        val q = levels.first().questions.first()
        val s = session(q)
        assertEquals(NarrationPlan.question(q, s), NarrationPlan.question(q, s.copy(wrongAnswerIds = setOf(s.choiceOrder[0]))))
        assertThrows(IllegalArgumentException::class.java) { NarrationPlan.question(q, s.copy(questionId = "not-this-question")) }
    }
}
