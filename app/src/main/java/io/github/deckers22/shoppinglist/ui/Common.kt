package io.github.deckers22.shoppinglist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.deckers22.shoppinglist.formatAmount
import io.github.deckers22.shoppinglist.parseAmount
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/** Space at the end of a list so the last row is not hidden behind the floating button. */
val FabClearance = 96.dp

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Add/edit dialog for anything that has a name, an optional amount and a unit. */
@Composable
fun ItemDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double?, unit: String) -> Unit,
    initialName: String = "",
    initialAmount: Double? = null,
    initialUnit: String = "",
    amountLabel: String = "Amount",
    onDelete: (() -> Unit)? = null,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var amount by rememberSaveable { mutableStateOf(formatAmount(initialAmount)) }
    var unit by rememberSaveable { mutableStateOf(initialUnit) }
    val amountInvalid = amount.isNotBlank() && parseAmount(amount) == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(amountLabel) },
                        singleLine = true,
                        isError = amountInvalid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(3f),
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(2f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && !amountInvalid,
                onClick = { onConfirm(name.trim(), parseAmount(amount), unit.trim()) },
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * A list whose rows can be dragged into a new order. Put the `handle` modifier on the
 * part of the row that should start a drag. [onReorder] is called once, when the row
 * is dropped, with the complete list in its new order.
 */
@Composable
fun <T> ReorderableList(
    items: List<T>,
    key: (T) -> Long,
    onReorder: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    row: @Composable (item: T, handle: Modifier, isDragging: Boolean) -> Unit,
) {
    // Rows move in this local copy while dragging; the database is written on drop.
    var local by remember { mutableStateOf(items) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(items) {
        if (!dragging) local = items
    }

    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        local = local.toMutableList().apply { add(to.index, removeAt(from.index)) }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = FabClearance),
    ) {
        items(local, key = { key(it) }) { item ->
            ReorderableItem(reorderState, key = key(item)) { isDragging ->
                val handle = Modifier.draggableHandle(
                    onDragStarted = { dragging = true },
                    onDragStopped = {
                        dragging = false
                        onReorder(local)
                    },
                )
                row(item, handle, isDragging)
            }
        }
    }
}
