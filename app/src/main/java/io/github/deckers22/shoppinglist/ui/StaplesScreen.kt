package io.github.deckers22.shoppinglist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.deckers22.shoppinglist.AppViewModel
import io.github.deckers22.shoppinglist.data.Staple
import io.github.deckers22.shoppinglist.quantityLabel

/** Standard groceries: tap one to put it on the shopping list. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaplesScreen(vm: AppViewModel) {
    val staples by vm.staples.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Staple?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staples") },
                actions = {
                    TextButton(
                        enabled = staples.isNotEmpty(),
                        onClick = { vm.addStaplesToShoppingList(staples) },
                    ) { Text("Add all to list") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "New staple")
            }
        },
    ) { padding ->
        if (staples.isEmpty()) {
            EmptyState(
                "No staples yet.\nAdd the groceries you buy regularly, then tap one to put it on your shopping list.",
                Modifier.padding(padding),
            )
        } else {
            ReorderableList(
                items = staples,
                key = { it.id },
                onReorder = { ordered -> vm.reorderStaples(ordered.map { it.id }) },
                modifier = Modifier.padding(padding),
            ) { staple, handle, isDragging ->
                val quantity = quantityLabel(staple.amount, staple.unit)
                Surface(shadowElevation = if (isDragging) 6.dp else 0.dp) {
                    ListItem(
                        modifier = Modifier.clickable(
                            onClickLabel = "Add to shopping list",
                            onClick = { vm.addStaplesToShoppingList(listOf(staple)) },
                        ),
                        leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                        headlineContent = { Text(staple.name) },
                        supportingContent = if (quantity.isEmpty()) null else ({ Text(quantity) }),
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { editing = staple }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit ${staple.name}")
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
        }
    }

    if (showAdd) {
        ItemDialog(
            title = "New staple",
            onDismiss = { showAdd = false },
            onConfirm = { name, amount, unit ->
                vm.addStaple(name, amount, unit)
                showAdd = false
            },
        )
    }
    editing?.let { staple ->
        ItemDialog(
            title = "Edit staple",
            initialName = staple.name,
            initialAmount = staple.amount,
            initialUnit = staple.unit,
            onDismiss = { editing = null },
            onConfirm = { name, amount, unit ->
                val current = staples.firstOrNull { it.id == staple.id } ?: staple
                vm.updateStaple(current.copy(name = name, amount = amount, unit = unit))
                editing = null
            },
            onDelete = {
                vm.deleteStaple(staple.id)
                editing = null
            },
        )
    }
}
