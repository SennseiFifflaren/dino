package com.dinoexpert.ui

import android.media.AudioManager
import android.media.ToneGenerator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinoexpert.R
import com.dinoexpert.ui.theme.Forest
import com.dinoexpert.ui.theme.Ink

@Composable
fun DinoExpertApp(viewModel: QuizViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val systemReducedMotion = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    val reduced = state.progress.settings.reducedAnimation || systemReducedMotion
    val tone = remember { runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 25) }.getOrNull() }
    DisposableEffect(tone) { onDispose { tone?.release() } }
    LaunchedEffect(viewModel, tone) {
        viewModel.sounds.collect { cue ->
            runCatching {
                tone?.startTone(when (cue) {
                    SoundCue.Correct -> ToneGenerator.TONE_PROP_ACK
                    SoundCue.Retry -> ToneGenerator.TONE_PROP_BEEP
                    SoundCue.Complete -> ToneGenerator.TONE_PROP_PROMPT
                }, 180)
            }
        }
    }
    BackHandler(state.screen != Screen.Home && state.screen != Screen.Loading) {
        viewModel.dispatch(GameAction.Home)
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
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
                when (state.screen) {
                    Screen.Loading -> LoadingScreen(state.problem == UiProblem.Load) { viewModel.dispatch(GameAction.Reload) }
                    Screen.Home -> HomeScreen(state, reduced) { viewModel.dispatch(GameAction.Play) }
                    Screen.Quiz -> QuizScreen(state, reduced, viewModel::dispatch)
                    Screen.Complete -> LevelCompletionScreen(reduced) { viewModel.dispatch(GameAction.Play) }
                    Screen.Settings -> SettingsScreen(state, viewModel::dispatch)
                }
            }
        }
    }
}

@Composable
private fun PrehistoricScenery() {
    Canvas(Modifier.fillMaxSize()) {
        val ground = Color(0xFFDDEACF).copy(alpha = .5f)
        drawOval(ground, Offset(-size.width * .2f, size.height * .82f), Size(size.width * 1.4f, size.height * .45f))
        val leaf = Color(0xFF97BA91).copy(alpha = .17f)
        repeat(4) { i ->
            drawOval(leaf, Offset(size.width - 40 - i * 22, 35f + i * 50), Size(90f, 35f))
            drawOval(leaf, Offset(-45f + i * 5, size.height * .4f + i * 45), Size(90f, 30f))
        }
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
fun HomeScreen(state: GameUiState, reducedAnimation: Boolean, play: () -> Unit) {
    val session = state.progress.session
    Column(
        Modifier.widthIn(max = 700.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, color = Forest, textAlign = TextAlign.Center)
        Text(stringResource(R.string.subtitle), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        ProfessorFossilCharacter(ProfessorState.Idle, reducedAnimation)
        Dialogue(stringResource(R.string.home_welcome))
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.tertiaryContainer) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.level_one), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.level_description), style = MaterialTheme.typography.bodyMedium)
                if (session != null) {
                    Text(stringResource(R.string.resume_summary, session.questionIndex + 1))
                } else if (1 in state.progress.completedLevels) {
                    Text(stringResource(R.string.completed_home), fontWeight = FontWeight.Bold)
                }
            }
        }
        Button(onClick = play, enabled = !state.saving, modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)) {
            Text(stringResource(when {
                state.progress.session != null -> R.string.continue_game
                1 in state.progress.completedLevels -> R.string.play_again
                else -> R.string.play
            }))
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
fun LevelCompletionScreen(reducedAnimation: Boolean, replay: () -> Unit) {
    Column(
        Modifier.widthIn(max = 700.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(stringResource(R.string.level_complete), style = MaterialTheme.typography.displaySmall, color = Forest, textAlign = TextAlign.Center)
        ProfessorFossilCharacter(ProfessorState.Congratulations, reducedAnimation)
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.tertiaryContainer) {
            Text(stringResource(R.string.badge), Modifier.padding(24.dp), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        }
        Dialogue(stringResource(R.string.completion_message))
        Text(stringResource(R.string.completion_detail), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Button(onClick = replay, modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)) {
            Text(stringResource(R.string.play_again))
        }
    }
}
