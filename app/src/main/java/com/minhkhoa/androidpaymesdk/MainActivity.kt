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
import androidx.fragment.app.Fragment
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

val APP_TOKEN_DEFAULT_SANDBOX =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6MTIsImlhdCI6MTYxMzk5MDU5Nn0.donBYzgUyZ2qJwg2TVu43qCQBmYRkbPCsJwdbmLulQ8"
val PUBLIC_KEY_DEFAULT_SANDBOX = "-----BEGIN PUBLIC KEY-----\n" +
        "    MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIXbBm3mTT7Ovlo9LNJK7noshpk8g+zm\n" +
        "    ueFTyrU7muUuXKboD7cg1h/K9zMW4qHFG+3LTo4Cc8fjoqbUm4UILgMCAwEAAQ==\n" +
        "    -----END PUBLIC KEY-----"
val SECRET_KEY_DEFAULT_SANDBOX = "ecd336c200e96265e00e312c6ca28d22"
val PRIVATE_KEY_DEFAULT_SANDBOX = "-----BEGIN RSA PRIVATE KEY-----\n" +
        "    MIIBOQIBAAJAZCKupmrF4laDA7mzlQoxSYlQApMzY7EtyAvSZhJs1NeW5dyoc0XL\n" +
        "    yM+/Uxuh1bAWgcMLh3/0Tl1J7udJGTWdkQIDAQABAkAjzvM9t7kD84PudR3vEjIF\n" +
        "    5gCiqxkZcWa5vuCCd9xLUEkdxyvcaLWZEqAjCmF0V3tygvg8EVgZvdD0apgngmAB\n" +
        "    AiEAvTF57hIp2hkf7WJnueuZNY4zhxn7QNi3CQlGwrjOqRECIQCHfqO53A5rvxCA\n" +
        "    ILzx7yXHzk6wnMcGnkNu4b5GH8usgQIhAKwv4WbZRRnoD/S+wOSnFfN2DlOBQ/jK\n" +
        "    xBsHRE1oYT3hAiBSfLx8OAXnfogzGLsupqLfgy/QwYFA/DSdWn0V/+FlAQIgEUXd\n" +
        "    A8pNN3/HewlpwTGfoNE8zCupzYQrYZ3ld8XPGeQ=\n" +
        "    -----END RSA PRIVATE KEY-----"

val APP_TOKEN_DEFAULT_DEV =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6MTIsImlhdCI6MTYyMDg4MjQ2NH0.DJfi52Dc66IETflV2dQ8G_q4oUAVw_eG4TzrqkL0jLU"
val PUBLIC_KEY_DEFAULT_DEV = "-----BEGIN PUBLIC KEY-----\n" +
        "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJi70XBS5+LtaCrNsrnWlVG6xec+J9M1\n" +
        "DzzvsmDfqRgTIw7RQ94SnEBBcTXhaIAZ8IW7OIWkVU0OXcybQEoLsdUCAwEAAQ==\n" +
        "-----END PUBLIC KEY-----"
val SECRET_KEY_DEFAULT_DEV = "34cfcd29432cdd5feaecb87519046e2d"
val PRIVATE_KEY_DEFAULT_DEV = "-----BEGIN RSA PRIVATE KEY-----\n" +
        "MIIBOgIBAAJBAIA7GmDWkjuOQsx99tACXhOlJ4atsBN0YMPEmKhi9Ewk6bNBPvaX\n" +
        "pRMWjn7c8GfWrFUIVqlrvSlMYxmW/XaATjcCAwEAAQJAKZ6FPj8GcWwIBEUyEWtj\n" +
        "S28EODMxfe785S1u+uA7OGcerljPNOTme6iTuhooO5pB9Q5N7nB2KzoWOADwPOS+\n" +
        "uQIhAN2S5dxxadDL0wllNGeux7ltES0z2UfW9+RViByX/fAbAiEAlCd86Hy6otfd\n" +
        "k9K2YeylsdDwZfmkKq7p27ZcNqVUlBUCIQCxzEfRHdzoZDZjKqfjrzerTp7i4+Eu\n" +
        "KYzf19aSA1ENEwIgAnyXMB/H0ivlYDHNNd+O+GkVX+DMzJqa+kEZUyF7RfECICtK\n" +
        "rkcDyRzI6EtUFG+ALQOUliRRh7aiGXXZYb2KnlKy\n" +
        "-----END RSA PRIVATE KEY-----"

var AppToken: String = ""
var PrivateKey: String = ""
var AppSecretKey: String = ""
var PublicKey: String = ""

val APP_TOKEN_DEFAULT =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6NywiaWF0IjoxNjE0OTExMDE0fQ.PJ0ke0Ky_0BoMPi45Cu803VlR8F3e8kOMoNh9I07AR4"
val PUBLIC_KEY_DEFAULT = "-----BEGIN PUBLIC KEY-----\n" +
        "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJQKJge1dTHz6Qkyz95X92QnsgDqerCB\n" +
        "UzBmt/Qg+5E/oKpw7RBfni3SlCDGotBJH437YvsDBMx8OMCP8ROd7McCAwEAAQ==\n" +
        "-----END PUBLIC KEY-----"
val SECRET_KEY_DEFAULT = "bda4d9de88f37efb93342d8764ac9b84"
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
            this.supportFragmentManager,
            onSuccess = { json: JSONObject? ->
            },
            onError = { jsonObject, code, message ->
                PayME.showError(message)
                println("code"+code+"message"+message)
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
    lateinit var buttonTransfer: Button
    lateinit var buttonScanQr: Button
    lateinit var buttonPayNotAccount: Button
    lateinit var buttonKYC: Button
    lateinit var buttonPay: Button
    lateinit var textView: TextView
    lateinit var inputUserId: EditText
    lateinit var inputPhoneNumber: EditText
    lateinit var moneyDeposit: EditText
    lateinit var moneyPay: EditText
    lateinit var moneyWithdraw: EditText
    lateinit var moneyTransfer: EditText
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
            if (code == ERROR_CODE.ACCOUNT_NOT_ACTIVATED) {
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
        buttonKYC = findViewById(R.id.buttonKYC)
        buttonScanQr = findViewById(R.id.buttonScanQr)
        buttonReload = findViewById(R.id.buttonReload)
        buttonDeposit = findViewById(R.id.buttonDeposit)
        buttonTransfer = findViewById(R.id.buttonTransfer)
        buttonPayNotAccount = findViewById(R.id.buttonPayNotAccount)
        loading = findViewById(R.id.loading)
        buttonWithdraw = findViewById(R.id.buttonWithdraw)
        buttonPay = findViewById(R.id.buttonPay)
        textView = findViewById(R.id.textBalance)
        inputUserId = findViewById(R.id.inputUserId)
        inputPhoneNumber = findViewById(R.id.inputPhoneNumber)
        moneyDeposit = findViewById(R.id.moneyDeposit)
        moneyPay = findViewById(R.id.moneyPay)
        moneyWithdraw = findViewById(R.id.moneyWithdraw)
        moneyTransfer = findViewById(R.id.moneyTransfer)
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
        var configColor = arrayOf<String>("#6756d6", "#6756d6")


        buttonReload.setOnClickListener {
            if (ConnectToken.length > 0) {
                updateWalletInfo()
            }

        }
        buttonScanQr.setOnClickListener {
//            PayME.showError("helooo")

            payme?.scanQR(this.supportFragmentManager,onSuccess = {
                
            },onError = {jsonObject, i, s ->  })
        }
        buttonKYC.setOnClickListener {
            payme?.openKYC(this.supportFragmentManager, onSuccess = {
                println("mo kyc thanh cong")

            }, onError = { jsonObject, i, s ->
                println("code"+i+"message"+s)

                PayME.showError(s)
            })
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

            if (inputPhoneNumber.text.toString().length >= 10 && inputUserId.text.toString().length > 0 && (inputPhoneNumber.text.toString().length == 10 || inputPhoneNumber.text.toString().length == 0) && loading.visibility != View.VISIBLE) {
                val params: MutableMap<String, Any> = mutableMapOf()
                val tz = TimeZone.getTimeZone("UTC")
                val df: DateFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset

                df.setTimeZone(tz)
                val nowAsISO: String = df.format(Date())

                val dataExample =
                    "{\"userId\":\"${inputUserId.text.toString()}\",\"timestamp\":\"${nowAsISO}\",\"phone\":\"${inputPhoneNumber.text.toString()}\"}"

                val connectToken = CryptoAES.encrypt(
                    dataExample,
                    if (env == Env.PRODUCTION) SecretKey else if (env == Env.DEV) SECRET_KEY_DEFAULT_DEV else SECRET_KEY_DEFAULT_SANDBOX
                )
                ConnectToken = connectToken
                loading.visibility = View.VISIBLE
                payme =
                    PayME(
                        this,
                        if (env == Env.PRODUCTION) AppToken else if (env == Env.DEV) APP_TOKEN_DEFAULT_DEV else APP_TOKEN_DEFAULT_SANDBOX,
                        if (env == Env.PRODUCTION) PublicKey else if (env == Env.DEV) PUBLIC_KEY_DEFAULT_DEV else PUBLIC_KEY_DEFAULT_SANDBOX,
                        ConnectToken,
                        if (env == Env.PRODUCTION) PrivateKey else if (env == Env.DEV) PRIVATE_KEY_DEFAULT_DEV else PRIVATE_KEY_DEFAULT_SANDBOX,
                        configColor,
                        LANGUAGES.VN,
                        env,
                        showLog
                    )
                payme?.login(onSuccess = { accountStatus ->
                    println("accountStatus" + accountStatus)
                    if (accountStatus == AccountStatus.NOT_ACTIVATED) {
                        //Tài khoản chưa kich hoạt
                    }
                    if (accountStatus == AccountStatus.NOT_KYC) {
                        //Tài khoản chưa định danh
                    }
                    if (accountStatus == AccountStatus.KYC_APPROVED) {
                        //Tài khoản đã
                    }
                    payme?.getAccountInfo(onSuccess = { data ->
                        println("getAccountInfo" + data)


                    }, onError = { jsonObject, i, s ->

                    })
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
                    this.supportFragmentManager,
                    onSuccess = { json: JSONObject? ->
                    },
                    onError = { jsonObject, code, message ->
                        PayME.showError(message)
                        println("code"+code+"message"+message)

                        if (code == ERROR_CODE.EXPIRED) {
                            walletView.setVisibility(View.GONE)
                            payme?.logout()
                        }
                    })
            }




        }

        buttonDeposit.setOnClickListener {


            val amount = convertInt(moneyDeposit.text.toString())
            payme?.deposit(
                this.supportFragmentManager,
                amount,
                true,
                onSuccess = { json: JSONObject? ->
                },
                onError = { jsonObject, code, message ->
                    PayME.showError(message)
                    println("code"+code+"message"+message)

                    if (code == ERROR_CODE.EXPIRED) {
                        walletView.setVisibility(View.GONE)
                    }
                    if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVATED) {
                        openWallet()
                    }
                })


        }
        buttonWithdraw.setOnClickListener {

            val amount = convertInt(moneyWithdraw.text.toString())

            payme?.withdraw(this.supportFragmentManager,amount, false,
                onSuccess = { json: JSONObject? ->
                },
                onError = { jsonObject, code, message ->
                    println("code"+code+"message"+message)

                    PayME.showError(message)
                    if (code == ERROR_CODE.EXPIRED) {
                        walletView.setVisibility(View.GONE)
                    }
                    if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVATED) {
                        openWallet()
                    }
                })
        }
        buttonTransfer.setOnClickListener {

            val amount = convertInt(moneyTransfer.text.toString())

            payme?.transfer(this.supportFragmentManager,amount, "chuyen tien cho ban nhe", true,
                onSuccess = { json: JSONObject? ->
                    println("onSuccesstransfer")
                },
                onError = { jsonObject, code, message ->
                    PayME.showError(message)
                    println("code"+code+"message"+message)

                    if (code == ERROR_CODE.EXPIRED) {
                        walletView.setVisibility(View.GONE)
                    }
                    if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVATED) {
                        openWallet()
                    }
                })
        }

        buttonPay.setOnClickListener {
            val nextValues = Random.nextInt(0, 100000)

            val amount = convertInt(moneyPay.text.toString())

            val storeId: Long =
                if (env == Env.PRODUCTION) 57956431 else if (env == Env.SANDBOX) 37048160 else 9
            val infoPayment =
                InfoPayment(
                    "PAY",
                    amount,
                    "Nội dung đơn hàng",
                    nextValues.toString(),
                    storeId,
                    "OpenEWallet",
                    ""
                )
            payme?.getPaymentMethods(storeId,
                onSuccess = {list->
                    payme?.pay(this.supportFragmentManager, infoPayment, true,null,
                        onSuccess = { json: JSONObject? ->
                        },
                        onError = { jsonObject, code, message ->

                            if (message != null && message.length > 0) {
                                PayME.showError(message)
                            }
                            if (code == ERROR_CODE.EXPIRED) {
                                walletView.setVisibility(View.GONE)
                            }
                            if (code == ERROR_CODE.ACCOUNT_NOT_KYC || code == ERROR_CODE.ACCOUNT_NOT_ACTIVATED) {
                                openWallet()
                            }
                        }

                    )

            },onError = {jsonObject, code, message ->

            })



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
