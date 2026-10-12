package com.dinoexpert.core

import kotlinx.serialization.json.Json

object QuestionBank {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(contents: String, expectedLevels: IntRange = 1..5): List<QuizLevel> {
        val questions = json.decodeFromString<List<QuizQuestion>>(contents)
        val errors = validate(questions, expectedLevels)
        require(errors.isEmpty()) { errors.joinToString("\n") }
        return questions.groupBy { it.level }.toSortedMap().map { (number, items) ->
            QuizLevel(number, items)
        }
    }

    fun validate(questions: List<QuizQuestion>, expectedLevels: IntRange): List<String> = buildList {
        if (questions.size != expectedLevels.count() * 10) add("Expected ten questions per level")
        if (questions.map { it.id }.toSet().size != questions.size) add("Question IDs must be unique")
        expectedLevels.forEach { level ->
            if (questions.count { it.level == level } != 10) add("Level $level needs ten questions")
        }
        questions.forEach { q ->
            fun invalid(condition: Boolean, message: String) {
                if (condition) add("${q.id}: $message")
            }
            invalid(q.id.isBlank(), "ID is empty")
            invalid(q.level !in expectedLevels, "Invalid level")
            invalid(q.difficulty.isBlank(), "Difficulty is empty")
            invalid(q.text.isBlank(), "Question is empty")
            invalid(q.choices.size != 4, "Exactly four choices required")
            invalid(q.choices.any { it.id.isBlank() || it.text.isBlank() }, "Empty choice")
            invalid(q.choices.map { it.id }.toSet().size != 4, "Choice IDs must be distinct")
            invalid(q.choices.map { it.text.trim().lowercase() }.toSet().size != 4, "Choice texts must be distinct")
            invalid(q.choices.count { it.id == q.correctAnswerId } != 1, "Exactly one correct choice required")
            invalid(q.explanation.isBlank(), "Explanation is empty")
            invalid(q.topic.isBlank() || q.source.isBlank(), "Attribution and topic required")
            invalid(!q.referenceUrl.matches(Regex("https://[^/\\s]+/.*")), "HTTPS reference URL required")
        }
    }
}
