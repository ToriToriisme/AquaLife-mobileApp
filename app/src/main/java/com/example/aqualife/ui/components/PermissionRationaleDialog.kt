package com.example.aqualife.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PermissionRationaleDialog(
    permissionName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cần quyền $permissionName") },
        text = { 
            Text(
                when (permissionName) {
                    "Camera" -> "Ứng dụng cần quyền truy cập camera để chụp ảnh sản phẩm."
                    "Storage" -> "Ứng dụng cần quyền truy cập bộ nhớ để lưu và chọn ảnh."
                    "Microphone" -> "Ứng dụng cần quyền truy cập microphone để tìm kiếm bằng giọng nói."
                    else -> "Ứng dụng cần quyền này để hoạt động đầy đủ."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Cho phép")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        modifier = modifier
    )
}

