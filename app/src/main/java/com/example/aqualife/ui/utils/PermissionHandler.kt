package com.example.aqualife.ui.utils

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberCameraPermissionState() = rememberPermissionState(Manifest.permission.CAMERA)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberStoragePermissionsState() = rememberMultiplePermissionsState(
    permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberMicrophonePermissionState() = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberAllMediaPermissionsState() = rememberMultiplePermissionsState(
    permissions = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    ) + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun usePermissions(
    onPermissionsGranted: () -> Unit = {},
    onPermissionsDenied: () -> Unit = {}
): PermissionState {
    val cameraPermission = rememberCameraPermissionState()
    val storagePermissions = rememberStoragePermissionsState()
    val microphonePermission = rememberMicrophonePermissionState()
    
    var showRationale by remember { mutableStateOf(false) }
    
    return remember {
        object : PermissionState {
            override fun requestCamera() {
                if (cameraPermission.status.isGranted) {
                    onPermissionsGranted()
                } else {
                    cameraPermission.launchPermissionRequest()
                }
            }
            
            override fun requestStorage() {
                if (storagePermissions.allPermissionsGranted) {
                    onPermissionsGranted()
                } else {
                    storagePermissions.launchMultiplePermissionRequest()
                }
            }
            
            override fun requestMicrophone() {
                if (microphonePermission.status.isGranted) {
                    onPermissionsGranted()
                } else {
                    microphonePermission.launchPermissionRequest()
                }
            }
            
            override fun hasCameraPermission(): Boolean = cameraPermission.status.isGranted
            
            override fun hasStoragePermission(): Boolean = storagePermissions.allPermissionsGranted
            
            override fun hasMicrophonePermission(): Boolean = microphonePermission.status.isGranted
            
            override fun shouldShowRationale(): Boolean = showRationale
        }
    }
}

interface PermissionState {
    fun requestCamera()
    fun requestStorage()
    fun requestMicrophone()
    fun hasCameraPermission(): Boolean
    fun hasStoragePermission(): Boolean
    fun hasMicrophonePermission(): Boolean
    fun shouldShowRationale(): Boolean
}

