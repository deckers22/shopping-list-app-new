package io.github.deckers22.shoppinglist

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import io.github.deckers22.shoppinglist.data.AppDatabase
import io.github.deckers22.shoppinglist.data.Backup
import io.github.deckers22.shoppinglist.data.Ingredient
import io.github.deckers22.shoppinglist.data.Recipe
import io.github.deckers22.shoppinglist.data.ShoppingItem
import io.github.deckers22.shoppinglist.data.Staple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val dao = db.dao()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    val shoppingItems: StateFlow<List<ShoppingItem>> = dao.observeShoppingItems().asState()
    val recipes: StateFlow<List<Recipe>> = dao.observeRecipes().asState()
    val staples: StateFlow<List<Staple>> = dao.observeStaples().asState()

    /** One-off texts for the snackbar. */
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages

    private fun <T> Flow<List<T>>.asState(): StateFlow<List<T>> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Shopping list

    fun addShoppingItem(name: String, amount: Double?, unit: String) = viewModelScope.launch {
        appendToShoppingList(listOf(ShoppingItem(name = name, amount = amount, unit = unit)))
    }

    fun updateShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        dao.updateShoppingItem(item)
    }

    fun deleteShoppingItem(id: Long) = viewModelScope.launch { dao.deleteShoppingItem(id) }

    fun clearCheckedShoppingItems() = viewModelScope.launch { dao.deleteCheckedShoppingItems() }

    fun clearShoppingList() = viewModelScope.launch { dao.deleteAllShoppingItems() }

    fun reorderShoppingItems(orderedIds: List<Long>) = viewModelScope.launch {
        db.withTransaction {
            orderedIds.forEachIndexed { index, id -> dao.setShoppingPosition(id, index) }
        }
    }

    private suspend fun appendToShoppingList(items: List<ShoppingItem>) {
        if (items.isEmpty()) return
        db.withTransaction {
            val start = dao.nextShoppingPosition()
            dao.insertShoppingItems(
                items.mapIndexed { index, item -> item.copy(id = 0, position = start + index) },
            )
        }
    }

    // Recipes

    fun observeRecipe(id: Long): Flow<Recipe?> = dao.observeRecipe(id)

    fun observeIngredients(recipeId: Long): Flow<List<Ingredient>> = dao.observeIngredients(recipeId)

    fun addRecipe(name: String, servings: Int, notes: String, onCreated: (Long) -> Unit) =
        viewModelScope.launch {
            onCreated(dao.insertRecipe(Recipe(name = name, servings = servings, notes = notes)))
        }

    fun updateRecipe(recipe: Recipe) = viewModelScope.launch { dao.updateRecipe(recipe) }

    fun deleteRecipe(id: Long) = viewModelScope.launch { dao.deleteRecipe(id) }

    fun addIngredient(recipeId: Long, name: String, amount: Double?, unit: String) =
        viewModelScope.launch {
            db.withTransaction {
                val ingredient = Ingredient(
                    recipeId = recipeId,
                    name = name,
                    amount = amount,
                    unit = unit,
                    position = dao.nextIngredientPosition(recipeId),
                )
                dao.insertIngredients(listOf(ingredient))
            }
        }

    fun updateIngredient(ingredient: Ingredient) = viewModelScope.launch {
        dao.updateIngredient(ingredient)
    }

    fun deleteIngredient(id: Long) = viewModelScope.launch { dao.deleteIngredient(id) }

    /** Puts every ingredient on the shopping list, scaled to [servings] people. */
    fun addRecipeToShoppingList(recipe: Recipe, ingredients: List<Ingredient>, servings: Int) =
        viewModelScope.launch {
            appendToShoppingList(
                ingredients.map {
                    ShoppingItem(
                        name = it.name,
                        amount = scaleAmount(it.amount, recipe.servings, servings),
                        unit = it.unit,
                        source = recipe.name,
                    )
                },
            )
            _messages.tryEmit(
                if (ingredients.size == 1) "1 ingredient added to shopping list"
                else "${ingredients.size} ingredients added to shopping list",
            )
        }

    // Staples

    fun addStaple(name: String, amount: Double?, unit: String) = viewModelScope.launch {
        db.withTransaction {
            val staple = Staple(
                name = name,
                amount = amount,
                unit = unit,
                position = dao.nextStaplePosition(),
            )
            dao.insertStaples(listOf(staple))
        }
    }

    fun updateStaple(staple: Staple) = viewModelScope.launch { dao.updateStaple(staple) }

    fun deleteStaple(id: Long) = viewModelScope.launch { dao.deleteStaple(id) }

    fun reorderStaples(orderedIds: List<Long>) = viewModelScope.launch {
        db.withTransaction {
            orderedIds.forEachIndexed { index, id -> dao.setStaplePosition(id, index) }
        }
    }

    fun addStaplesToShoppingList(staples: List<Staple>) = viewModelScope.launch {
        appendToShoppingList(
            staples.map { ShoppingItem(name = it.name, amount = it.amount, unit = it.unit) },
        )
        _messages.tryEmit(
            if (staples.size == 1) "${staples[0].name} added to shopping list"
            else "${staples.size} items added to shopping list",
        )
    }

    // Backup

    fun exportBackup(uri: Uri) = viewModelScope.launch {
        val result = runCatching {
            val backup = db.withTransaction {
                Backup(
                    exportedAt = LocalDateTime.now().toString(),
                    shoppingItems = dao.getShoppingItems(),
                    recipes = dao.getRecipes(),
                    ingredients = dao.getIngredients(),
                    staples = dao.getStaples(),
                )
            }
            withContext(Dispatchers.IO) {
                val stream = getApplication<Application>().contentResolver.openOutputStream(uri, "wt")
                    ?: error("Cannot open file")
                stream.use { it.write(json.encodeToString(Backup.serializer(), backup).toByteArray()) }
            }
        }
        _messages.tryEmit(if (result.isSuccess) "Backup saved" else "Could not save backup")
    }

    /** Replaces all data in the app with the contents of the backup file. */
    fun importBackup(uri: Uri) = viewModelScope.launch {
        val result = runCatching {
            val backup = withContext(Dispatchers.IO) {
                val stream = getApplication<Application>().contentResolver.openInputStream(uri)
                    ?: error("Cannot open file")
                val text = stream.use { it.readBytes().decodeToString() }
                json.decodeFromString(Backup.serializer(), text)
            }
            // Parsing happens before anything is deleted, and the transaction rolls back
            // on failure, so a bad file leaves the existing data untouched.
            db.withTransaction {
                dao.deleteAllShoppingItems()
                dao.deleteAllIngredients()
                dao.deleteAllRecipes()
                dao.deleteAllStaples()
                dao.insertShoppingItems(backup.shoppingItems)
                dao.insertRecipes(backup.recipes)
                dao.insertIngredients(backup.ingredients)
                dao.insertStaples(backup.staples)
            }
        }
        _messages.tryEmit(
            if (result.isSuccess) "Backup restored"
            else "Could not restore: this is not a valid backup file",
        )
    }
}
