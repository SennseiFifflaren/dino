package com.dinoexpert.data

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dinoexpert.core.PlayerProgress
import com.dinoexpert.core.ProgressCodec
import com.dinoexpert.core.QuizEngine
import kotlinx.coroutines.flow.first

private val Context.progressStore by preferencesDataStore(
    name = "dino_progress",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

class ProgressRepository(context: Context) {
    private val store = context.applicationContext.progressStore
    private val key = stringPreferencesKey("player_progress_v1")

    suspend fun load(engine: QuizEngine): PlayerProgress = ProgressCodec.decode(store.data.first()[key], engine)
    suspend fun save(progress: PlayerProgress) {
        store.edit { it[key] = ProgressCodec.encode(progress) }
    }
}
