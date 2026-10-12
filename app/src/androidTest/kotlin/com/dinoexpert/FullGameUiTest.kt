package com.dinoexpert

import android.media.MediaPlayer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.dinoexpert.core.*
import com.dinoexpert.data.QuestionRepository
import com.dinoexpert.ui.*
import com.dinoexpert.ui.theme.DinoTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class FullGameUiTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val engine by lazy { QuizEngine(QuestionRepository(context).load()) }

    @Test fun levelSelectionDisablesLockedLevelsAndAllowsOnlyUnlockedChoice() {
        var selected: Int? = null
        compose.setContent {
            DinoTheme { LevelSelectionScreen(GameUiState(Screen.Levels, PlayerProgress(), engine)) {
                if (it is GameAction.OpenLevel) selected = it.level
            } }
        }
        compose.onNodeWithText("Dino Beginner").assertIsEnabled()
        compose.onNodeWithText("Dino Explorer").performScrollTo().assertIsNotEnabled().performClick()
        compose.runOnIdle { assertNull(selected) }
        compose.onNodeWithText("Dino Beginner").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, selected) }
    }

    @Test fun voiceToggleIsIndependentOfSoundEffectsAndReducedAnimation() {
        val state = mutableStateOf(GameUiState(Screen.Settings, PlayerProgress(settings = GameSettings(false, true)), engine))
        compose.setContent {
            DinoTheme { SettingsScreen(state.value) {
                if (it is GameAction.Voice) state.value = state.value.copy(progress = state.value.progress.copy(
                    settings = state.value.progress.settings.copy(voiceEnabled = it.enabled)))
            } }
        }
        compose.onNodeWithText("Professor’s voice").performScrollTo().performClick()
        compose.runOnIdle {
            assertFalse(state.value.progress.settings.voiceEnabled)
            assertFalse(state.value.progress.settings.soundEnabled)
            assertTrue(state.value.progress.settings.reducedAnimation)
        }
    }

    @Test fun androidDecoderOpensBundledVoiceClipWithoutTextToSpeechOrInternet() {
        val media = MediaPlayer()
        try {
            context.assets.openFd(NarrationPlan.shared("correct")).use {
                media.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
            media.prepare()
            assertTrue(media.duration > 300)
        } finally { media.release() }
    }
}
