package io.github.deckers22.shoppinglist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.deckers22.shoppinglist.AppViewModel
import io.github.deckers22.shoppinglist.data.ShoppingItem
import io.github.deckers22.shoppinglist.quantityLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(vm: AppViewModel) {
    val items by vm.shoppingItems.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ShoppingItem?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping list") },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Remove checked items") },
                            enabled = items.any { it.checked },
                            onClick = {
                                menuOpen = false
                                vm.clearCheckedShoppingItems()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Clear whole list") },
                            enabled = items.isNotEmpty(),
                            onClick = {
                                menuOpen = false
                                confirmClear = true
                            },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        },
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(
                "Your shopping list is empty.\nAdd items with +, or from Recipes and Staples.",
                Modifier.padding(padding),
            )
        } else {
            ReorderableList(
                items = items,
                key = { it.id },
                onReorder = { ordered -> vm.reorderShoppingItems(ordered.map { it.id }) },
                modifier = Modifier.padding(padding),
            ) { item, handle, isDragging ->
                ShoppingRow(
                    item = item,
                    handle = handle,
                    isDragging = isDragging,
                    onToggle = { vm.updateShoppingItem(item.copy(checked = !item.checked)) },
                    onEdit = { editing = item },
                    onDelete = { vm.deleteShoppingItem(item.id) },
                )
            }
        }
    }

    if (showAdd) {
        ItemDialog(
            title = "Add item",
            onDismiss = { showAdd = false },
            onConfirm = { name, amount, unit ->
                vm.addShoppingItem(name, amount, unit)
                showAdd = false
            },
        )
    }
    editing?.let { item ->
        ItemDialog(
            title = "Edit item",
            initialName = item.name,
            initialAmount = item.amount,
            initialUnit = item.unit,
            onDismiss = { editing = null },
            onConfirm = { name, amount, unit ->
                // Copy from the live row: `item` is a snapshot from when the dialog opened,
                // and the row may have been moved since.
                val current = items.firstOrNull { it.id == item.id } ?: item
                vm.updateShoppingItem(current.copy(name = name, amount = amount, unit = unit))
                editing = null
            },
        )
    }
    if (confirmClear) {
        ConfirmDialog(
            title = "Clear shopping list?",
            text = "This removes all ${items.size} items from the list.",
            confirmLabel = "Clear",
            onConfirm = {
                vm.clearShoppingList()
                confirmClear = false
            },
            onDismiss = { confirmClear = false },
        )
    }
}

@Composable
private fun ShoppingRow(
    item: ShoppingItem,
    handle: Modifier,
    isDragging: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val details = listOfNotNull(
        quantityLabel(item.amount, item.unit).ifEmpty { null },
        item.source?.let { "for $it" },
    ).joinToString(" · ")

    Surface(shadowElevation = if (isDragging) 6.dp else 0.dp) {
        ListItem(
            modifier = Modifier.clickable(onClick = onEdit),
            leadingContent = { Checkbox(checked = item.checked, onCheckedChange = { onToggle() }) },
            headlineContent = {
                Text(
                    item.name,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                    color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
            },
            supportingContent = if (details.isEmpty()) null else ({ Text(details) }),
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete ${item.name}")
                    }
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Drag to reorder",
                        modifier = handle.padding(12.dp),
                    )
                }
            },
        )
    }
}
