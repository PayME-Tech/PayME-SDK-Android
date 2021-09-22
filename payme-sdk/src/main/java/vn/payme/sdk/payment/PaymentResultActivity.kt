package vn.payme.sdk.payment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsIntent

import vn.payme.sdk.PayME
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.store.Store
import android.content.ComponentName

import androidx.browser.customtabs.CustomTabsClient

import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vn.payme.sdk.api.PaymentApi


class PaymentResultActivity : AppCompatActivity() {


    lateinit var mClient: CustomTabsClient
    var count = 0
    var navigationEventSave = 0

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
                    if(Store.paymentInfo.deeplinkUrlScheme.length>0 && navigationEventSave != 6){
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
        CustomTabsClient.bindCustomTabsService(
            this,
            "com.android.chrome",
            object : CustomTabsServiceConnection() {

                override fun onCustomTabsServiceConnected(
                    name: ComponentName,
                    client: CustomTabsClient
                ) {
                    // mClient is now valid.

                    mClient = client
                    mClient.warmup(0);
                    val session = mClient.newSession(object : CustomTabsCallback() {
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
                    openChrome(session)


                }

                override fun onServiceDisconnected(name: ComponentName) {
                    // mClient is no longer valid. This also invalidates sessions.
//                    mClient = null
                }
            })
        return


    }

    fun openChrome(session: CustomTabsSession?) {
        val builder = CustomTabsIntent.Builder();
        builder.setShowTitle(false)
        builder.setInstantAppsEnabled(true)
        if (session != null) {
            builder.setSession(session)
        }
        val customTabsIntent = builder.build();
        val url = intent.getStringExtra("qrContent")
//        val link = "https://stevesouders.com/misc/test-postmessage.php"

        customTabsIntent.intent.setData(Uri.parse(url))
        customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivityForResult(customTabsIntent.intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            finish()
            Store.paymentInfo.deeplinkUrlScheme = ""
            PayME.onError(null, ERROR_CODE.USER_CANCELLED, "")
        }
    }

}