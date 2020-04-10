package com.dkarakaya.songify.util

import android.Manifest
import android.R
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar


const val REQUEST_EXTERNAL_STORAGE_INTENT = 0

// Storage Permissions
const val REQUEST_EXTERNAL_STORAGE = 1
private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
)

/**
 * Checks if the app has permission to write to device storage
 *
 * If the app does not has permission then the user will be prompted to grant permissions
 *
 */
fun Activity.verifyStoragePermissions() {
    // Check if we have write permission
    val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
    if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
        // Permission is not granted
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE)
        } else {
            // No explanation needed, we can request the permission.
            requestPermissionSnackbar()
        }
    }
}

private fun Activity.requestPermissionSnackbar() {
    val snackbar = Snackbar.make(findViewById(R.id.content), "Songify needs the access to external storage", Snackbar.LENGTH_LONG)
    snackbar.setAction("TO SETTINGS") {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        ActivityCompat.startActivityForResult(this, intent, REQUEST_EXTERNAL_STORAGE_INTENT, null)
    }
    snackbar.show()
}
