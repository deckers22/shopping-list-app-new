package io.github.deckers22.shoppinglist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Shopping list
    @Query("SELECT * FROM shopping_items ORDER BY position, id")
    fun observeShoppingItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items ORDER BY position, id")
    suspend fun getShoppingItems(): List<ShoppingItem>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM shopping_items")
    suspend fun nextShoppingPosition(): Int

    @Insert
    suspend fun insertShoppingItems(items: List<ShoppingItem>)

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Query("UPDATE shopping_items SET position = :position WHERE id = :id")
    suspend fun setShoppingPosition(id: Long, position: Int)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteShoppingItem(id: Long)

    @Query("DELETE FROM shopping_items WHERE checked = 1")
    suspend fun deleteCheckedShoppingItems()

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAllShoppingItems()

    // Recipes
    @Query("SELECT * FROM recipes ORDER BY name COLLATE NOCASE")
    fun observeRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes")
    suspend fun getRecipes(): List<Recipe>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeRecipe(id: Long): Flow<Recipe?>

    @Insert
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert
    suspend fun insertRecipes(recipes: List<Recipe>)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Long)

    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()

    // Ingredients
    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY position, id")
    fun observeIngredients(recipeId: Long): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients ORDER BY recipeId, position, id")
    suspend fun getIngredients(): List<Ingredient>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM ingredients WHERE recipeId = :recipeId")
    suspend fun nextIngredientPosition(recipeId: Long): Int

    @Insert
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredient(id: Long)

    @Query("DELETE FROM ingredients")
    suspend fun deleteAllIngredients()

    // Staples
    @Query("SELECT * FROM staples ORDER BY position, id")
    fun observeStaples(): Flow<List<Staple>>

    @Query("SELECT * FROM staples ORDER BY position, id")
    suspend fun getStaples(): List<Staple>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM staples")
    suspend fun nextStaplePosition(): Int

    @Insert
    suspend fun insertStaples(staples: List<Staple>)

    @Update
    suspend fun updateStaple(staple: Staple)

    @Query("UPDATE staples SET position = :position WHERE id = :id")
    suspend fun setStaplePosition(id: Long, position: Int)

    @Query("DELETE FROM staples WHERE id = :id")
    suspend fun deleteStaple(id: Long)

    @Query("DELETE FROM staples")
    suspend fun deleteAllStaples()
}
