package com.example.aqualife.ui.screen

// ============================================================================
// ANDROIDX IMPORTS
// ============================================================================
// Compose Foundation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

// Compose Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

// Compose Material3
import androidx.compose.material3.*

// Compose Runtime
import androidx.compose.runtime.*

// Compose UI
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Navigation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

// ============================================================================
// THIRD-PARTY IMPORTS
// ============================================================================
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

// ============================================================================
// LOCAL IMPORTS
// ============================================================================
import com.example.aqualife.ui.viewmodel.NotificationViewModel

// ============================================================================
// NOTIFICATIONS SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.allNotifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    
    // Initialize sample notifications if empty
    LaunchedEffect(Unit) {
        if (notifications.isEmpty()) {
            viewModel.createSampleNotifications()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Thông báo", fontWeight = FontWeight.Bold)
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge {
                                Text("$unreadCount")
                            }
                        }
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text("Đọc tất cả", fontSize = 12.sp)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chưa có thông báo nào", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onRead = { viewModel.markAsRead(notification.id) },
                        onDelete = { viewModel.deleteNotification(notification.id) }
                    )
                }
            }
        }
    }
}

// ============================================================================
// NOTIFICATION ITEM COMPONENT
// ============================================================================

@Composable
fun NotificationItem(
    notification: com.example.aqualife.data.local.entity.NotificationEntity,
    onRead: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = when (notification.type) {
        "ORDER" -> Icons.Default.ShoppingCart
        "PROMOTION" -> Icons.Default.LocalOffer
        "SYSTEM" -> Icons.Default.Info
        else -> Icons.Default.Notifications
    }
    
    val iconColor = when (notification.type) {
        "ORDER" -> Color(0xFF4CAF50)
        "PROMOTION" -> Color(0xFFFF9800)
        "SYSTEM" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!notification.isRead) onRead() },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Show image if available, otherwise show icon
            if (notification.imageUrl != null && notification.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = notification.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatNotificationTime(notification.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            if (!notification.isRead) {
                Badge(
                    modifier = Modifier.align(Alignment.Top)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

fun formatNotificationTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Vừa xong"
        diff < 3600000 -> "${diff / 60000} phút trước"
        diff < 86400000 -> "${diff / 3600000} giờ trước"
        diff < 604800000 -> "${diff / 86400000} ngày trước"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
