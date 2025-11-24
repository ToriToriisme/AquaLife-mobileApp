package com.example.aqualife.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aqualife.data.local.dao.NotificationDao
import com.example.aqualife.data.local.entity.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    val allNotifications: StateFlow<List<NotificationEntity>> = notificationDao.getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadNotifications: StateFlow<List<NotificationEntity>> = notificationDao.getUnreadNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = notificationDao.getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationDao.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationDao.markAllAsRead()
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationDao.deleteNotification(notificationId)
        }
    }

    // Create sample notifications for demo
    fun createSampleNotifications() {
        viewModelScope.launch {
            val sampleNotifications = listOf(
                NotificationEntity(
                    id = "1",
                    title = "Đơn hàng mới",
                    message = "Bạn có đơn hàng mới #12345",
                    type = "ORDER"
                ),
                NotificationEntity(
                    id = "2",
                    title = "Khuyến mãi",
                    message = "Giảm 20% cho tất cả cá biển",
                    type = "PROMOTION"
                ),
                NotificationEntity(
                    id = "3",
                    title = "Thông báo hệ thống",
                    message = "Ứng dụng đã được cập nhật",
                    type = "SYSTEM"
                )
            )
            sampleNotifications.forEach { notification ->
                notificationDao.insertNotification(notification)
            }
        }
    }
}

