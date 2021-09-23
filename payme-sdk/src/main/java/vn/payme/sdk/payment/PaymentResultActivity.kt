package vn.payme.sdk.payment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import vn.payme.sdk.PayME
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.store.Store
import android.content.ComponentName
import androidx.browser.customtabs.*

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import vn.payme.sdk.api.PaymentApi
import androidx.browser.customtabs.CustomTabsClient

import androidx.browser.customtabs.CustomTabsServiceConnection


class PaymentResultActivity : AppCompatActivity() {
    var count = 0
    var navigationEventSave = 0
    var opened = false

    fun loopCallApi() {
        count++
        val paymentApi = PaymentApi()
        paymentApi.checkVisa(onSuccess = { jsonObject ->
            val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
            val Payment = OpenEWallet.optJSONObject("Payment")
            val GetTransactionInfo = Payment.optJSONObject("GetTransactionInfo")
            val state = GetTransactionInfo.optString("state")
            val succeeded = GetTransactionInfo.optBoolean("succeeded")
            if (succeeded) {
                if (state == "SUCCEEDED") {
                    if (Store.paymentInfo.deeplinkUrlScheme.length > 0 && navigationEventSave != 6) {
                        val urlString = Store.paymentInfo.deeplinkUrlScheme
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        PayME.context.startActivity(intent);
                    }
                } else if (state == "PENDING") {
                    if (navigationEventSave != 6 && count <= 6) {
                        GlobalScope.launch {
                            delay(5000)
                            loopCallApi()
                        }
                    }


                } else {
                }

            } else {
            }


        }, onError = { jsonObject, code, s ->

        })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connection: CustomTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                name: ComponentName, client: CustomTabsClient
            ) {
                val session = client.newSession(object : CustomTabsCallback() {
                    override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                        super.onNavigationEvent(navigationEvent, extras)
                        if (navigationEventSave == 6) {
                            count = 0
                            loopCallApi()

                        }
                        navigationEventSave = navigationEvent
                        if (navigationEvent == 2) {


                        }
                    }

                    override fun onPostMessage(message: String, extras: Bundle?) {
                        super.onPostMessage(message, extras)
                    }
                })
                if (!opened) {
                    openChrome(session)

                }
            }

            override fun onServiceDisconnected(componentName: ComponentName) {}
        }
        CustomTabsClient.bindCustomTabsService(
            this,
            "com.android.chrome", connection
        )
        android.os.Handler().postDelayed(
            {
                if (!opened) {
                    println("openChrome2222222222")
                    openChrome(null)
                }

            },
            500 // Timeout value
        )

        return


    }

    fun openChrome(session: CustomTabsSession?) {
        opened = true
        val builder = CustomTabsIntent.Builder();
        builder.setShowTitle(false)
        builder.setInstantAppsEnabled(true)
        if (session != null) {
            builder.setSession(session)
        }
        val customTabsIntent = builder.build();
        val url = intent.getStringExtra("qrContent")

        customTabsIntent.intent.setData(Uri.parse(url))
        customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivityForResult(customTabsIntent.intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("onActivityResult" + requestCode)
        if (requestCode == 101) {
            finish()
            Store.paymentInfo.deeplinkUrlScheme = ""
            PayME.onError(null, ERROR_CODE.USER_CANCELLED, "")
        }
    }

}