package com.dinoexpert.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dinoexpert.R
import com.dinoexpert.ui.theme.Forest

@Composable
fun SettingsScreen(state: GameUiState, dispatch: (GameAction) -> Unit) {
    var showCredits by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    Column(
        Modifier.widthIn(max = 700.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.displaySmall, color = Forest)
        Text(stringResource(R.string.settings_description), style = MaterialTheme.typography.bodyLarge)
        SettingToggle(stringResource(R.string.sound), stringResource(R.string.sound_description),
            state.progress.settings.soundEnabled, !state.saving) { dispatch(GameAction.Sound(it)) }
        SettingToggle(stringResource(R.string.voice), stringResource(R.string.voice_description),
            state.progress.settings.voiceEnabled, !state.saving) { dispatch(GameAction.Voice(it)) }
        SettingToggle(stringResource(R.string.reduce_animation), stringResource(R.string.reduce_description),
            state.progress.settings.reducedAnimation, !state.saving) { dispatch(GameAction.ReducedAnimation(it)) }
        Text(stringResource(R.string.offline_note), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.talkback_voice), style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(onClick = { showCredits = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(stringResource(R.string.audio_credits)) }
        Button(onClick = { dispatch(GameAction.Back) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(stringResource(R.string.back)) }
    }
    if (showCredits) {
        val credits = remember(context) { context.assets.open("audio/CREDITS.txt").bufferedReader().use { it.readText() } }
        AlertDialog(
            onDismissRequest = { showCredits = false }, title = { Text(stringResource(R.string.audio_credits)) },
            text = { Text(credits, Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { showCredits = false }) { Text(stringResource(R.string.close)) } },
        )
    }
}

@Composable
private fun SettingToggle(title: String, description: String, checked: Boolean, enabled: Boolean, change: (Boolean) -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(
            Modifier.fillMaxWidth().toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = change).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    }
}
