package vn.payme.sdk.kyc

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class PermisionCamera() {
    fun isGrantedCamera(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        } else {
            return true
        }
    }

    fun requestCamera(context: Context, activity: FragmentActivity) {
        val MY_CAMERA_REQUEST_CODE = 100
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                MY_CAMERA_REQUEST_CODE
            );
        } else {
        }
    }

    fun openSetting(activity: Activity) {
        val i = Intent()
        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:" + activity?.applicationContext!!.packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        activity?.applicationContext!!.startActivity(i)
    }
}