package com.dinoexpert.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dinoexpert.core.PlayerProgress
import com.dinoexpert.core.QuizEngine
import com.dinoexpert.data.ProgressRepository
import com.dinoexpert.data.QuestionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen { Loading, Home, Quiz, Complete, Settings }
enum class UiProblem { Load, Save }
enum class SoundCue { Correct, Retry, Complete }

data class GameUiState(
    val screen: Screen = Screen.Loading,
    val progress: PlayerProgress = PlayerProgress(),
    val engine: QuizEngine? = null,
    val saving: Boolean = false,
    val problem: UiProblem? = null,
)

sealed interface GameAction {
    data object Play : GameAction
    data class Answer(val id: String) : GameAction
    data object Next : GameAction
    data object Home : GameAction
    data object Settings : GameAction
    data class Sound(val enabled: Boolean) : GameAction
    data class ReducedAnimation(val enabled: Boolean) : GameAction
    data object Reload : GameAction
}

/** All user actions are processed in order. Saves finish before state is displayed. */
class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProgressRepository(application)
    private val mutableState = MutableStateFlow(GameUiState())
    val state = mutableState.asStateFlow()
    private val mutableSounds = MutableSharedFlow<SoundCue>(extraBufferCapacity = 8)
    val sounds = mutableSounds.asSharedFlow()
    private val actions = Channel<GameAction>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            load()
            for (action in actions) process(action)
        }
    }

    fun dispatch(action: GameAction) { actions.trySend(action) }

    private suspend fun load() {
        mutableState.value = GameUiState()
        try {
            val engine = withContext(Dispatchers.IO) { QuizEngine(QuestionRepository(getApplication()).load()) }
            mutableState.value = GameUiState(Screen.Home, repository.load(engine), engine)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            mutableState.value = GameUiState(problem = UiProblem.Load)
        }
    }

    private suspend fun process(action: GameAction) {
        if (action == GameAction.Reload) { load(); return }
        val old = mutableState.value
        val engine = old.engine ?: return
        var screen = old.screen
        var cue: SoundCue? = null
        val next = when (action) {
            GameAction.Play -> {
                screen = Screen.Quiz
                if (old.progress.session != null) old.progress else engine.start(old.progress, 1)
            }
            is GameAction.Answer -> {
                if (old.screen != Screen.Quiz) return
                engine.answer(old.progress, action.id).also { p ->
                    if (p != old.progress) cue = if (p.session!!.answeredCorrectly) SoundCue.Correct else SoundCue.Retry
                }
            }
            GameAction.Next -> {
                if (old.screen != Screen.Quiz) return
                engine.next(old.progress).also { p ->
                    if (p != old.progress && p.session == null) { screen = Screen.Complete; cue = SoundCue.Complete }
                }
            }
            GameAction.Home -> { screen = Screen.Home; old.progress }
            GameAction.Settings -> { screen = Screen.Settings; old.progress }
            is GameAction.Sound -> old.progress.copy(settings = old.progress.settings.copy(soundEnabled = action.enabled))
            is GameAction.ReducedAnimation -> old.progress.copy(settings = old.progress.settings.copy(reducedAnimation = action.enabled))
            GameAction.Reload -> return
        }
        if (next != old.progress) {
            mutableState.value = old.copy(saving = true, problem = null)
            try {
                repository.save(next)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableState.value = old.copy(problem = UiProblem.Save)
                return
            }
        }
        mutableState.value = old.copy(screen = screen, progress = next, saving = false, problem = null)
        if (cue != null && next.settings.soundEnabled) mutableSounds.tryEmit(cue)
    }
}
