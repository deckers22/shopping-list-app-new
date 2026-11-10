package io.github.deckers22.shoppinglist.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.deckers22.shoppinglist.AppViewModel
import io.github.deckers22.shoppinglist.data.Ingredient
import io.github.deckers22.shoppinglist.data.Recipe
import io.github.deckers22.shoppinglist.quantityLabel
import io.github.deckers22.shoppinglist.scaleAmount

@Composable
fun RecipeDetailScreen(vm: AppViewModel, recipeId: Long, onBack: () -> Unit) {
    val recipe by remember(recipeId) { vm.observeRecipe(recipeId) }.collectAsState(initial = null)
    val ingredients by remember(recipeId) { vm.observeIngredients(recipeId) }
        .collectAsState(initial = emptyList())

    // Null while loading, and after the recipe has been deleted.
    recipe?.let { RecipeDetail(vm, it, ingredients, onBack) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetail(
    vm: AppViewModel,
    recipe: Recipe,
    ingredients: List<Ingredient>,
    onBack: () -> Unit,
) {
    // The number of people shown on screen. Changing it rescales what is displayed and
    // what goes on the shopping list; the stored recipe stays as it was written.
    var servings by rememberSaveable(recipe.id, recipe.servings) { mutableStateOf(recipe.servings) }
    var editRecipe by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var addIngredient by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Ingredient?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { editRecipe = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit recipe")
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete recipe")
                    }
                },
            )
        },
        floatingActionButton = {
            if (ingredients.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { vm.addRecipeToShoppingList(recipe, ingredients, servings) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    text = { Text("Add to shopping list") },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = FabClearance),
        ) {
            item(key = "header") {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ServingsStepper(servings = servings, onChange = { servings = it })
                    if (servings != recipe.servings) {
                        Text(
                            "Amounts scaled from the recipe for ${peopleLabel(recipe.servings)}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (recipe.notes.isNotBlank()) {
                        Text(
                            recipe.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                    Text(
                        "Ingredients",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                HorizontalDivider()
            }
            items(ingredients, key = { it.id }) { ingredient ->
                val scaled = scaleAmount(ingredient.amount, recipe.servings, servings)
                ListItem(
                    modifier = Modifier.clickable { editing = ingredient },
                    headlineContent = { Text(ingredient.name) },
                    trailingContent = {
                        Text(
                            quantityLabel(scaled, ingredient.unit),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                )
            }
            item(key = "add") {
                TextButton(
                    onClick = { addIngredient = true },
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add ingredient")
                }
            }
        }
    }

    // Ingredient amounts are always entered for the recipe's own number of people.
    val amountLabel = "Amount for ${recipe.servings}"
    if (addIngredient) {
        ItemDialog(
            title = "Add ingredient",
            amountLabel = amountLabel,
            onDismiss = { addIngredient = false },
            onConfirm = { name, amount, unit ->
                vm.addIngredient(recipe.id, name, amount, unit)
                addIngredient = false
            },
        )
    }
    editing?.let { ingredient ->
        ItemDialog(
            title = "Edit ingredient",
            amountLabel = amountLabel,
            initialName = ingredient.name,
            initialAmount = ingredient.amount,
            initialUnit = ingredient.unit,
            onDismiss = { editing = null },
            onConfirm = { name, amount, unit ->
                vm.updateIngredient(ingredient.copy(name = name, amount = amount, unit = unit))
                editing = null
            },
            onDelete = {
                vm.deleteIngredient(ingredient.id)
                editing = null
            },
        )
    }
    if (editRecipe) {
        RecipeDialog(
            title = "Edit recipe",
            initialName = recipe.name,
            initialServings = recipe.servings,
            initialNotes = recipe.notes,
            onDismiss = { editRecipe = false },
            onConfirm = { name, newServings, notes ->
                vm.updateRecipe(recipe.copy(name = name, servings = newServings, notes = notes))
                editRecipe = false
            },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete recipe?",
            text = "\"${recipe.name}\" and its ingredients will be deleted.",
            confirmLabel = "Delete",
            onConfirm = {
                confirmDelete = false
                vm.deleteRecipe(recipe.id)
                onBack()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun ServingsStepper(servings: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("For", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        FilledTonalIconButton(onClick = { onChange(servings - 1) }, enabled = servings > 1) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            peopleLabel(servings),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(96.dp),
        )
        FilledTonalIconButton(onClick = { onChange(servings + 1) }, enabled = servings < 999) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}
