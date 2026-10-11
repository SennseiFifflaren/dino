package com.dinoexpert

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.dinoexpert.core.*
import com.dinoexpert.data.ProgressRepository
import com.dinoexpert.data.QuestionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class ActivityRestoreTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun activityRecreationAndRelaunchKeepCorrectFeedback() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engine = QuizEngine(QuestionRepository(context).load())
        val repository = ProgressRepository(context)
        val before = runBlocking { repository.load(engine) }
        val p = engine.start(PlayerProgress(settings = GameSettings(false, true)), 1)
        val q = engine.question(p.session!!)
        val correctLabel = "${'A' + p.session!!.choiceOrder.indexOf(q.correctAnswerId)}. ${q.choices.first { it.id == q.correctAnswerId }.text}"
        try {
            runBlocking { repository.save(p) }
            ActivityScenario.launch(MainActivity::class.java).use { activity ->
                awaitText("Continue")
                compose.onNodeWithText("Continue").performScrollTo().performClick()
                awaitText(correctLabel)
                compose.onNodeWithText(correctLabel).performScrollTo().performClick()
                awaitText(q.explanation)
                activity.recreate()
                awaitText(q.explanation)
                compose.onNodeWithText(q.explanation).performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("Question 1 of 10").assertExists()
            }
            ActivityScenario.launch(MainActivity::class.java).use {
                awaitText("Continue")
                compose.onNodeWithText("Continue").performScrollTo().performClick()
                awaitText(q.explanation)
                compose.onNodeWithText(q.explanation).performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("Next Question").performScrollTo().performClick()
                awaitText("Question 2 of 10")
            }
        } finally {
            runBlocking { repository.save(before) }
        }
    }

    private fun awaitText(text: String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
}
