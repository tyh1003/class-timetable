package com.example.ui.components

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimeSlot

@Composable
fun TimeSlotManagerDialog(
    timeSlots: List<TimeSlot>,
    onAddSlot: (code: String, start: String, end: String) -> Unit,
    onUpdateSlot: (TimeSlot) -> Unit,
    onDeleteSlot: (TimeSlot) -> Unit,
    onResetToBenchmark: () -> Unit,
    onDismiss: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var slotToEdit by remember { mutableStateOf<TimeSlot?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Dialog for adding a new slot
    if (showAddDialog) {
        var code by remember { mutableStateOf("") }
        var startTime by remember { mutableStateOf("") }
        var endTime by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新增節次時程") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it; isError = false },
                        label = { Text("節次名稱 / 代碼") },
                        placeholder = { Text("例如：10, e, 午休") },
                        isError = isError && code.isBlank(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_slot_code")
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it; isError = false },
                        label = { Text("開始時間") },
                        placeholder = { Text("例如：08:00") },
                        isError = isError && startTime.isBlank(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_slot_start")
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it; isError = false },
                        label = { Text("結束時間") },
                        placeholder = { Text("例如：08:50") },
                        isError = isError && endTime.isBlank(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_slot_end")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isBlank() || startTime.isBlank() || endTime.isBlank()) {
                            isError = true
                            return@Button
                        }
                        onAddSlot(code.trim(), startTime.trim(), endTime.trim())
                        showAddDialog = false
                    },
                    modifier = Modifier.testTag("confirm_add_slot_button")
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

    // Dialog for editing existing slot
    slotToEdit?.let { slot ->
        var code by remember { mutableStateOf(slot.code) }
        var startTime by remember { mutableStateOf(slot.startTime) }
        var endTime by remember { mutableStateOf(slot.endTime) }

        AlertDialog(
            onDismissRequest = { slotToEdit = null },
            title = { Text("編輯節次時程") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("節次名稱 / 代碼") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("開始時間") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("結束時間") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                            onUpdateSlot(slot.copy(code = code.trim(), startTime = startTime.trim(), endTime = endTime.trim()))
                            slotToEdit = null
                        }
                    }
                ) {
                    Text("儲存")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { slotToEdit = null }) {
                    Text("取消")
                }
            }
        )
    }

    // Reset confirmation
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("恢復預設基準時程") },
            text = {
                Text("確定要將節次時程重設為預設基準表（y, z, 1~4, n, 5~9, a~d）嗎？")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToBenchmark()
                        showResetConfirm = false
                    }
                ) {
                    Text("恢復預設")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirm = false }) {
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
                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("時程表設定", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "可自行新增、修改時間或刪除節次：",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(timeSlots, key = { it.id }) { slot ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = slot.code,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${slot.startTime} ~ ${slot.endTime}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }

                                IconButton(
                                    onClick = { slotToEdit = slot },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "修改",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (timeSlots.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteSlot(slot) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "刪除",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("重設基準表", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_slot_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("新增節次", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("關閉")
            }
        }
    )
}
