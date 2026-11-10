package io.github.deckers22.shoppinglist.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.deckers22.shoppinglist.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(vm: AppViewModel) {
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> if (uri != null) vm.exportBackup(uri) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> pendingImport = uri }

    Scaffold(topBar = { TopAppBar(title = { Text("Backup") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Your shopping list, recipes and staples are stored on this phone. " +
                    "Installing a newer version of the app keeps them.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Uninstalling the app or switching phones does not. Save a backup file " +
                    "now and then, for example to Google Drive, and restore it when needed.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = { exportLauncher.launch("shopping-list-backup-${LocalDate.now()}.json") },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save backup file") }
            OutlinedButton(
                // Some file apps do not label .json files as JSON, so allow any file here;
                // a wrong file is rejected when it is read.
                onClick = { importLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Restore from backup file") }
        }
    }

    pendingImport?.let { uri ->
        ConfirmDialog(
            title = "Restore backup?",
            text = "Everything currently in the app will be replaced by the contents of the backup file.",
            confirmLabel = "Restore",
            onConfirm = {
                vm.importBackup(uri)
                pendingImport = null
            },
            onDismiss = { pendingImport = null },
        )
    }
}
