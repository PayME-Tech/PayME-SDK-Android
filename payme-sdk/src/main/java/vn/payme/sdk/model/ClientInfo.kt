package vn.payme.sdk.model

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.provider.Settings
import vn.payme.sdk.PayME
import java.io.File
import java.lang.reflect.Method
import kotlin.random.Random


class ClientInfo {
    var appVersion: String = ""
    var sdkVerSion: String = "0.9.37"
    var appPackageName: String? = ""
    var deviceId: String? = ""
    fun getDeviceName(): String? {
        return try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod: Method = systemPropertiesClass.getMethod("get", String::class.java)
            val `object` = Any()
            val obj: Any = getMethod.invoke(`object`, "ro.product.device")
            if (obj == null) "" else obj as String
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))
    }
    constructor(context: Context) {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        this.appVersion = packageInfo.versionName
        this.deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        if(deviceId?.length==0){
            val pref = context.getSharedPreferences("PayME_SDK", Context.MODE_PRIVATE)
            val clientId = pref.getString("deviceId_random", "")
            if(clientId?.length==0){
                val nextValues = Random.nextInt(0, 1000000000)
                this.deviceId =  nextValues.toString()
                pref.edit().putString("deviceId_random", nextValues.toString()).commit()
            }else{
                this.deviceId =clientId
            }
        }
        this.appPackageName = context.packageName
    }
    constructor() {

    }

   private fun getRootInfo(): Boolean {
        return if (checkRootFiles() || checkTags()) {
            true
        } else false
    }

    private fun checkRootFiles(): Boolean {
        var root = false
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            root = File(path).exists()
            if (root) break
        }
        return root
    }

    private fun checkTags(): Boolean {
        val tag = Build.TAGS
        return tag != null && tag.trim { it <= ' ' }.contains("test-keys")
    }
    fun getClientInfo(): MutableMap<String, Any> {
        val clientInfo: MutableMap<String, Any> = mutableMapOf()
        clientInfo["deviceId"] = deviceId.toString()
        clientInfo["platform"] = "ANDROID_SDK"
        clientInfo["channel"] = ""
        clientInfo["version"] = sdkVerSion
        clientInfo["isEmulator"] =  isEmulator()
        clientInfo["isRoot"] =  getRootInfo()
        clientInfo["userAgent"] =  getDeviceName().toString()
        return clientInfo
    }
}