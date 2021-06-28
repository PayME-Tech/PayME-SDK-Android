package vn.payme.sdk.evenbus

import android.content.Intent

class ActivityResult(
    val requestCode: Int,val  resultCode: Int, val data: Intent?
) {

}