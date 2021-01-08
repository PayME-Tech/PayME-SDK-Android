package com.minhkhoa.androidpaymesdk

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import io.sentry.Sentry
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.model.Action
import vn.payme.sdk.model.Env
import vn.payme.sdk.model.InfoPayment
import java.lang.Exception
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

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
    lateinit var buttonChangeEnv: Button
     var env= Env.DEV
    val AppToken: String =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6MX0.wNtHVZ-olKe7OAkgLigkTSsLVQKv_YL9fHKzX9mn9II"
    val PublicKey: String = "-----BEGIN PUBLIC KEY-----\n" +
            "   MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKWcehEELB4GdQ4cTLLQroLqnD3AhdKi\n" +
            "   wIhTJpAi1XnbfOSrW/Ebw6h1485GOAvuG/OwB+ScsfPJBoNJeNFU6J0CAwEAAQ==\n" +
            "   -----END PUBLIC KEY-----"
    var ConnectToken: String ="Wg6/5DKn0Cz2POaTJaSDVAKxnUwh0ufV7l8VIkrOhqJ91EMkC+LzA155acuiKGe2xo+0+iA7gS9POxYsvTIoc/gBHjypVn5Jauy5QpDsep0="
//    var ConnectToken: String =""
//    var ConnectToken: String =
//        "8T/jAlhqHz14QDV0kx2M/Rf+9hY55ojZwUJA2fQVoPY9kIQLWtcajE11A7bh5FMbXGH9UM+pOd0IEW8l/hMWL0eI2/FKTN67i7arcr89fHU="

    //    var ConnectToken: String =
//        "Zn9T0j9jtZYPzi4B8Ti8NiXnEAJLACAljMcY20NKTyK58QzFP10VP4Tav2kKdmw\\/Xpq5Nm85hVpXGxFER6OPuBMcgUZRBhdkgc8SjkPDpjo="
    val PrivateKey: String = "-----BEGIN PRIVATE KEY-----\n" +
            "    MIIBPAIBAAJBAKWcehEELB4GdQ4cTLLQroLqnD3AhdKiwIhTJpAi1XnbfOSrW/Eb\n" +
            "    w6h1485GOAvuG/OwB+ScsfPJBoNJeNFU6J0CAwEAAQJBAJSfTrSCqAzyAo59Ox+m\n" +
            "    Q1ZdsYWBhxc2084DwTHM8QN/TZiyF4fbVYtjvyhG8ydJ37CiG7d9FY1smvNG3iDC\n" +
            "    dwECIQDygv2UOuR1ifLTDo4YxOs2cK3+dAUy6s54mSuGwUeo4QIhAK7SiYDyGwGo\n" +
            "    CwqjOdgOsQkJTGoUkDs8MST0MtmPAAs9AiEAjLT1/nBhJ9V/X3f9eF+g/bhJK+8T\n" +
            "    KSTV4WE1wP0Z3+ECIA9E3DWi77DpWG2JbBfu0I+VfFMXkLFbxH8RxQ8zajGRAiEA\n" +
            "    8Ly1xJ7UW3up25h9aa9SILBpGqWtJlNQgfVKBoabzsU=\n" +
            "    -----END PRIVATE KEY-----";
    val Secretkey = "3zA9HDejj1GnyVK0"

    fun SHA256(text: String): String? {
        val charset = Charsets.UTF_8
        val byteArray = text.toByteArray(charset)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(byteArray)
        return hash.fold("", { str, it -> str + "%02x".format(it)})
    }
    fun updateWalletInfo() {

        payme.getWalletInfo(onSuccess = { jsonObject ->
            println("onSuccess=" + jsonObject.toString())
            val walletBalance = jsonObject.getJSONObject("Wallet")
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
        println("SHA256"+SHA256("291995"))
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
        buttonChangeEnv = findViewById(R.id.buttonChangeENV)
        var configColor = arrayOf<String>("#75255b", "#9d455f")
        buttonChangeEnv.setOnClickListener {
            if(env == Env.DEV){
                env= Env.SANDBOX
                buttonChangeEnv.text = Env.SANDBOX.toString()
            }else{
                env= Env.DEV
                buttonChangeEnv.text = Env.DEV.toString()

            }
        }

        this.payme =
            PayME(this, AppToken, PublicKey, ConnectToken, PrivateKey, configColor, env)
//        payme.pay(this.supportFragmentManager, 100000, "Merchant ghi chú đơn hàng", "", "",
//            onSuccess = { json: JSONObject ->
//                println("onSuccess2222" + json.toString())
//            },
//            onError = { message: String ->
//                println("onError" + message)
//            },
//            onClose = {
//                println("CLOSE")
//            }
//        )
//        payme.getAccountInfo(onSuccess = { jsonObject ->
//            val OpenEWallet = jsonObject.getJSONObject("OpenEWallet")
//            val Init = OpenEWallet.getJSONObject("Init")
//
//            val isExistInMainWallet = Init.optBoolean("isExistInMainWallet")
////            Cần phải Register hay không, hay chỉ Login của người dùng ( false -> gọi register, true -> gọi login)
//
//            val succeeded = Init.optBoolean("succeeded")
////            Kết quả (có tồn tại account hay chưa )
//
//            val kyc = Init.optJSONObject("kyc")
//            if (kyc != null) {
//                val state = kyc.optString("kyc")
////           APPROVED
////            Đã duyệt
////             REJECTED
////            Đã từ chối
////            PENDING
////            Chờ duyệt
////            CANCELED
////            Đã huỷ
////            BANNED
////            Bị ban do sai nhìu lần
//            }
//        }, onError = { jsonObject, code, mesage ->
//
//        })


        buttonReload.setOnClickListener {
            if (ConnectToken.length > 0) {
                updateWalletInfo()
            }

        }
        buttonSubmit.setOnClickListener {
            val tz = TimeZone.getTimeZone("UTC")
            val df: DateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(tz)
            val nowAsISO: String = df.format(Date())
            val dataExample =
                "{\"userId\":\"${inputUserId.text.toString()}\",\"timestamp\":\"${nowAsISO}\",\"phone\":\"${inputPhoneNumber.text.toString()}\"}"
            val connectToken = CryptoAES.encrypt(dataExample, "3zA9HDejj1GnyVK0")
            Log.d("connectToken", connectToken)
            ConnectToken = connectToken
            loading.visibility = View.VISIBLE

            this.payme =
                PayME(
                    this,
                    AppToken,
                    PublicKey,
                    ConnectToken,
                    PrivateKey,
                    configColor,
                    env
                )
            this.payme.initAccount(onSuccess = {jsonObject->
                loading.visibility = View.GONE

                Toast.makeText(
                    context,
                    "Đăng ky ConnectToken thành công",
                    Toast.LENGTH_LONG
                ).show()
            },
            onError = {jsonObject, code, message ->
                loading.visibility = View.GONE

                Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            })
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
                val infoPayment = InfoPayment("PAY", 10000, "Thành công.", 4323, 1, "OpenEWallet")
                payme.pay(this.supportFragmentManager, infoPayment,
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