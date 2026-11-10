package io.github.deckers22.shoppinglist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// When a table changes: raise `version` and add a Migration to the builder below.
// Never use fallbackToDestructiveMigration - it wipes the user's data on update.
@Database(
    entities = [ShoppingItem::class, Recipe::class, Ingredient::class, Staple::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "shopping.db",
            ).build().also { instance = it }
        }
    }
}
