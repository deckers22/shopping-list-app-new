package io.github.deckers22.shoppinglist.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

// These classes are both the database tables and the backup file format.
// New fields must have a default value so older backup files keep importing.

@Serializable
@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double? = null,
    val unit: String = "",
    val checked: Boolean = false,
    val position: Int = 0,
    /** Name of the recipe this item came from, if any. */
    val source: String? = null,
)

@Serializable
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Number of people the ingredient amounts are written for. */
    val servings: Int = 4,
    val notes: String = "",
)

@Serializable
@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipeId")],
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val name: String,
    val amount: Double? = null,
    val unit: String = "",
    val position: Int = 0,
)

/** A standard grocery that can be put on the shopping list with one tap. */
@Serializable
@Entity(tableName = "staples")
data class Staple(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double? = null,
    val unit: String = "",
    val position: Int = 0,
)

@Serializable
data class Backup(
    val version: Int = 1,
    val exportedAt: String = "",
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val ingredients: List<Ingredient> = emptyList(),
    val staples: List<Staple> = emptyList(),
)
