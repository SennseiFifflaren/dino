package com.dinoexpert.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dinoexpert.R
import com.dinoexpert.core.QuizQuestion
import com.dinoexpert.core.QuizSession
import com.dinoexpert.ui.theme.CorrectGreen
import com.dinoexpert.ui.theme.Forest
import com.dinoexpert.ui.theme.Ink
import com.dinoexpert.ui.theme.RetryRust

@Composable
fun QuizScreen(state: GameUiState, reducedAnimation: Boolean, dispatch: (GameAction) -> Unit) {
    val engine = state.engine ?: return
    val session = state.progress.session ?: return
    val question = engine.question(session)
    val total = engine.levels.first { it.number == session.level }.questions.size
    val characterState = when {
        session.answeredCorrectly -> ProfessorState.Celebrating
        session.wrongAnswerIds.isNotEmpty() -> ProfessorState.Thinking
        else -> ProfessorState.Asking
    }
    val answered = session.questionIndex + if (session.answeredCorrectly) 1 else 0
    val progressLabel = stringResource(R.string.answered_progress, answered, total)
    BoxWithConstraints(Modifier.widthIn(max = 1100.dp).fillMaxWidth()) {
        val wide = maxWidth >= 600.dp
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(if (wide) 28.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.level_label), style = MaterialTheme.typography.labelLarge, color = Forest)
            Text(stringResource(R.string.question_progress, session.questionIndex + 1, total), style = MaterialTheme.typography.titleLarge)
            LinearProgressIndicator(
                progress = { answered.toFloat() / total },
                modifier = Modifier.fillMaxWidth().height(8.dp).semantics { contentDescription = progressLabel },
            )
            if (wide) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    ProfessorFossilCharacter(characterState, reducedAnimation, Modifier.width(270.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Dialogue(stringResource(R.string.asking))
                        QuestionText(question)
                    }
                }
            } else {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfessorFossilCharacter(characterState, reducedAnimation)
                    if (session.wrongAnswerIds.isEmpty() && !session.answeredCorrectly) Dialogue(stringResource(R.string.asking), Modifier.fillMaxWidth())
                    QuestionText(question)
                }
            }
            if (wide) {
                session.choiceOrder.chunked(2).forEachIndexed { rowIndex, row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEachIndexed { index, id ->
                            AnswerButton(question, session, id, rowIndex * 2 + index, state.saving, Modifier.weight(1f), dispatch)
                        }
                    }
                }
            } else {
                session.choiceOrder.forEachIndexed { index, id ->
                    AnswerButton(question, session, id, index, state.saving, Modifier.fillMaxWidth(), dispatch)
                }
            }
            if (session.answeredCorrectly) {
                Surface(shape = MaterialTheme.shapes.large, color = Color(0xFFDDF1DE), modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(if (session.questionIndex % 2 == 0) R.string.correct else R.string.correct_alternate),
                            style = MaterialTheme.typography.titleLarge, color = CorrectGreen)
                        Text(stringResource(R.string.explanation_title), style = MaterialTheme.typography.labelLarge, color = CorrectGreen)
                        Text(question.explanation, style = MaterialTheme.typography.bodyLarge, color = Ink)
                        Text(stringResource(R.string.source_label, question.source), style = MaterialTheme.typography.bodyMedium, color = Ink)
                    }
                }
            } else if (session.wrongAnswerIds.isNotEmpty()) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.retry), style = MaterialTheme.typography.titleLarge)
                        question.hint?.let { Text(stringResource(R.string.hint, it), style = MaterialTheme.typography.bodyLarge) }
                    }
                }
            }
            Button(
                onClick = { dispatch(GameAction.Next) },
                enabled = session.answeredCorrectly && !state.saving,
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            ) {
                Text(stringResource(if (session.questionIndex + 1 == total) R.string.finish_level else R.string.next_question))
            }
            if (!session.answeredCorrectly) Text(stringResource(R.string.next_locked), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun QuestionText(question: QuizQuestion) {
    Surface(shape = MaterialTheme.shapes.large, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Text(question.text, modifier = Modifier.padding(20.dp).semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun AnswerButton(
    question: QuizQuestion, session: QuizSession, id: String, index: Int, saving: Boolean,
    modifier: Modifier, dispatch: (GameAction) -> Unit,
) {
    val choice = question.choices.first { it.id == id }
    val correct = session.answeredCorrectly && id == question.correctAnswerId
    val wrong = id in session.wrongAnswerIds
    val background = when { correct -> Color(0xFFDDF1DE); wrong -> Color(0xFFFFE9D4); else -> MaterialTheme.colorScheme.surface }
    val foreground = when { correct -> CorrectGreen; wrong -> RetryRust; else -> Ink }
    OutlinedButton(
        onClick = { dispatch(GameAction.Answer(id)) },
        enabled = !session.answeredCorrectly && !wrong && !saving,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(if (correct || wrong) 2.dp else 1.dp, if (correct || wrong) foreground else MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = background, contentColor = foreground,
            disabledContainerColor = background, disabledContentColor = foreground),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier.heightIn(min = 76.dp),
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.answer_label, ('A' + index).toString(), choice.text), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            if (correct || wrong) Text(stringResource(if (correct) R.string.answer_correct else R.string.answer_wrong), style = MaterialTheme.typography.labelLarge)
        }
    }
}
