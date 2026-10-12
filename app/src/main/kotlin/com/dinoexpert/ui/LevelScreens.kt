package com.dinoexpert.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dinoexpert.R
import com.dinoexpert.ui.theme.Forest
import com.dinoexpert.ui.theme.Ink
import androidx.compose.ui.graphics.Color

@Composable
fun levelTitle(level: Int): String = stringResource(when (level) {
    1 -> R.string.level_one
    2 -> R.string.level_two
    3 -> R.string.level_three
    4 -> R.string.level_four
    else -> R.string.level_five
})

@Composable
fun LevelSelectionScreen(state: GameUiState, dispatch: (GameAction) -> Unit) {
    BoxWithConstraints(Modifier.widthIn(max = 1000.dp).fillMaxWidth()) {
        val columns = if (maxWidth >= 600.dp) 2 else 1
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(stringResource(R.string.choose_level), style = MaterialTheme.typography.displaySmall, color = Forest, modifier = Modifier.semantics { heading() })
            Text(stringResource(R.string.journey_description), style = MaterialTheme.typography.bodyLarge)
            (1..5).toList().chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { level -> LevelCard(level, state, Modifier.weight(1f), dispatch) }
                    if (columns == 2 && row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LevelCard(level: Int, state: GameUiState, modifier: Modifier, dispatch: (GameAction) -> Unit) {
    val unlocked = level <= state.progress.highestUnlockedLevel
    val session = state.progress.session?.takeIf { it.level == level } ?: state.progress.parkedSessions[level]
    val completed = level in state.progress.completedLevels
    Card(
        onClick = { dispatch(GameAction.OpenLevel(level)) }, enabled = unlocked && !state.saving,
        modifier = modifier.heightIn(min = 170.dp), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = if (completed) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = Color(0xFFE6EAE2), disabledContentColor = Ink),
    ) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FossilBadge(level, completed, Modifier.size(56.dp))
                Text(stringResource(R.string.level_number, level), style = MaterialTheme.typography.labelLarge)
            }
            Text(levelTitle(level), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(when (level) {
                1 -> R.string.level_topic_1
                2 -> R.string.level_topic_2
                3 -> R.string.level_topic_3
                4 -> R.string.level_topic_4
                else -> R.string.level_topic_5
            }), style = MaterialTheme.typography.bodyMedium)
            Text(when {
                !unlocked -> stringResource(R.string.level_locked, level - 1)
                session != null -> stringResource(R.string.level_resume, session.questionIndex + 1)
                completed -> stringResource(R.string.level_replay)
                else -> stringResource(R.string.level_unlocked)
            }, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun FinalAchievementScreen(reducedAnimation: Boolean, levels: () -> Unit, speaking: Boolean = false) {
    Column(
        Modifier.widthIn(max = 750.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(R.string.final_title), style = MaterialTheme.typography.displaySmall, color = Forest, textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() })
        ProfessorFossilCharacter(ProfessorState.Congratulations, reducedAnimation, speaking = speaking)
        Dialogue(stringResource(R.string.final_message))
        (1..5).forEach { level ->
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FossilBadge(level, true, Modifier.size(52.dp))
                    Text(stringResource(R.string.badge, levelTitle(level)), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                }
            }
        }
        Text(stringResource(R.string.final_detail), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Button(onClick = levels, modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)) { Text(stringResource(R.string.choose_level)) }
    }
}
