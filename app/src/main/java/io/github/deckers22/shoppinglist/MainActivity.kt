package io.github.deckers22.shoppinglist

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.deckers22.shoppinglist.ui.BackupScreen
import io.github.deckers22.shoppinglist.ui.RecipeDetailScreen
import io.github.deckers22.shoppinglist.ui.RecipesScreen
import io.github.deckers22.shoppinglist.ui.ShoppingListScreen
import io.github.deckers22.shoppinglist.ui.StaplesScreen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppTheme { App() } }
    }
}

@Composable
private fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colors, content = content)
}

private enum class Tab(val label: String, val icon: ImageVector) {
    LIST("List", Icons.Default.ShoppingCart),
    RECIPES("Recipes", Icons.Default.Favorite),
    STAPLES("Staples", Icons.Default.Star),
    BACKUP("Backup", Icons.Default.Settings),
}

@Composable
private fun App() {
    val vm: AppViewModel = viewModel()
    var tab by rememberSaveable { mutableStateOf(Tab.LIST) }
    var openRecipeId by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.messages.collectLatest { snackbar.showSnackbar(it) }
    }
    BackHandler(enabled = openRecipeId != null) { openRecipeId = null }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach {
                    NavigationBarItem(
                        selected = tab == it,
                        onClick = {
                            tab = it
                            openRecipeId = null
                        },
                        icon = { Icon(it.icon, contentDescription = null) },
                        label = { Text(it.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            val recipeId = openRecipeId
            when (tab) {
                Tab.LIST -> ShoppingListScreen(vm)
                Tab.RECIPES ->
                    if (recipeId == null) {
                        RecipesScreen(vm, onOpen = { openRecipeId = it })
                    } else {
                        RecipeDetailScreen(vm, recipeId, onBack = { openRecipeId = null })
                    }
                Tab.STAPLES -> StaplesScreen(vm)
                Tab.BACKUP -> BackupScreen(vm)
            }
        }
    }
}
