package com.dinoexpert

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.dinoexpert.core.*
import com.dinoexpert.data.ProgressRepository
import com.dinoexpert.data.QuestionRepository
import com.dinoexpert.ui.*
import com.dinoexpert.ui.theme.DinoTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class QuizUiTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val engine by lazy { QuizEngine(QuestionRepository(context).load()) }

    @Test fun retryCorrectExplanationAndExplicitNext() {
        val state = mutableStateOf(GameUiState(Screen.Quiz, engine.start(PlayerProgress(), 1), engine))
        compose.setContent {
            DinoTheme {
                QuizScreen(state.value, true, dispatch = { action ->
                    val p = state.value.progress
                    state.value = state.value.copy(progress = when (action) {
                        is GameAction.Answer -> engine.answer(p, action.id)
                        GameAction.Next -> engine.next(p)
                        else -> p
                    })
                })
            }
        }
        val session = state.value.progress.session!!
        val question = engine.question(session)
        fun label(id: String) = "${'A' + session.choiceOrder.indexOf(id)}. ${question.choices.first { it.id == id }.text}"
        session.choiceOrder.forEach { compose.onNodeWithText(label(it)).assertExists() }
        compose.onNodeWithText("Next Question").performScrollTo().assertIsNotEnabled()
        val wrong = session.choiceOrder.first { it != question.correctAnswerId }
        compose.onNodeWithText(label(wrong)).performScrollTo().performClick()
        compose.onNodeWithText(label(wrong)).assertIsNotEnabled()
        compose.onNodeWithText(label(question.correctAnswerId)).assertIsEnabled()
        compose.onNodeWithText("Next Question").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText(label(question.correctAnswerId)).performScrollTo().performClick()
        compose.onNodeWithText(question.explanation).performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, state.value.progress.session!!.questionIndex) }
        compose.onNodeWithText("Next Question").performScrollTo().assertIsEnabled().performClick()
        compose.onNodeWithText("Question 2 of 10").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, state.value.progress.session!!.questionIndex) }
    }

    @Test fun wideWindowUsesTwoAnswerColumns() {
        val p = engine.start(PlayerProgress(), 1)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(.5f, 1f)) {
                DinoTheme { Box(Modifier.width(800.dp)) { QuizScreen(GameUiState(Screen.Quiz, p, engine), true, dispatch = {}) } }
            }
        }
        val session = p.session!!
        val q = engine.question(session)
        fun node(index: Int) = compose.onNodeWithText("${'A' + index}. ${q.choices.first { it.id == session.choiceOrder[index] }.text}")
        val first = node(0).fetchSemanticsNode().boundsInRoot
        val second = node(1).fetchSemanticsNode().boundsInRoot
        assertEquals(first.top, second.top, 1f)
        assertTrue(second.left > first.left)
        session.choiceOrder.indices.forEach { node(it).assertExists() }
    }

    @Test fun datastoreRetainsRetriesAndCorrectFeedbackAcrossRepositoryRecreation() = runBlocking {
        val repository = ProgressRepository(context)
        val before = repository.load(engine)
        try {
            var p = engine.start(PlayerProgress(), 1).copy(settings = GameSettings(false, true))
            val q = engine.question(p.session!!)
            p = engine.answer(p, q.choices.first { it.id != q.correctAnswerId }.id)
            repository.save(p)
            assertEquals(p, ProgressRepository(context).load(engine))
            p = engine.answer(p, q.correctAnswerId)
            repository.save(p)
            val restored = ProgressRepository(context).load(engine)
            assertEquals(p, restored)
            assertEquals(1, engine.next(restored).session!!.questionIndex)
        } finally {
            repository.save(before)
        }
    }
}
