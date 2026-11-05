package com.dnavarro.sidequest.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.R
import com.dnavarro.sidequest.model.TextRules
import com.dnavarro.sidequest.ui.theme.Parchment

/**
 * Plain text that turns into a field when tapped, with a ✓ button to save. Only letters,
 * digits and spaces can be typed, capped at [maxLength].
 */
@Composable
fun EditableText(
    value: String,
    onSave: (String) -> Unit,
    maxLength: Int,
    required: Boolean,
    placeholder: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    if (!editing) {
        val empty = value.isEmpty()
        Text(
            text = if (empty) placeholder else value,
            style = if (empty) textStyle.copy(color = Parchment.InkFaded, fontStyle = FontStyle.Italic) else textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
                .clickable {
                    draft = TextFieldValue(value, TextRange(value.length))
                    errorText = null
                    editing = true
                }
                .padding(vertical = 2.dp),
        )
        return
    }

    val save: () -> Unit = {
        val cleaned = TextRules.cleanName(draft.text, maxLength)
        if (required && cleaned.isEmpty()) {
            errorText = TextRules.REQUIRED_ERROR
        } else {
            onSave(cleaned)
            errorText = null
            editing = false
        }
    }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    BackHandler {
        editing = false
        errorText = null
    }

    Column(modifier) {
        OutlinedTextField(
            value = draft,
            onValueChange = { next ->
                val filtered = TextRules.filterName(next.text, maxLength)
                draft = if (filtered == next.text) next else TextFieldValue(filtered, TextRange(filtered.length))
                errorText = null
            },
            singleLine = true,
            textStyle = textStyle,
            isError = errorText != null,
            placeholder = { Text(placeholder, style = textStyle, color = Parchment.InkFaded) },
            trailingIcon = {
                IconButton(onClick = save) {
                    Icon(painterResource(R.drawable.ic_check), contentDescription = "Save", tint = Parchment.Wood)
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { save() }),
            colors = parchmentFieldColors(),
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
        )
        errorText?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
