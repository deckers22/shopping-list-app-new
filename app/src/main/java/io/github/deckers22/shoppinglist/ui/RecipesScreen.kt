package io.github.deckers22.shoppinglist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.deckers22.shoppinglist.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(vm: AppViewModel, onOpen: (Long) -> Unit) {
    val recipes by vm.recipes.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Recipes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "New recipe")
            }
        },
    ) { padding ->
        if (recipes.isEmpty()) {
            EmptyState("No recipes yet.\nTap + to add your first one.", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = FabClearance),
            ) {
                items(recipes, key = { it.id }) { recipe ->
                    ListItem(
                        modifier = Modifier.clickable { onOpen(recipe.id) },
                        headlineContent = { Text(recipe.name) },
                        supportingContent = { Text(peopleLabel(recipe.servings)) },
                    )
                }
            }
        }
    }

    if (showAdd) {
        RecipeDialog(
            title = "New recipe",
            onDismiss = { showAdd = false },
            onConfirm = { name, servings, notes ->
                vm.addRecipe(name, servings, notes, onCreated = onOpen)
                showAdd = false
            },
        )
    }
}

fun peopleLabel(servings: Int): String = if (servings == 1) "1 person" else "$servings people"

@Composable
fun RecipeDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, servings: Int, notes: String) -> Unit,
    initialName: String = "",
    initialServings: Int = 4,
    initialNotes: String = "",
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var servings by rememberSaveable { mutableStateOf(initialServings.toString()) }
    var notes by rememberSaveable { mutableStateOf(initialNotes) }
    val servingsValue = servings.trim().toIntOrNull()?.takeIf { it in 1..999 }

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
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it },
                    label = { Text("Ingredients are for … people") },
                    singleLine = true,
                    isError = servingsValue == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / preparation (optional)") },
                    minLines = 3,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && servingsValue != null,
                onClick = { onConfirm(name.trim(), servingsValue ?: initialServings, notes.trim()) },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
