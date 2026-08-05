package com.voicebudget.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.voicebudget.R
import com.voicebudget.domain.model.CustomCategory
import com.voicebudget.domain.model.CustomCategoryIcon
import com.voicebudget.domain.model.TransactionType
import com.voicebudget.presentation.components.customCategoryColorPalette
import com.voicebudget.presentation.components.customCategoryIcon

/** Settings entry point: lets the user add, rename, restyle, and remove their own categories. */
@Composable
fun CategoryManagementHost(
    onDismiss: () -> Unit,
    viewModel: CategoryManagementViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    var pendingDelete by remember { mutableStateOf<CustomCategory?>(null) }

    CategoryListDialog(
        categories = categories,
        onAddClick = viewModel::openAddDialog,
        onEditClick = viewModel::openEditDialog,
        onDeleteClick = { pendingDelete = it },
        onDismiss = onDismiss,
    )

    val editing = dialogState
    if (editing is CategoryDialogState.Editing) {
        CategoryEditorDialog(
            isNew = editing.id == null,
            name = editing.name,
            type = editing.type,
            icon = editing.icon,
            color = editing.color,
            nameError = editing.showNameError,
            onNameChange = { name -> viewModel.updateDraft { it.copy(name = name, showNameError = false) } },
            onTypeChange = { type -> viewModel.updateDraft { it.copy(type = type) } },
            onIconChange = { icon -> viewModel.updateDraft { it.copy(icon = icon) } },
            onColorChange = { color -> viewModel.updateDraft { it.copy(color = color) } },
            onConfirm = viewModel::confirmSave,
            onDismiss = viewModel::dismissDialog,
        )
    }

    pendingDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.categories_delete_title)) },
            text = { Text(stringResource(R.string.categories_delete_message, category.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(category)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun CategoryListDialog(
    categories: List<CustomCategory>,
    onAddClick: () -> Unit,
    onEditClick: (CustomCategory) -> Unit,
    onDeleteClick: (CustomCategory) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_manage_categories)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                TextButton(onClick = onAddClick, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(stringResource(R.string.categories_add_new))
                }

                if (categories.isEmpty()) {
                    Text(
                        text = stringResource(R.string.categories_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { category ->
                            CustomCategoryRow(
                                category = category,
                                onEdit = { onEditClick(category) },
                                onDelete = { onDeleteClick(category) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
    )
}

@Composable
private fun CustomCategoryRow(
    category: CustomCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = Color(category.color.toInt())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(customCategoryIcon(category.icon), contentDescription = null, tint = color)
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            )
            Text(
                text = stringResource(if (category.type == TransactionType.EXPENSE) R.string.type_expense else R.string.income),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        }
    }
}

@Composable
private fun CategoryEditorDialog(
    isNew: Boolean,
    name: String,
    type: TransactionType,
    icon: CustomCategoryIcon,
    color: Long,
    nameError: Boolean,
    onNameChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onIconChange: (CustomCategoryIcon) -> Unit,
    onColorChange: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (isNew) R.string.categories_add_title else R.string.categories_edit_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.categories_field_name)) },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text(stringResource(R.string.error_category_name_required)) }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TransactionType.entries.forEachIndexed { index, entryType ->
                        SegmentedButton(
                            selected = type == entryType,
                            onClick = { onTypeChange(entryType) },
                            shape = SegmentedButtonDefaults.itemShape(index, TransactionType.entries.size),
                        ) {
                            Text(stringResource(if (entryType == TransactionType.EXPENSE) R.string.type_expense else R.string.income))
                        }
                    }
                }

                Text(stringResource(R.string.categories_field_icon), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomCategoryIcon.entries.forEach { entryIcon ->
                        val selected = entryIcon == icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                )
                                .clickable { onIconChange(entryIcon) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                customCategoryIcon(entryIcon),
                                contentDescription = null,
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }

                Text(stringResource(R.string.categories_field_color), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    customCategoryColorPalette.forEach { entryColor ->
                        val selected = entryColor == color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(entryColor.toInt()))
                                .then(
                                    if (selected) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else {
                                        Modifier
                                    },
                                )
                                .clickable { onColorChange(entryColor) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
