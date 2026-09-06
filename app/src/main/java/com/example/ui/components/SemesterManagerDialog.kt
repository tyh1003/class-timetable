package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Semester

@Composable
fun SemesterManagerDialog(
    semesters: List<Semester>,
    activeSemesterId: Long,
    onSelectSemester: (Long) -> Unit,
    onAddSemester: (String) -> Unit,
    onRenameSemester: (Long, String) -> Unit,
    onDeleteSemester: (Semester) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var semesterToRename by remember { mutableStateOf<Semester?>(null) }
    var semesterToDelete by remember { mutableStateOf<Semester?>(null) }

    // Dialog to add a new semester
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新增學期") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "可自訂學期或年級名稱，例如「大一上學期」、「大一下」、「114-1」等：",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            if (it.isNotBlank()) isError = false
                        },
                        label = { Text("學期名稱") },
                        placeholder = { Text("例如：大二上學期") },
                        isError = isError,
                        supportingText = if (isError) { { Text("名稱不可為空") } } else null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_semester_name")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isBlank()) {
                            isError = true
                            return@Button
                        }
                        onAddSemester(newName.trim())
                        showAddDialog = false
                    },
                    modifier = Modifier.testTag("confirm_add_semester_button")
                ) {
                    Text("新增")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Dialog to rename a semester
    semesterToRename?.let { sem ->
        var editName by remember { mutableStateOf(sem.name) }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { semesterToRename = null },
            title = { Text("重新命名學期") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = {
                        editName = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("學期名稱") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isBlank()) {
                            isError = true
                            return@Button
                        }
                        onRenameSemester(sem.id, editName.trim())
                        semesterToRename = null
                    }
                ) {
                    Text("更新")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { semesterToRename = null }) {
                    Text("取消")
                }
            }
        )
    }

    // Dialog to confirm semester deletion
    semesterToDelete?.let { sem ->
        AlertDialog(
            onDismissRequest = { semesterToDelete = null },
            title = { Text("確認刪除學期") },
            text = {
                Text("刪除學期「${sem.name}」將會一併移除該學期的所有課程，確定要刪除嗎？")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSemester(sem)
                        semesterToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("確認刪除")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { semesterToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("學期 / 年級管理", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "點選即可切換當前課表學期，可依年級（如大一、大二）或上、下學期自行命名：",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(semesters, key = { it.id }) { sem ->
                        val isSelected = sem.id == activeSemesterId

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSemester(sem.id) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectSemester(sem.id) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sem.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 15.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "目前顯示中",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Rename button
                                IconButton(
                                    onClick = { semesterToRename = sem },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "重新命名",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Delete button (only if more than 1 semester exists)
                                if (semesters.size > 1) {
                                    IconButton(
                                        onClick = { semesterToDelete = sem },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "刪除學期",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_semester_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("新增學期 / 年級")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}
