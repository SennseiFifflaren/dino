package com.dinoexpert.ui

import android.view.accessibility.AccessibilityManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.dinoexpert.R
import com.dinoexpert.audio.GameAudio
import com.dinoexpert.core.NarrationPlan
import com.dinoexpert.ui.theme.Forest
import com.dinoexpert.ui.theme.Ink

@Composable
fun DinoExpertApp(viewModel: QuizViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val systemReducedMotion = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    val reduced = state.progress.settings.reducedAnimation || systemReducedMotion
    val audio = remember(context) { GameAudio(context) }
    val speaking by audio.speaking.collectAsStateWithLifecycle()
    val audioError by audio.playbackError.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateFlow.collectAsState()
    val accessibility = remember(context) { context.getSystemService(AccessibilityManager::class.java) }
    var talkBack by remember { mutableStateOf(accessibility.isTouchExplorationEnabled) }
    DisposableEffect(accessibility) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { talkBack = it }
        accessibility.addTouchExplorationStateChangeListener(listener)
        onDispose { accessibility.removeTouchExplorationStateChangeListener(listener) }
    }
    DisposableEffect(audio) { onDispose { audio.release() } }
    LaunchedEffect(viewModel, audio, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.sounds.collect(audio::effect)
        }
    }
    LaunchedEffect(state.screen, state.progress.settings.soundEnabled, lifecycleState) {
        if (!state.progress.settings.soundEnabled || lifecycleState != Lifecycle.State.RESUMED ||
            state.screen !in listOf(Screen.Quiz, Screen.Complete, Screen.Final)) audio.stopEffects()
    }
    val session = state.progress.session
    LaunchedEffect(state.screen, session?.questionId, session?.answeredCorrectly, session?.wrongAnswerIds?.size,
        state.progress.settings.voiceEnabled, lifecycleState, talkBack) {
        audio.stop()
        if (!state.progress.settings.voiceEnabled || talkBack || lifecycleState != Lifecycle.State.RESUMED) return@LaunchedEffect
        val clips = when (state.screen) {
            Screen.Quiz -> session?.let { NarrationPlan.current(state.engine!!.question(it), it) }.orEmpty()
            Screen.Complete -> listOf(NarrationPlan.shared("complete"))
            Screen.Final -> listOf(NarrationPlan.shared("final"))
            else -> emptyList()
        }
        audio.speak(clips)
    }
    BackHandler(state.screen != Screen.Home && state.screen != Screen.Loading) {
        viewModel.dispatch(GameAction.Back)
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PrehistoricScenery()
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.screen != Screen.Home && state.screen != Screen.Loading) {
                    TextButton(onClick = { viewModel.dispatch(GameAction.Home) }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.home))
                    }
                } else {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium, color = Forest,
                        modifier = Modifier.padding(horizontal = 12.dp))
                }
                Spacer(Modifier.weight(1f))
                if (state.screen != Screen.Loading && state.screen != Screen.Settings) {
                    TextButton(onClick = { viewModel.dispatch(GameAction.Settings) }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.settings))
                    }
                }
            }
            if (state.problem == UiProblem.Save) {
                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.save_error), modifier = Modifier.padding(16.dp))
                }
            }
            if (audioError && state.screen == Screen.Quiz && state.progress.settings.voiceEnabled) {
                Text(stringResource(R.string.voice_error), Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.bodyMedium)
            }
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
                when (state.screen) {
                    Screen.Loading -> LoadingScreen(state.problem == UiProblem.Load) { viewModel.dispatch(GameAction.Reload) }
                    Screen.Home -> HomeScreen(state, reduced, viewModel::dispatch)
                    Screen.Levels -> LevelSelectionScreen(state, viewModel::dispatch)
                    Screen.Quiz -> QuizScreen(state, reduced, viewModel::dispatch, speaking,
                        onReadQuestion = {
                            session?.let {
                                val q = state.engine!!.question(it)
                                audio.speak(if (it.answeredCorrectly) NarrationPlan.current(q, it) else NarrationPlan.question(q, it))
                            }
                        }, onStopVoice = audio::stop)
                    Screen.Complete -> LevelCompletionScreen(state, reduced, viewModel::dispatch, speaking)
                    Screen.Final -> FinalAchievementScreen(reduced, { viewModel.dispatch(GameAction.Levels) }, speaking)
                    Screen.Settings -> SettingsScreen(state, viewModel::dispatch)
                }
                if (state.screen == Screen.Complete || state.screen == Screen.Final) {
                    CelebrationConfetti(reduced, Modifier.matchParentSize())
                }
            }
        }
    }
}

@Composable
private fun PrehistoricScenery() {
    Canvas(Modifier.fillMaxSize()) {
        val ridge = Path().apply {
            moveTo(0f, size.height * .72f)
            lineTo(size.width * .18f, size.height * .53f)
            lineTo(size.width * .38f, size.height * .69f)
            lineTo(size.width * .7f, size.height * .49f)
            lineTo(size.width, size.height * .66f)
            lineTo(size.width, size.height); lineTo(0f, size.height); close()
        }
        drawPath(ridge, Color(0xFFD8E7D5).copy(alpha = .45f))
        val ground = Color(0xFFDDEACF).copy(alpha = .5f)
        drawOval(ground, Offset(-size.width * .2f, size.height * .82f), Size(size.width * 1.4f, size.height * .45f))
        drawOval(Color(0xFFF1E0B5).copy(alpha = .5f), Offset(size.width * .3f, size.height * .83f), Size(size.width * .7f, size.height * .4f))
        val leaf = Color(0xFF97BA91).copy(alpha = .17f)
        repeat(4) { i ->
            drawOval(leaf, Offset(size.width - 40 - i * 22, 35f + i * 50), Size(90f, 35f))
            drawOval(leaf, Offset(-45f + i * 5, size.height * .4f + i * 45), Size(90f, 30f))
        }
        val fossil = Offset(size.width * .13f, size.height * .9f)
        val spiral = Path().apply {
            repeat(65) { i ->
                val angle = i * .19f
                val radius = i * .8f
                val point = fossil + Offset(kotlin.math.cos(angle), kotlin.math.sin(angle)) * radius
                if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
        }
        drawPath(spiral, Color(0xFFB59B67).copy(alpha = .25f), style = Stroke(3.dp.toPx()))
    }
}

@Composable
private fun LoadingScreen(error: Boolean, retry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        if (!error) CircularProgressIndicator()
        Spacer(Modifier.height(20.dp))
        Text(stringResource(if (error) R.string.load_error else R.string.loading), textAlign = TextAlign.Center)
        if (error) Button(onClick = retry, modifier = Modifier.padding(top = 20.dp).heightIn(min = 56.dp)) {
            Text(stringResource(R.string.try_again))
        }
    }
}

@Composable
fun HomeScreen(state: GameUiState, reducedAnimation: Boolean, dispatch: (GameAction) -> Unit) {
    val session = state.progress.session ?: state.progress.parkedSessions.toSortedMap().values.firstOrNull()
    Column(
        Modifier.widthIn(max = 700.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, color = Forest, textAlign = TextAlign.Center)
        Text(stringResource(R.string.subtitle), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        ProfessorFossilCharacter(ProfessorState.Idle, reducedAnimation)
        Dialogue(stringResource(R.string.home_welcome))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            (1..5).forEach { FossilBadge(it, it in state.progress.completedLevels, Modifier.size(48.dp)) }
        }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.tertiaryContainer) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.journey_description), style = MaterialTheme.typography.titleLarge)
                Text(levelTitle(session?.level ?: state.progress.highestUnlockedLevel), style = MaterialTheme.typography.bodyLarge)
                if (session != null) {
                    Text(stringResource(R.string.resume_summary, session.level, session.questionIndex + 1))
                }
                Text(stringResource(R.string.completed_home, state.progress.completedLevels.size), fontWeight = FontWeight.Bold)
            }
        }
        Button(onClick = { dispatch(GameAction.Play) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)) {
            Text(stringResource(when {
                session != null -> R.string.continue_game
                state.engine?.allLevelsCompleted(state.progress) == true -> R.string.play_again
                else -> R.string.play
            }))
        }
        OutlinedButton(onClick = { dispatch(GameAction.Levels) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text(stringResource(R.string.choose_level))
        }
        if (state.engine?.allLevelsCompleted(state.progress) == true) {
            TextButton(onClick = { dispatch(GameAction.Achievement) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.achievement))
            }
        }
        Text(stringResource(R.string.offline_note), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

@Composable
fun Dialogue(text: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.professor_name), style = MaterialTheme.typography.labelLarge, color = Forest)
            Text(text, style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
    }
}

@Composable
fun LevelCompletionScreen(state: GameUiState, reducedAnimation: Boolean, dispatch: (GameAction) -> Unit, speaking: Boolean = false) {
    val level = state.progress.celebrationLevel ?: state.progress.completedLevels.maxOrNull() ?: 1
    Column(
        Modifier.widthIn(max = 700.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(stringResource(R.string.level_complete), style = MaterialTheme.typography.displaySmall, color = Forest, textAlign = TextAlign.Center)
        ProfessorFossilCharacter(ProfessorState.Congratulations, reducedAnimation, speaking = speaking)
        FossilBadge(level, true, Modifier.size(110.dp))
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.tertiaryContainer) {
            Text(stringResource(R.string.badge, levelTitle(level)), Modifier.padding(24.dp), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        }
        Dialogue(stringResource(R.string.completion_message))
        Text(stringResource(R.string.completion_detail), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (level < 5) {
            Text(stringResource(R.string.new_unlock, levelTitle(level + 1)), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Button(onClick = { dispatch(GameAction.OpenLevel(level + 1)) }, enabled = !state.saving,
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)) { Text(stringResource(R.string.next_adventure)) }
        }
        OutlinedButton(onClick = { dispatch(GameAction.Replay(level)) }, enabled = !state.saving, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text(stringResource(R.string.play_again))
        }
        TextButton(onClick = { dispatch(GameAction.Levels) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.levels)) }
    }
}
