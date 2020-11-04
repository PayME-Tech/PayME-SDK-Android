package com.minhkhoa.androidpaymesdk

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.model.Action
import vn.payme.sdk.model.Env

class MainActivity : AppCompatActivity() {
    val AppToken: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6MX0.wNtHVZ-olKe7OAkgLigkTSsLVQKv_YL9fHKzX9mn9II"
    lateinit var payme: PayME
    lateinit var context: Context

    val PublicKey: String = "-----BEGIN PUBLIC KEY-----\n" +
            "   MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKWcehEELB4GdQ4cTLLQroLqnD3AhdKi\n" +
            "   wIhTJpAi1XnbfOSrW/Ebw6h1485GOAvuG/OwB+ScsfPJBoNJeNFU6J0CAwEAAQ==\n" +
            "   -----END PUBLIC KEY-----"
    var ConnectToken: String = "U2FsdGVkX1+iOzrma3dpZN1ZKgNFruz8U4cD+Pa5hNrRtsQ4b1ID3t42seTswQfDOdYyBiUzUjHj+LqcSoHOqAXQKkFibrbZHnGDxUyaD3sXkIG+OvhPAfVAMRos6tp8FwxWwXTYDeBMgnRMfazFPA=="
    val PrivateKey: String = "-----BEGIN PRIVATE KEY-----\n" +
            "    MIIBPAIBAAJBAKWcehEELB4GdQ4cTLLQroLqnD3AhdKiwIhTJpAi1XnbfOSrW/Eb\n" +
            "    w6h1485GOAvuG/OwB+ScsfPJBoNJeNFU6J0CAwEAAQJBAJSfTrSCqAzyAo59Ox+m\n" +
            "    Q1ZdsYWBhxc2084DwTHM8QN/TZiyF4fbVYtjvyhG8ydJ37CiG7d9FY1smvNG3iDC\n" +
            "    dwECIQDygv2UOuR1ifLTDo4YxOs2cK3+dAUy6s54mSuGwUeo4QIhAK7SiYDyGwGo\n" +
            "    CwqjOdgOsQkJTGoUkDs8MST0MtmPAAs9AiEAjLT1/nBhJ9V/X3f9eF+g/bhJK+8T\n" +
            "    KSTV4WE1wP0Z3+ECIA9E3DWi77DpWG2JbBfu0I+VfFMXkLFbxH8RxQ8zajGRAiEA\n" +
            "    8Ly1xJ7UW3up25h9aa9SILBpGqWtJlNQgfVKBoabzsU=\n" +
            "    -----END PRIVATE KEY-----";


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_main)
        val button: Button = findViewById(R.id.button)
        val buttonDeposit: Button = findViewById(R.id.buttonDeposit)
        val buttonWithdraw: Button = findViewById(R.id.buttonWithdraw)
        val buttonPay: Button = findViewById(R.id.buttonPay)
        val textView: TextView = findViewById(R.id.textview)
        val inputConnectToken: EditText = findViewById(R.id.inputConnectToken)
        var configColor = arrayOf<String>("#75255b", "#9d455f")
         this.payme = PayME(this, AppToken, PublicKey, ConnectToken, PrivateKey, configColor, Env.SANDBOX)




        inputConnectToken.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // you can call or do what you want with your EditText here
                // yourEditText...
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                ConnectToken = s.toString()
                payme = PayME(context, AppToken, PublicKey, ConnectToken, PrivateKey, configColor, Env.SANDBOX)

            }
        })

        payme.geWalletInfo(onSuccess = { jsonObject ->
            println("onSuccess=" + jsonObject.toString())
            val walletBalance = jsonObject.getJSONObject("walletBalance")
            val balance = walletBalance.get("balance")
            textView.setText("Số dư : " + balance.toString())


        }, onError = { jsonObject, code, message ->
            println("onError=" + message)
        })


        button.setOnClickListener {
            payme.openWallet(
                Action.OPEN, null, null, null,
                onSuccess = { json: JSONObject ->
                    println("onSuccess2222" + json.toString())
                },
                onError = { message: String ->
                    println("onError" + message)
                })

        }
        buttonDeposit.setOnClickListener {
            payme.openWallet(Action.DEPOSIT, null, null, null,
                onSuccess = { json: JSONObject ->
                    println("onSuccess2222" + json.toString())
                },
                onError = { message: String ->
                    println("onError" + message)
                })

        }
        buttonWithdraw.setOnClickListener {
            payme.openWallet(Action.WITHDRAW, null, null, null,
                onSuccess = { json: JSONObject ->
                    println("onSuccess2222" + json.toString())
                },
                onError = { message: String ->
                    println("onError" + message)
                })

        }
        buttonPay.setOnClickListener {
            payme.pay(this.supportFragmentManager, 2000000, "Merchant ghi chú đơn hàng", "", "",
                onSuccess = { json: JSONObject ->
                    println("onSuccess2222" + json.toString())
                },
                onError = { message: String ->
                    println("onError" + message)
                })

        }
    }
}