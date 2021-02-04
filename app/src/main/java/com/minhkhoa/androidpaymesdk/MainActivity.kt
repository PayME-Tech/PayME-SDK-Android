package com.minhkhoa.androidpaymesdk

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.enums.*
import vn.payme.sdk.model.*
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


val APP_TOKEN = "APP_TOKEN"
val PUBLIC_KEY = "PUBLIC_KEY"
val ON_LOG = "ON_LOG"
val SECRET_KEY = "SECRET_KEY"
val PRIVATE_KEY = "PRIVATE_KEY"
val APP_PHONE = "APP_PHONE"
val APP_USER_ID = "APP_USER_ID"

val APP_TOKEN_DEFAULT_DEV = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6NH0.U60jaOwKcaQ6bUX-6O21RMOoFR_5ZkjpGgj6rus0r60"
val PUBLIC_KEY_DEFAULT_DEV = "-----BEGIN PUBLIC KEY-----\n" +
        "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMwvSFz/mOfxBSVkGeqfRv3oQaCsx9V2\n" +
        "hqdL4Y0PK+r2P+8Jd9pOS61uehd1gsjU1/xMFHWFGKrH6lO8+TSLGukCAwEAAQ==\n" +
        "-----END PUBLIC KEY-----"
val SECRET_KEY_DEFAULT_DEV = "zfQpwE6iHbOeAfgX"
val PRIVATE_KEY_DEFAULT_DEV = "-----BEGIN RSA PRIVATE KEY-----\n" +
        "MIIBOgIBAAJBAIpXByu/SQKImCFT5xTyqLe6zcqDAL/aapD4kYueJiSTFQYzobNx\n" +
        "UA7wRqsljHGfouFXB0gguiPjtoRWgY9XMpMCAwEAAQJALQVFgCcwS3LIj5AOk/Kk\n" +
        "laZlcpJPnCAoriU2uIkvQJdijzoz6baxQDY5xfxwBh7wExmKGvUWxR/qt7ULVf1a\n" +
        "AQIhAMVtGD6vc0zVBuIoWFE2RDYt28WN37p5zC1NtpRebnzjAiEAs2I4WSyUQSzD\n" +
        "P0yR0P+khUI/8oy/iZ/VSASAxzmjkpECIQCTRaZoXIkuL1tLKb14F3saz2q6G/Nh\n" +
        "L6pXwTkJxMe28QIgTiPG7/FfU1SwaG5uRmBVxkapnHp7JPQe8BQmFKKjAkECIBM4\n" +
        "Hel54r1RnKQVUtiLphlZgesayKzrtK2kAgssWKi1\n" +
        "-----END RSA PRIVATE KEY-----"
var AppToken: String =""
var PrivateKey: String = ""
var AppSecretKey: String = ""
var PublicKey: String = ""

val APP_TOKEN_DEFAULT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6NSwiaWF0IjoxNjEyNDMzNDI0fQ.rNl0i-yAEk4MOjcT5OAk7gxnxyAzPQVx9dHCiiH86rM"
val PUBLIC_KEY_DEFAULT = "-----BEGIN PUBLIC KEY----- MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIwGH/c+jndwseq5JCU9SuRSbrT8IMiZ DFyA26aX6xkz42keW2sLRkHo4miAHvc+q91omHJEQXIfcAj2cA1AC6MCAwEAAQ== -----END PUBLIC KEY-----"
val SECRET_KEY_DEFAULT = "27d616faf57ae6db2f052f561de80e83"
val PRIVATE_KEY_DEFAULT = "-----BEGIN RSA PRIVATE KEY-----\n" +
"MIIBOQIBAAJAZCKupmrF4laDA7mzlQoxSYlQApMzY7EtyAvSZhJs1NeW5dyoc0XL\n" +
"yM+/Uxuh1bAWgcMLh3/0Tl1J7udJGTWdkQIDAQABAkAjzvM9t7kD84PudR3vEjIF\n" +
"5gCiqxkZcWa5vuCCd9xLUEkdxyvcaLWZEqAjCmF0V3tygvg8EVgZvdD0apgngmAB\n" +
"AiEAvTF57hIp2hkf7WJnueuZNY4zhxn7QNi3CQlGwrjOqRECIQCHfqO53A5rvxCA\n" +
"ILzx7yXHzk6wnMcGnkNu4b5GH8usgQIhAKwv4WbZRRnoD/S+wOSnFfN2DlOBQ/jK\n" +
"xBsHRE1oYT3hAiBSfLx8OAXnfogzGLsupqLfgy/QwYFA/DSdWn0V/+FlAQIgEUXd\n" +
"A8pNN3/HewlpwTGfoNE8zCupzYQrYZ3ld8XPGeQ=\n" +
"-----END RSA PRIVATE KEY-----"


class MainActivity : AppCompatActivity() {
    companion object {

        var AppToken: String = APP_TOKEN_DEFAULT
        var PrivateKey: String = PRIVATE_KEY_DEFAULT
        var SecretKey: String = SECRET_KEY_DEFAULT
        var PublicKey: String = PUBLIC_KEY_DEFAULT

        var payme: PayME? = null
        lateinit var context: Context
        var env = Env.DEV
        lateinit var paymePref: SharedPreferences
        var showLog: Boolean = false
    }


    fun convertInt(amount: String): Int {
        try {
            return Integer.parseInt(amount)

        } catch (e: Exception) {
            return 0

        }

    }

    fun openWallet() {
        payme?.openWallet(
            onSuccess = { json: JSONObject? ->
            },
            onError = { jsonObject, code, message ->
                PayME.showError(message)
                if (code == ERROR_CODE.EXPIRED) {
                    walletView.setVisibility(View.GONE)
                    payme?.logout()
                }

            })
    }


    lateinit var button: Button
    lateinit var buttonLogin: Button
    lateinit var buttonLogout: Button
    lateinit var buttonReload: ImageView
    lateinit var buttonDeposit: Button
    lateinit var buttonWithdraw: Button
    lateinit var buttonPay: Button
    lateinit var textView: TextView
    lateinit var inputUserId: EditText
    lateinit var inputPhoneNumber: EditText
    lateinit var moneyDeposit: EditText
    lateinit var moneyPay: EditText
    lateinit var moneyWithdraw: EditText
    lateinit var walletView: LinearLayout
    lateinit var buttonSetting: ImageView
    lateinit var spinnerEnvironment: Spinner
    lateinit var loading: ProgressBar

    var ConnectToken: String =
        "qBpM18YIyB15rdpFFfJpzsUBXNkaQ9rnCAN3asLNCrmEgQoS9YlhEVL8iQWT+6hhLSMs/C6uBUXxqD1PN33yhtfisiynwC1TeGV8TuT5bcdsSdgR+il/apjp886i1HJ3"

    fun updateWalletInfo() {

        payme?.getWalletInfo(onSuccess = { jsonObject ->
            println("onSuccess=" + jsonObject.toString())
            val walletBalance = jsonObject.getJSONObject("Wallet")
            val balance = walletBalance.get("balance")
            val decimal = DecimalFormat("#,###")
            textView.text = "${decimal.format(balance)}đ"
        }, onError = { jsonObject, code, message ->
            PayME.showError(message)
            if (code == ERROR_CODE.ACCOUNT_NOT_ACTIVETES) {
                openWallet()
            }
        })

    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_main)
        paymePref = getSharedPreferences("PaymePref", MODE_PRIVATE)

        // Get value of keys
        SecretKey = paymePref.getString(SECRET_KEY, SECRET_KEY_DEFAULT)!!
        PrivateKey = paymePref.getString(PRIVATE_KEY, PRIVATE_KEY_DEFAULT)!!
        PublicKey = paymePref.getString(PUBLIC_KEY, PUBLIC_KEY_DEFAULT)!!
        AppToken = paymePref.getString(APP_TOKEN, APP_TOKEN_DEFAULT)!!
        showLog = paymePref.getBoolean(ON_LOG, false)!!

        val userId = paymePref.getString(APP_USER_ID, "1001")
        val phoneNumber = paymePref.getString(APP_PHONE, "0929000200")

        button = findViewById(R.id.button)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonLogout = findViewById(R.id.buttonLogout)
        buttonSetting = findViewById(R.id.buttonSetting)
        buttonReload = findViewById(R.id.buttonReload)
        buttonDeposit = findViewById(R.id.buttonDeposit)
        loading = findViewById(R.id.loading)
        buttonWithdraw = findViewById(R.id.buttonWithdraw)
        buttonPay = findViewById(R.id.buttonPay)
        textView = findViewById(R.id.textBalance)
        inputUserId = findViewById(R.id.inputUserId)
        inputPhoneNumber = findViewById(R.id.inputPhoneNumber)
        moneyDeposit = findViewById(R.id.moneyDeposit)
        moneyPay = findViewById(R.id.moneyPay)
        moneyWithdraw = findViewById(R.id.moneyWithdraw)
        walletView = findViewById(R.id.walletView)
        spinnerEnvironment = findViewById(R.id.enviromentSpiner)
        inputUserId.setText(userId)
        inputPhoneNumber.setText(phoneNumber)

        inputUserId.addTextChangedListener {
            if (walletView.visibility == View.VISIBLE) {
                walletView.visibility = View.GONE
            }
        }
        inputPhoneNumber.addTextChangedListener {
            if (walletView.visibility == View.VISIBLE) {
                walletView.visibility = View.GONE
            }
        }
        var configColor = arrayOf<String>("#75255b", "#9d455f")


        buttonReload.setOnClickListener {
            if (ConnectToken.length > 0) {
                updateWalletInfo()
            }

        }
        var list = arrayListOf<String>()
        list.add(Env.DEV.toString())
        list.add(Env.PRODUCTION.toString())
        list.add(Env.SANDBOX.toString())
        val spinnerAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)
        spinnerEnvironment.adapter = spinnerAdapter

        spinnerEnvironment.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {

                if (list.get(position) == Env.SANDBOX.toString()) {
                    env = Env.SANDBOX
                }
                if (list.get(position) == Env.DEV.toString()) {
                    env = Env.DEV
                }
                if (list.get(position) == Env.PRODUCTION.toString()) {
                    env = Env.PRODUCTION
                }
                walletView.visibility = View.GONE
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        })
        buttonLogin.setOnClickListener {

            if (inputUserId.text.toString().length > 0 && (inputPhoneNumber.text.toString().length == 10 || inputPhoneNumber.text.toString().length == 0) && loading.visibility != View.VISIBLE) {
                val params: MutableMap<String, Any> = mutableMapOf()
                val tz = TimeZone.getTimeZone("UTC")
                val df: DateFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset

                df.setTimeZone(tz)
                val nowAsISO: String = df.format(Date())

                val dataExample =
                    "{\"userId\":\"${inputUserId.text.toString()}\",\"timestamp\":\"${nowAsISO}\",\"phone\":\"${inputPhoneNumber.text.toString()}\"}"

                val connectToken = CryptoAES.encrypt(dataExample,if(env==Env.PRODUCTION) SecretKey else SECRET_KEY_DEFAULT_DEV)
                ConnectToken = connectToken
                loading.visibility = View.VISIBLE
                payme =
                    PayME(
                        this,
                        if(env==Env.PRODUCTION) AppToken else APP_TOKEN_DEFAULT_DEV,
                        if(env==Env.PRODUCTION) PublicKey else PUBLIC_KEY_DEFAULT_DEV ,
                    ConnectToken  ,
                        if(env==Env.PRODUCTION) PrivateKey else PRIVATE_KEY_DEFAULT_DEV  ,
                        configColor,
                        LANGUAGES.VN,
                        env,
                        showLog
                    )
                payme?.login(onSuccess = { accountStatus ->
                    if(accountStatus == AccountStatus.NOT_ACTIVED){
                        //Tài khoản chưa kich hoạt
                    }
                    if(accountStatus == AccountStatus.NOT_KYC){
                        //Tài khoản chưa định danh
                    }
                    if(accountStatus == AccountStatus.KYC_OK){
                        //Tài khoản đã
                    }
                    loading.visibility = View.GONE
                    paymePref.edit().putString(APP_USER_ID, inputUserId.text.toString()).commit()
                    paymePref.edit().putString(APP_PHONE, inputPhoneNumber.text.toString())
                        .commit()
                    walletView.setVisibility(View.VISIBLE)
                    Toast.makeText(
                        context,
                        "Đăng ky ConnectToken thành công",
                        Toast.LENGTH_LONG
                    ).show()
                },
                    onError = { jsonObject, code, message ->
                        loading.visibility = View.GONE
                        PayME.showError(message)

                    })


            }


        }



        buttonLogout.setOnClickListener {
            if (payme != null) {
                payme?.logout()
                inputPhoneNumber.text = null
                inputUserId.text = null
                walletView.setVisibility(View.GONE)
            }

        }

        button.setOnClickListener {
            if (ConnectToken.length > 0) {
                payme?.openWallet(
                    onSuccess = { json: JSONObject? ->
                    },
                    onError = { jsonObject, code, message ->
                        PayME.showError(message)
                        if (code == ERROR_CODE.EXPIRED) {
                            walletView.setVisibility(View.GONE)
                            payme?.logout()
                        }
                    })
            }


        }

        buttonDeposit.setOnClickListener {


            val amount = convertInt(moneyDeposit.text.toString())
            payme?.deposit(amount, null, "",
                onSuccess = { json: JSONObject? ->
                },
                onError = { jsonObject, code, message ->
                    PayME.showError(message)
                    if (code == ERROR_CODE.EXPIRED) {
                        walletView.setVisibility(View.GONE)
                        payme?.logout()
                    }
                    if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVETES) {
                        openWallet()
                    }
                })


        }
        buttonWithdraw.setOnClickListener {

            val amount = convertInt(moneyWithdraw.text.toString())

            payme?.withdraw(amount, null, "",
                onSuccess = { json: JSONObject? ->
                },
                onError = { jsonObject, code, message ->
                    PayME.showError(message)
                    if (code == ERROR_CODE.EXPIRED) {
                        walletView.setVisibility(View.GONE)
                        payme?.logout()
                    }
                    if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVETES) {
                        openWallet()
                    }
                })
        }
        buttonPay.setOnClickListener {
            val nextValues = List(10) { Random.nextInt(0, 100000) }

            val amount = convertInt(moneyPay.text.toString())
            val infoPayment =
                InfoPayment("PAY", amount, "Nội dung đơn hàng", nextValues.toString(), 4, "OpenEWallet","")
            payme?.pay(this.supportFragmentManager, infoPayment,true,
                onSuccess = { json: JSONObject? ->
                },
                onError = { jsonObject, code, message ->
                    if(message!=null && message.length>0){
                        PayME.showError(message)
                    }
                    if (code == ERROR_CODE.EXPIRED) {
                        walletView.setVisibility(View.GONE)
                        payme?.logout()
                    }
                    if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVETES) {
                        openWallet()
                    }
                }
            )
        }

        buttonSetting.setOnClickListener {
            val intent = Intent(this, SettingAcitivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("TESTAA AppToken", AppToken)
        Log.d("TESTAA PublicKey", PublicKey)
        Log.d("TESTAA PrivateKey", PrivateKey)
        Log.d("TESTAA SecretKey", SecretKey)
    }
}
