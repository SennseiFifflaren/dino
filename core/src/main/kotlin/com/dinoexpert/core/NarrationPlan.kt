package com.dinoexpert.core

/** Stable choice IDs keep spoken answers aligned with the shuffled A–D buttons. */
object NarrationPlan {
    fun question(question: QuizQuestion, session: QuizSession): List<String> {
        require(question.id == session.questionId)
        require(session.choiceOrder.size == 4 && session.choiceOrder.toSet() == question.choices.map { it.id }.toSet())
        return buildList {
            add(path(question.id, "question"))
            session.choiceOrder.forEachIndexed { index, id ->
                add(shared("option_${'a' + index}"))
                add(path(question.id, id))
            }
        }
    }

    fun current(question: QuizQuestion, session: QuizSession): List<String> = when {
        session.answeredCorrectly -> listOf(
            shared(if (session.questionIndex % 2 == 0) "correct" else "correct_alternate"),
            path(question.id, question.correctAnswerId), path(question.id, "explanation"),
        )
        session.wrongAnswerIds.isNotEmpty() -> buildList {
            add(shared("retry"))
            if (!question.hint.isNullOrBlank()) add(path(question.id, "hint"))
        }
        else -> question(question, session)
    }

    fun path(questionId: String, clip: String) = "audio/voice/$questionId/$clip.ogg"
    fun shared(name: String) = "audio/voice/shared/$name.ogg"
}
