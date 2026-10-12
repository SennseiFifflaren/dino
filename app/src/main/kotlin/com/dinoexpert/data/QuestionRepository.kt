package com.dinoexpert.data

import android.content.Context
import com.dinoexpert.core.QuestionBank
import com.dinoexpert.core.QuizLevel

class QuestionRepository(private val context: Context) {
    // Future localized banks retain these stable IDs for save compatibility.
    fun load(): List<QuizLevel> = context.assets.open("questions_en.json")
        .bufferedReader().use { QuestionBank.parse(it.readText(), 1..5) }
}
