package com.novelforge.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novelforge.core.model.*
import com.novelforge.domain.workspace.*
import java.util.UUID

@Composable
fun WorkspaceStructureSettings(folders: List<StarterFolder>, onChange: (List<StarterFolder>) -> Unit) {
    var editKey by rememberSaveable { mutableStateOf<String?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Your standard structure", style = MaterialTheme.typography.titleLarge)
        Text("Choose the folders created for new novels. Every enabled folder includes an editable README. Existing novels are never changed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        folders.forEach { folder ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(folder.enabled, { enabled -> onChange(folders.map { if (it.key == folder.key) it.copy(enabled = enabled) else it }) })
                Text(folder.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                ActionIcon(Icons.Outlined.Edit, "Rename ${folder.name}", { editKey = folder.key; name = folder.name; error = null })
                ActionIcon(Icons.Outlined.Close, "Remove ${folder.name}", { onChange(folders.filter { it.key != folder.key }) })
            }
        }
        if (folders.none { it.enabled }) Text("No folders selected. Your standard workspace will start empty.", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { adding = true; name = ""; error = null }, enabled = folders.size < 40) {
            Icon(Icons.Outlined.CreateNewFolder, null)
            Spacer(Modifier.width(8.dp))
            Text("Add starter folder")
        }
        TextButton(onClick = { onChange(defaultStarterFolders) }) { Text("Restore default structure") }
        Text("Changes save automatically. Renaming Manuscript keeps its chapter templates; renaming Novel Overview keeps its opening guide.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (adding || editKey != null) AlertDialog(
        onDismissRequest = { adding = false; editKey = null },
        title = { Text(if (adding) "Add starter folder" else "Rename starter folder") },
        text = {
            Column {
                OutlinedTextField(name, { name = it; error = null }, label = { Text("Folder name") }, singleLine = true, isError = error != null)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = {
            try {
                val valid = MetadataPolicy.folder(name)
                val updated = if (adding) folders + StarterFolder("custom-${UUID.randomUUID()}", valid)
                    else folders.map { if (it.key == editKey) it.copy(name = valid) else it }
                onChange(validateStarterFolders(updated))
                adding = false; editKey = null
            } catch (e: IllegalArgumentException) { error = e.message }
        }) { Text("Save folder") } },
        dismissButton = { TextButton(onClick = { adding = false; editKey = null }) { Text("Cancel") } },
    )
}
