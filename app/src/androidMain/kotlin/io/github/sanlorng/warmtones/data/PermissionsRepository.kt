package io.github.sanlorng.warmtones.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionsRepository(private val context: Context) {

    private val _permissionsGranted = MutableStateFlow(hasRequiredPermissions())
    val permissionsGranted = _permissionsGranted.asStateFlow()

    fun hasRequiredPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun updatePermissionsStatus() {
        _permissionsGranted.value = hasRequiredPermissions()
    }

    fun getRequiredPermissions(): Array<String> {
        return REQUIRED_PERMISSIONS
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)
    }
}