package com.innerken.aadenprinterx.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.innerken.aadenprinterx.viewmodel.PrinterViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SettingPage(
    printerViewModel: PrinterViewModel,
    back: () -> Unit,
    onSaveClick: () -> Unit
) {
    var showIpDialog by remember { mutableStateOf(false) }
    var tempIP by remember { mutableStateOf(printerViewModel.globalSettingManager.ip) }

    var showFilterSetDialog by remember { mutableStateOf(false) }
    var tempFilterSet by remember { mutableStateOf(printerViewModel.globalSettingManager.filterSet) }

    val whiteListEnabled = remember { mutableStateOf(printerViewModel.globalSettingManager.filterMode) }

    if (showIpDialog) {
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = { Text("Edit IP/编辑") },
            text = {
                TextField(
                    value = tempIP,
                    onValueChange = { tempIP = it },
                    placeholder = { Text("Enter new IP/输入新的ip") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    printerViewModel.globalSettingManager.ip = tempIP // 保存
                    showIpDialog = false
                }) {
                    Text("Save/保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIpDialog = false }) {
                    Text("Cancel/取消")
                }
            }
        )
    }

    if (showFilterSetDialog) {
        AlertDialog(
            onDismissRequest = { showFilterSetDialog = false },
            title = { Text("Edit Filter Set/编辑") },
            text = {
                TextField(
                    value = tempFilterSet,
                    onValueChange = { tempFilterSet = it },
                    placeholder = { Text("Enter new IP/输入新的ip") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    printerViewModel.globalSettingManager.filterSet = tempIP // 保存
                    showFilterSetDialog = false
                }) {
                    Text("Save/保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterSetDialog = false }) {
                    Text("Cancel/取消")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { back()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "Aaden Printer X",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IP: ${printerViewModel.globalSettingManager.ip}",
                        color = Color.White
                    )
                    IconButton(onClick = {
                        tempIP = printerViewModel.globalSettingManager.ip
                        showIpDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit IP", tint = Color.White)
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("开启白名单/White List", color = Color.White)
                    Switch(
                        checked = whiteListEnabled.value,
                        onCheckedChange = { whiteListEnabled.value = it }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Set: ${printerViewModel.globalSettingManager.filterSet}",
                        color = Color.White
                    )
                    IconButton(onClick = {
                        tempFilterSet = printerViewModel.globalSettingManager.filterSet
                        showFilterSetDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Filter Set", tint = Color.White)
                    }
                }
            }


            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("保存/SAVE")
            }
        }
    }
}
