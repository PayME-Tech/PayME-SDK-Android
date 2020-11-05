package vn.payme.sdk.model

import android.content.Context
import android.content.pm.PackageInfo
import android.provider.Settings
import vn.payme.sdk.PayME

class ClientInfo {
    var appVersion: String = ""
    var sdkVerSion: String = ""
    var appPackageName: String? = ""
    var deviceId: String? = ""

    constructor(context: Context) {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        this.appVersion = packageInfo.versionName
        this.deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        this.appPackageName = context.packageName
    }
    constructor() {

    }

    fun getClientInfo(): MutableMap<String, Any> {
        val clientInfo: MutableMap<String, Any> = mutableMapOf()
        clientInfo["clientId"] = deviceId.toString()
        clientInfo["platform"] = "ANDROID"
        clientInfo["appVersion"] = appVersion.toString()
        clientInfo["sdkVesion"] = sdkVerSion.toString()
        clientInfo["sdkType"] = "native"
        clientInfo["appPackageName"] = appPackageName.toString()
        return clientInfo
    }
}