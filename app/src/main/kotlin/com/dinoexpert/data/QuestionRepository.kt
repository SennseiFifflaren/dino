package com.dinoexpert.data

import android.content.Context
import com.dinoexpert.core.QuestionBank
import com.dinoexpert.core.QuizLevel

class QuestionRepository(private val context: Context) {
    // One bundled English bank in Phase 1. Future localized banks retain stable IDs.
    fun load(): List<QuizLevel> = context.assets.open("questions_en.json")
        .bufferedReader().use { QuestionBank.parse(it.readText(), 1..1) }
}
