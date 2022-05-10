package com.minhkhoa.androidpaymesdk

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
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
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6OTUsImlhdCI6MTY1MTczMjM0Nn0.TFsg9wizgtWa7EbGzrjC2Gn55TScsJzKGjfeN78bhlg"
val PUBLIC_KEY_DEFAULT_SANDBOX = "-----BEGIN PUBLIC KEY-----\n" +
        "    MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAId28RoBckMTTPqVCC3c1f+fH+BbdVvv\n" +
        "    wDkSf+0zmaUlCFyQpassU3+8CvM6QbeYSYGWp1YIwGqg2wTF94zT4eECAwEAAQ==\n" +
        "    -----END PUBLIC KEY-----"
val SECRET_KEY_DEFAULT_SANDBOX = "b5d8cf6c30d9cb4a861036bdde44c137"
val PRIVATE_KEY_DEFAULT_SANDBOX = "-----BEGIN RSA PRIVATE KEY-----\n" +
        "    MIIBOwIBAAJBAMEKxNcErAKSzmWcps6HVScLctpdDkBiygA3Pif9rk8BoSU0BYAs\n" +
        "    G5pW8yRmhCwVMRQq+VhJNZq+MejueSBICz8CAwEAAQJBALfa29K1/mWNEMqyQiSd\n" +
        "    vDotqzvSOQqVjDJcavSHpgZTrQM+YzWwMKAHXLABYCY4K0t01AjXPPMYBueJtFeA\n" +
        "    i3ECIQDpb6Fp0yGgulR9LHVcrmEQ4ZTADLEASg+0bxVjv9vkWwIhANOzlw9zDMRr\n" +
        "    i/5bwttz/YBgY/nMj7YIEy/v4htmllntAiA5jLDRoyCOPIGp3nUMpVz+yW5froFQ\n" +
        "    nfGjPSOb1OgEMwIhAI4FhyvoJQKIm8wyRxDuSXycLbXhU+/sjuKz7V4wfmEpAiBb\n" +
        "    PmELTX6BquyCs9jUzoPxDWKQSQGvVUwcWXtpnYxSvQ==\n" +
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
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6NSwiaWF0IjoxNjEyNDMzNDI0fQ.rNl0i-yAEk4MOjcT5OAk7gxnxyAzPQVx9dHCiiH86rM"
val PUBLIC_KEY_DEFAULT = "-----BEGIN PUBLIC KEY-----\n" +
        "    MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIwGH/c+jndwseq5JCU9SuRSbrT8IMiZ\n" +
        "    DFyA26aX6xkz42keW2sLRkHo4miAHvc+q91omHJEQXIfcAj2cA1AC6MCAwEAAQ==\n" +
        "    -----END PUBLIC KEY-----"
val SECRET_KEY_DEFAULT = "27d616faf57ae6db2f052f561de80e83"
val PRIVATE_KEY_DEFAULT = "-----BEGIN RSA PRIVATE KEY-----\n" +
        "    MIIBOQIBAAJAZCKupmrF4laDA7mzlQoxSYlQApMzY7EtyAvSZhJs1NeW5dyoc0XL\n" +
        "    yM+/Uxuh1bAWgcMLh3/0Tl1J7udJGTWdkQIDAQABAkAjzvM9t7kD84PudR3vEjIF\n" +
        "    5gCiqxkZcWa5vuCCd9xLUEkdxyvcaLWZEqAjCmF0V3tygvg8EVgZvdD0apgngmAB\n" +
        "    AiEAvTF57hIp2hkf7WJnueuZNY4zhxn7QNi3CQlGwrjOqRECIQCHfqO53A5rvxCA\n" +
        "    ILzx7yXHzk6wnMcGnkNu4b5GH8usgQIhAKwv4WbZRRnoD/S+wOSnFfN2DlOBQ/jK\n" +
        "    xBsHRE1oYT3hAiBSfLx8OAXnfogzGLsupqLfgy/QwYFA/DSdWn0V/+FlAQIgEUXd\n" +
        "    A8pNN3/HewlpwTGfoNE8zCupzYQrYZ3ld8XPGeQ=\n" +
        "    -----END RSA PRIVATE KEY-----"


class MainActivity : AppCompatActivity() {
    companion object {

        var AppToken: String = APP_TOKEN_DEFAULT
        var PrivateKey: String = PRIVATE_KEY_DEFAULT
        var SecretKey: String = SECRET_KEY_DEFAULT
        var PublicKey: String = PUBLIC_KEY_DEFAULT

        var payme: PayME? = null
        lateinit var context: Context
        var env = Env.SANDBOX
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
    lateinit var buttonPayQR: Button
    lateinit var buttonScanQR: Button
    lateinit var buttonOpenHistory: Button
    lateinit var buttonWithdraw: Button
    lateinit var buttonTransfer: Button
    lateinit var buttonScanQr: Button
    lateinit var buttonPayNotAccount: Button
    lateinit var buttonKYC: Button
    lateinit var buttonPay: Button
    lateinit var buttonOpenService: Button
    lateinit var textView: TextView
    lateinit var inputUserId: EditText
    lateinit var inputQRString: EditText
    lateinit var inputPhoneNumber: EditText
    lateinit var inputUsername: EditText
    lateinit var moneyDeposit: EditText
    lateinit var moneyPay: EditText
    lateinit var moneyWithdraw: EditText
    lateinit var moneyTransfer: EditText
    lateinit var walletView: LinearLayout
    lateinit var buttonSetting: ImageView
    lateinit var spinnerEnvironment: Spinner
    lateinit var spinnerLanguage: Spinner
    lateinit var spinnerPayCode: Spinner
    lateinit var spinnerPayQRPayCode: Spinner
    lateinit var spinnerScanQRPayCode: Spinner
    lateinit var spinnerService: Spinner
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
        buttonOpenHistory = findViewById(R.id.buttonOpenHistory)
        buttonPayNotAccount = findViewById(R.id.buttonPayNotAccount)
        loading = findViewById(R.id.loading)
        buttonWithdraw = findViewById(R.id.buttonWithdraw)
        buttonPay = findViewById(R.id.buttonPay)
        buttonScanQR = findViewById(R.id.buttonScanQr)
        buttonPayQR = findViewById(R.id.buttonPayQR)
        textView = findViewById(R.id.textBalance)
        inputUserId = findViewById(R.id.inputUserId)
        inputQRString = findViewById(R.id.inputPayQR)
        inputUsername = findViewById(R.id.username)
        inputPhoneNumber = findViewById(R.id.inputPhoneNumber)
        moneyDeposit = findViewById(R.id.moneyDeposit)
        moneyPay = findViewById(R.id.moneyPay)
        moneyWithdraw = findViewById(R.id.moneyWithdraw)
        moneyTransfer = findViewById(R.id.moneyTransfer)
        walletView = findViewById(R.id.walletView)
        spinnerEnvironment = findViewById(R.id.enviromentSpiner)
        spinnerPayCode = findViewById(R.id.payCodeSpiner)
        spinnerPayQRPayCode = findViewById(R.id.payQrPayCodeSpinner)
        spinnerScanQRPayCode= findViewById(R.id.scanQrPayCodeSpinner)
        spinnerLanguage = findViewById(R.id.languageSpinner)
        spinnerService = findViewById(R.id.serviceSpinner)
        buttonOpenService = findViewById(R.id.buttonOpenService)
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
        buttonOpenHistory.setOnClickListener{
            payme?.openHistory(supportFragmentManager,onSuccess = {

            },onError = {jsonObject, i, s ->
                PayME.showError(s)
            })
        }
        buttonPayQR.setOnClickListener {
            payme?.payQRCode(supportFragmentManager,inputQRString.text.toString(),spinnerPayQRPayCode.selectedItem.toString(),true,onSuccess = {

            },onError = {jsonObject, i, s ->
            PayME.showError(s)
            })
        }
        buttonScanQr.setOnClickListener {
            payme?.scanQR(this.supportFragmentManager,spinnerScanQRPayCode.selectedItem.toString(),onSuccess = {

            },onError = {jsonObject, i, s ->  })
        }
        buttonOpenService.setOnClickListener {
            payme?.openService(supportFragmentManager,Service(spinnerService.selectedItem.toString(),""),onSuccess = {},onError = {jsonObject, i, s ->
                PayME.showError(s)
            })
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
        spinnerLanguage.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                if(payme!=null){
                    payme?.setLanguage(context,if(spinnerLanguage.selectedItem.toString() == LANGUAGES.VI.toString()) LANGUAGES.VI else LANGUAGES.EN)
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        })

        buttonLogin.setOnClickListener {
            if (spinnerEnvironment.selectedItem.toString() == Env.SANDBOX.toString()) {
                env = Env.SANDBOX
            }
            if (spinnerEnvironment.selectedItem.toString() == Env.DEV.toString()) {
                env = Env.DEV
            }
            if (spinnerEnvironment.selectedItem.toString() == Env.PRODUCTION.toString()) {
                env = Env.PRODUCTION
            }
            if (spinnerEnvironment.selectedItem.toString() == Env.STAGING.toString()) {
                env = Env.STAGING
            }

            if (inputPhoneNumber.text.toString().length >= 10 && inputUserId.text.toString().length > 0 && (inputPhoneNumber.text.toString().length == 10 || inputPhoneNumber.text.toString().length == 0) && loading.visibility != View.VISIBLE) {
                val params: MutableMap<String, Any> = mutableMapOf()
                val tz = TimeZone.getTimeZone("UTC")
                val df: DateFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset

                df.setTimeZone(tz)
                val nowAsISO: String = df.format(Date())
                val dataE = "{\"userId\":\"${inputUserId.text.toString()}\",\"phone\":\"${inputPhoneNumber.text.toString()}\",\"timestamp\":\"${nowAsISO}\",\"kycInfo\":{\"fullname\":\"Lai Van Hieu\",\"gender\":\"MALE\",\"birthday\":\"1995-01-20T06:53:07.621Z\",\"address\":\"31 vu tung\",\"identifyType\":\"CMND\",\"identifyNumber\":\"String\",\"issuedAt\":\"2012-01-20T06:53:07.621Z\",\"placeOfIssue\":\"Hai Duong\",\"video\":\"https://sbx-static.payme.vn//2020/10/28/Co-29vnK6.mp4\",\"face\":\"https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg\",\"image\":{\"front\":\"https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg\",\"back\":\"https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg\"}}}"
                println("dataE:"+dataE)
                val dataExample =
                    "{\"userId\":\"${inputUserId.text.toString()}\",\"timestamp\":\"${nowAsISO}\",\"phone\":\"${inputPhoneNumber.text.toString()}\"}"

                val connectToken = CryptoAES.encrypt(
                    dataExample,
                    if (env == Env.PRODUCTION || env == Env.STAGING) SecretKey else if (env == Env.DEV) SECRET_KEY_DEFAULT_DEV else SECRET_KEY_DEFAULT_SANDBOX
                )
                ConnectToken = connectToken
                loading.visibility = View.VISIBLE
                println("env"+env.toString())
                payme =
                    PayME(
                        this,
                        if (env == Env.PRODUCTION || env == Env.STAGING) AppToken else if (env == Env.DEV) APP_TOKEN_DEFAULT_DEV else APP_TOKEN_DEFAULT_SANDBOX,
                        if (env == Env.PRODUCTION || env == Env.STAGING) PublicKey else if (env == Env.DEV) PUBLIC_KEY_DEFAULT_DEV else PUBLIC_KEY_DEFAULT_SANDBOX,
                        ConnectToken,
                        if (env == Env.PRODUCTION || env == Env.STAGING) PrivateKey else if (env == Env.DEV) PRIVATE_KEY_DEFAULT_DEV else PRIVATE_KEY_DEFAULT_SANDBOX,
                        configColor,
                        if(spinnerLanguage.selectedItem.toString() == LANGUAGES.VI.toString()) LANGUAGES.VI else LANGUAGES.EN,
                        env,
                        showLog
                    )
                payme?.login(onSuccess = { accountStatus ->
//                    println("accountStatus" + accountStatus)
                    if (accountStatus == AccountStatus.NOT_ACTIVATED) {
                        //Tài khoản chưa kich hoạt
                    }
                    if (accountStatus == AccountStatus.NOT_KYC) {
                        //Tài khoản chưa định danh
                    }
                    if (accountStatus == AccountStatus.KYC_REVIEW) {
                        //Tài khoản đã gửi thông tin định danh dang chờ duyệt
                    }
                    if (accountStatus == AccountStatus.KYC_REJECTED) {
                        //Yêu cầu đinh danh bị từ
                    }
                    if (accountStatus == AccountStatus.KYC_APPROVED) {
                        updateWalletInfo()
                        //Tài khoản đã định danh
                    }
                    payme?.getAccountInfo(onSuccess = { data ->
                        println("getAccountInfo" + data)


                    }, onError = { jsonObject, i, s ->

                    })
                    payme?.getSupportedServices(onSuccess = {arrayList ->
                        var list = arrayListOf<String>()

                        arrayList?.forEach { service ->
                            list.add(service.code)
                        }
                        val spinnerAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)
                        spinnerService.adapter = spinnerAdapter

                    },onError = {jsonObject, i, s ->

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
                        println("message"+message)

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
            val nextValues = Random.nextInt(0, 1000000000)

            val amount = convertInt(moneyPay.text.toString())

            val storeId: Long? =
                if (env == Env.PRODUCTION ||env == Env.STAGING  ) 57956431 else if (env == Env.SANDBOX) null else 9
            val infoPayment =
                InfoPayment(
                    "PAY",
                    amount,
                    "Nội dung đơn hàng",
                    nextValues.toString(),
                    storeId,
                    "OpenEWallet",
                    "",
                    null
                )
                    payme?.pay(this.supportFragmentManager, infoPayment, true,spinnerPayCode.selectedItem.toString(),
                        onSuccess = { json: JSONObject? ->
                            println("jsononSuccess"+json)
                        },
                        onError = { jsonObject, code, message ->
                            println("jsononError"+code)

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





        }

        buttonSetting.setOnClickListener {
            val intent = Intent(this, SettingAcitivity::class.java)
            startActivity(intent)
        }
    }

}
