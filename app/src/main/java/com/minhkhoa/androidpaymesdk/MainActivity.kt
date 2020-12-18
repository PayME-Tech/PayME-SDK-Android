package com.minhkhoa.androidpaymesdk

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import io.sentry.Sentry
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.model.Action
import vn.payme.sdk.model.Env
import java.lang.Exception
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    val AppToken: String =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6MX0.wNtHVZ-olKe7OAkgLigkTSsLVQKv_YL9fHKzX9mn9II"
    lateinit var payme: PayME
    lateinit var context: Context
    fun convertInt(amount: String): Int {
        try {
            return Integer.parseInt(amount)

        } catch (e: Exception) {
            return 0

        }

    }

    lateinit var button: Button
    lateinit var buttonSubmit: LinearLayout
    lateinit var buttonReload: ImageView
    lateinit var loading: ProgressBar
    lateinit var buttonDeposit: Button
    lateinit var buttonWithdraw: Button
    lateinit var buttonPay: Button
    lateinit var textView: TextView
    lateinit var inputUserId: EditText
    lateinit var inputPhoneNumber: EditText
    lateinit var moneyDeposit: EditText
    lateinit var moneyPay: EditText
    lateinit var moneyWithdraw: EditText

    val PublicKey: String = "-----BEGIN PUBLIC KEY-----\n" +
            "   MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKWcehEELB4GdQ4cTLLQroLqnD3AhdKi\n" +
            "   wIhTJpAi1XnbfOSrW/Ebw6h1485GOAvuG/OwB+ScsfPJBoNJeNFU6J0CAwEAAQ==\n" +
            "   -----END PUBLIC KEY-----"
    var ConnectToken: String = "Zn9T0j9jtZYPzi4B8Ti8NiXnEAJLACAljMcY20NKTyK58QzFP10VP4Tav2kKdmw\\/Xpq5Nm85hVpXGxFER6OPuBMcgUZRBhdkgc8SjkPDpjo="
    val PrivateKey: String = "-----BEGIN PRIVATE KEY-----\n" +
            "    MIIBPAIBAAJBAKWcehEELB4GdQ4cTLLQroLqnD3AhdKiwIhTJpAi1XnbfOSrW/Eb\n" +
            "    w6h1485GOAvuG/OwB+ScsfPJBoNJeNFU6J0CAwEAAQJBAJSfTrSCqAzyAo59Ox+m\n" +
            "    Q1ZdsYWBhxc2084DwTHM8QN/TZiyF4fbVYtjvyhG8ydJ37CiG7d9FY1smvNG3iDC\n" +
            "    dwECIQDygv2UOuR1ifLTDo4YxOs2cK3+dAUy6s54mSuGwUeo4QIhAK7SiYDyGwGo\n" +
            "    CwqjOdgOsQkJTGoUkDs8MST0MtmPAAs9AiEAjLT1/nBhJ9V/X3f9eF+g/bhJK+8T\n" +
            "    KSTV4WE1wP0Z3+ECIA9E3DWi77DpWG2JbBfu0I+VfFMXkLFbxH8RxQ8zajGRAiEA\n" +
            "    8Ly1xJ7UW3up25h9aa9SILBpGqWtJlNQgfVKBoabzsU=\n" +
            "    -----END PRIVATE KEY-----";


    fun updateWalletInfo() {

        payme.getWalletInfo(onSuccess = { jsonObject ->
            println("onSuccess=" + jsonObject.toString())
            val walletBalance = jsonObject.getJSONObject("walletBalance")
            val balance = walletBalance.get("balance")
            val decimal = DecimalFormat("#,###")
            textView.text = "${decimal.format(balance)}đ"
        }, onError = { jsonObject, code, message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

            println("onError=" + message)
        })

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Sentry.captureMessage("testing SDK setup");

        context = this
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonReload = findViewById(R.id.buttonReload)
        loading = findViewById(R.id.loading)
        buttonDeposit = findViewById(R.id.buttonDeposit)
        buttonWithdraw = findViewById(R.id.buttonWithdraw)
        buttonPay = findViewById(R.id.buttonPay)
        textView = findViewById(R.id.textBalance)
        inputUserId = findViewById(R.id.inputUserId)
        inputPhoneNumber = findViewById(R.id.inputPhoneNumber)
        moneyDeposit = findViewById(R.id.moneyDeposit)
        moneyPay = findViewById(R.id.moneyPay)
        moneyWithdraw = findViewById(R.id.moneyWithdraw)
        var configColor = arrayOf<String>("#75255b", "#9d455f")
        this.payme =
            PayME(this, AppToken, PublicKey, ConnectToken, PrivateKey, configColor, Env.SANDBOX)




        buttonReload.setOnClickListener {
            if (ConnectToken.length > 0) {
                updateWalletInfo()
            }

        }
        buttonSubmit.setOnClickListener {
            if (loading.visibility == View.INVISIBLE && inputUserId.text.toString().length > 0) {
                loading.visibility = View.VISIBLE
                payme.genConnectToken(inputUserId.text.toString(),
                    inputPhoneNumber.text.toString(),
                    onSuccess = { jsonObject: JSONObject ->
                        val connectToken = jsonObject.getString("connectToken")
                        loading.visibility = View.INVISIBLE
                        ConnectToken = connectToken
                        println("connectToken" + connectToken)
                        Toast.makeText(
                            context,
                            "Đăng ký Connect Token thành công",
                            Toast.LENGTH_LONG
                        ).show()
                        this.payme =
                            PayME(
                                this,
                                AppToken,
                                PublicKey,
                                ConnectToken,
                                PrivateKey,
                                configColor,
                                Env.SANDBOX
                            )
                    },
                    onError = { json: JSONObject?, code: Int?, message: String ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        loading.visibility = View.INVISIBLE

                    })
            }


        }
        button.setOnClickListener {
            if (ConnectToken.length > 0) {
                payme.openWallet(
                    Action.OPEN, null, null, null,
                    onSuccess = { json: JSONObject ->
                        println("onSuccess2222" + json.toString())
                    },
                    onError = { message: String ->
                        println("onError" + message)
                    })
            }


        }
        buttonDeposit.setOnClickListener {
            if (ConnectToken.length > 0) {

                val amount = convertInt(moneyDeposit.text.toString())
                payme.openWallet(Action.DEPOSIT, amount, null, null,
                    onSuccess = { json: JSONObject ->
                        println("onSuccess2222" + json.toString())
                    },
                    onError = { message: String ->
                        println("onError" + message)
                    })
            }

        }
        buttonWithdraw.setOnClickListener {
            if (ConnectToken.length > 0) {

                val amount = convertInt(moneyWithdraw.text.toString())

                payme.openWallet(Action.WITHDRAW, amount, null, null,
                    onSuccess = { json: JSONObject ->
                        println("onSuccess2222" + json.toString())
                    },
                    onError = { message: String ->
                        println("onError" + message)
                    })
            }
        }
        buttonPay.setOnClickListener {
            if (ConnectToken.length > 0) {
                val amount = convertInt(moneyPay.text.toString())
                payme.pay(this.supportFragmentManager, amount, "Merchant ghi chú đơn hàng", "", "",
                    onSuccess = { json: JSONObject ->
                        println("onSuccess2222" + json.toString())
                    },
                    onError = { message: String ->
                        println("onError" + message)
                    },
                    onClose = {
                        println("CLOSE")
                    }
                )

            }
        }
    }
}