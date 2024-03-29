package vn.payme.sdk

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.spongycastle.jce.provider.BouncyCastleProvider
import vn.payme.sdk.api.AccountApi
import vn.payme.sdk.api.ENV_API
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.enums.*
import vn.payme.sdk.evenbus.*
import vn.payme.sdk.kyc.CameraKycPopup
import vn.payme.sdk.model.*
import vn.payme.sdk.payment.*
import vn.payme.sdk.store.Config
import vn.payme.sdk.store.PaymentInfo
import vn.payme.sdk.store.Store
import vn.payme.sdk.store.UserInfo
import java.security.Security
import java.util.*
import kotlin.collections.ArrayList


public class PayME {
    private val payMEOpenSDKPopup = PayMEOpenSDKPopup()
    companion object {
        internal lateinit var context: Context
        internal lateinit var onSuccess: ((JSONObject?) -> Unit)
        internal lateinit var onError: (JSONObject?, Int, String?) -> Unit
        internal lateinit var fragmentManager: FragmentManager
        internal lateinit var fragmentManagerScan: FragmentManager
        internal var enableSetting: Boolean = false
        fun showError(message: String?) {
            if (message != null && message != "null" && message != "") {
                Toasty.error(PayME.context, message, Toast.LENGTH_SHORT, true).show();
            }
        }
        fun onActivityResult(data: Bitmap) {
            EventBus.getDefault().post(CheckActivityResult(data))
        }
    }

    fun scanQR(
        fragmentManager: FragmentManager,
        payCode: String,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ): Unit {
        setLanguage(PayME.context, Store.config.language)
        val checkAccount = CheckAccount()
        if (checkAccount.check(RULE_CHECK_ACCOUNT.LOGGIN, onError)) {
            if (!((payCode == PAY_CODE.PAYME) ||
                        (payCode == PAY_CODE.ATM) ||
                        (payCode == PAY_CODE.MANUAL_BANK) ||
                        (payCode == PAY_CODE.VIET_QR) ||
                        (payCode == PAY_CODE.CREDIT))
            ) {
                onError(
                    null,
                    ERROR_CODE.PAYMENT_ERROR,
                    PayME.context.getString(R.string.method_not_supported)
                )
                return
            }

            onSuccess(null)
            openScanQR(fragmentManager, payCode, onSuccess = {
            },
                onError = { _, _, s ->
                    showError(s)
                }
            )
        }
    }

    internal fun openScanQR(
        fragmentManager: FragmentManager,
        payCode: String,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {

        PayME.fragmentManager = fragmentManager
        PayME.onSuccess = onSuccess
        PayME.onError = onError
        val scanQR = ScanQR()
        val bundle = Bundle()
        bundle.putString("payCode", payCode)
        scanQR.arguments = bundle
        scanQR.show(fragmentManager, null)
    }

    fun payQRCode(
        fragmentManager: FragmentManager,
        qr: String,
        payCode: String,
        isShowResultUI: Boolean,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        setLanguage(PayME.context, Store.config.language)
        val checkAccount = CheckAccount()
        if (checkAccount.check(RULE_CHECK_ACCOUNT.LOGGIN, onError)) {
            if (!((payCode == PAY_CODE.PAYME) ||
                        (payCode == PAY_CODE.ATM) ||
                        (payCode == PAY_CODE.MANUAL_BANK) ||
                        (payCode == PAY_CODE.VIET_QR) ||
                        (payCode == PAY_CODE.VN_PAY) ||
                        (payCode == PAY_CODE.CREDIT))
            ) {
                onError(
                    null,
                    ERROR_CODE.PAYMENT_ERROR,
                    PayME.context.getString(R.string.method_not_supported)
                )
                return
            }
            val paymentApi = PaymentApi()
            val loading = SpinnerDialog()
            loading.show(fragmentManager, null)
            paymentApi.postCheckDataQr(qr,
                onSuccess = { jsonObject ->
                    loading.dismiss()
                    val openEWallet = jsonObject.optJSONObject("OpenEWallet")
                    val payment = openEWallet.optJSONObject("Payment")
                    val detect = payment.optJSONObject("DetectV2")
                    val qrInfo = detect.optJSONObject("qrInfo")
                    val succeeded = detect.optBoolean("succeeded")
                    val message = detect.optString("message")
                    val typename = qrInfo.optString("__typename")
                    val amount = qrInfo.optInt("amount")

                    val checkNullLong = 0
                    if (!succeeded) {
                        loading.dismiss()
                        onError(null, ERROR_CODE.PAYMENT_ERROR, message)
                    } else if (typename == "DefaultQR") {
                        val action = qrInfo.optString("action")
//                        val message = qrInfo.optString("message")
                        val note = qrInfo.optString("note")
                        val amount = qrInfo.optInt("amount")
                        val orderId = qrInfo.optString("orderId")
                        val storeId = qrInfo.optLong("storeId")
                        val userName = qrInfo.optString("userName")
                        val type = qrInfo.optString("type")

                        val infoPayment =
                            InfoPayment(
                                action,
                                amount,
                                note,
                                orderId,
                                if (storeId == checkNullLong.toLong()) null else storeId,
                                type,
                                Store.paymentInfo.extraData,
                                if (userName != "null") userName else null
                            )
                        val paymeSDK = PayME()
                        paymeSDK.pay(
                            fragmentManager,
                            infoPayment,
                            isShowResultUI,
                            payCode,
                            onSuccess,
                            onError
                        )
                    } else if (typename == "VietQR") {
                        Log.d("test","Lên web")
                        val extraData = qrInfo.optString("extraData")
                        val note = qrInfo.optString("note")
                        transferInSDK(
                            fragmentManager,
                            amount,
                            note,
                            !isShowResultUI,
                            onSuccess,
                            onError,
                            extraData
                        )
                    } else {
                        loading.dismiss()
                        onError(null, ERROR_CODE.PAYMENT_ERROR, message)
                    }
                },
                onError = { jsonObject, code, message ->
                    loading.dismiss()
                    onError(jsonObject, code, message)
                }
            )
        }
    }

    internal fun payQRCodeInSDK(fragmentManager: FragmentManager, qr: String, payCode: String) {
        val paymentApi = PaymentApi()
        val loading = SpinnerDialog()
        loading.show(fragmentManager, null)
        paymentApi.postCheckDataQr(qr,
            onSuccess = { jsonObject ->
                loading.dismiss()
                val openEWallet = jsonObject.optJSONObject("OpenEWallet")
                val payment = openEWallet.optJSONObject("Payment")
                val detect = payment.optJSONObject("DetectV2")
                val qrInfo = detect.optJSONObject("qrInfo")
                val succeeded = detect.optBoolean("succeeded")
                val typename = qrInfo.optString("__typename")
                val amount = qrInfo.optInt("amount")

                val checkNullLong = 0
                if (!succeeded) {
                    loading.dismiss()
                    var popup = SearchQrResultPopup()
                    popup.show(fragmentManager, "ModalBottomSheet")
                } else if (typename == "DefaultQR") {
                    val action = qrInfo.optString("action")
//                    val message = qrInfo.optString("message")
                    val note = qrInfo.optString("note")
                    val amount = qrInfo.optInt("amount")
                    val orderId = qrInfo.optString("orderId")
                    val storeId = qrInfo.optLong("storeId")
                    val userName = qrInfo.optString("userName")
                    val type = qrInfo.optString("type")

                    val infoPayment =
                        InfoPayment(
                            action,
                            amount,
                            note,
                            orderId,
                            if (storeId == checkNullLong.toLong()) null else storeId,
                            type,
                            Store.paymentInfo.extraData,
                            if (userName != "null") userName else null

                        )
                    val paymeSDK = PayME()
                    paymeSDK.payInSDK(
                        fragmentManager,
                        payCode,
                        infoPayment,
                    )
                } else {
                    val extraData = qrInfo.optString("extraData")
                    val note = qrInfo.optString("note")
                    transferInSDK(
                        fragmentManager,
                        amount,
                        note,
                        true,
                        onSuccess,
                        onError,
                        extraData
                    )
                }
            },
            onError = { jsonObject, code, message ->
                Log.d("test","lỗi")
                Log.d("test",message.toString())
                loading.dismiss()
                showError(message)

            }
        )
    }

    @SuppressWarnings("deprecation")
    fun setLanguage(context: Context, language: LANGUAGES) {
        println("language.toString().toLowerCase() : " + language.toString().toLowerCase())
        Store.config.language = language
        val config = context.resources.configuration
        val lang = language.toString().toLowerCase() // your language code
        val locale = Locale(lang)
        Locale.setDefault(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            config.setLocale(locale)
        else
            config.locale = locale

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            context.createConfigurationContext(config)
        context.getResources().updateConfiguration(
            config,
            context.getResources().getDisplayMetrics()
        )
    }

    constructor(
        context: Context,
        appToken: String,
        publicKey: String,
        connectToken: String,
        appPrivateKey: String,
        configColor: Array<String>,
        language: LANGUAGES,
        env: Env,
        showLog: Boolean
    ) {
        PayME.context = context
        Store.config = Config(
            appPrivateKey,
            appToken,
            publicKey,
            connectToken,
            showLog,
            env,
            configColor,
            language,
        )
        Store.paymentInfo = PaymentInfo(
            null, 0, "", null, null, null, "", arrayListOf(),
            arrayListOf(), null, true, ""
        )
        Store.userInfo = UserInfo(0, false, false, false, "", null)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        ENV_API.updateEnv()
    }

    constructor() {

    }

    fun close() {
        EventBus.getDefault()
            .post(MyEven(TypeCallBack.onExpired, ""))
        EventBus.getDefault()
            .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT_NOT_CALL_BACK, null))
    }

    public fun onForgotPassword() {
        openWalletActivity(Action.FORGOT_PASSWORD, 0, "", null, null, onSuccess, onError)
    }

    internal fun checkRegisterClient(
        onSuccess: () -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val accountApi = AccountApi()
        val pref = PayME.context.getSharedPreferences("PayME_SDK", Context.MODE_PRIVATE)
        val clientId = pref.getString("clientId", "")
        val dataRegisterClientInfo = pref.getString("dataRegisterClientInfo", "")
        if (clientId?.length == 0 || !dataRegisterClientInfo.equals(
                Store.config.clientInfo?.getClientInfo().toString() + Store.config.env.toString()
            )
        ) {
            accountApi.registerClient(
                onSuccess = { jsonObject ->
                    val Client = jsonObject?.optJSONObject("Client")
                    val Register = Client?.optJSONObject("Register")
                    val clientId = Register?.optString("clientId")
                    Store.config.clientId = clientId.toString()
                    pref.edit().putString("clientId", clientId.toString()).commit()
                    pref.edit().putString(
                        "dataRegisterClientInfo",
                        Store.config.clientInfo?.getClientInfo()
                            .toString() + Store.config.env.toString()
                    ).commit()
                    onSuccess()
                },
                onError
            )
        } else {
            Store.config.clientId = clientId.toString()
            onSuccess()
        }
    }

    internal fun getSetting(
        onSuccess: () -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val paymentApi = PaymentApi()
        paymentApi.getSettings(
            onSuccess = { jsonObject ->
                val Setting = jsonObject.getJSONObject("Setting")
                val configs = Setting.getJSONArray("configs")
                for (i in 0 until configs.length()) {
                    val config = configs.optJSONObject(i)
                    val key = config.optString("key")
                    val valueString = config.optString("value")
                    if (key == "kyc.mode.enable") {
                        val value = JSONObject(valueString)
                        val identifyImg = value.optBoolean("identifyImg")
                        val kycVideo = value.optBoolean("kycVideo")
                        val faceImg = value.optBoolean("faceImg")
                        Store.config.enlableKycFace = faceImg
                        Store.config.enlableKycIdentify = identifyImg
                        Store.config.enlableKycVideo = kycVideo
                    }

                    if (key == "credit.sacom.auth.link" && valueString != "null") {
                        Store.config.creditSacomAuthLink = valueString
                    }
                    if (key == "sdk.scanModule.enable") {
                        val json = JSONObject(valueString)
                        val array = json.optJSONArray("appId")
                        if (array != null) {
                            for (index in 0 until array.length()){
                                val appId = array.getInt(index);
                                if (Store.config.appID == appId) {
                                    Store.config.scanModuleEnable = true
                                }
                            }
                        }
                    }
                    if (key == "sdk.web.secretKey" && valueString != "null") {
                        Store.config.sdkWebSecretKey = valueString
                    }
//                    if (key == "limit.param.amount.all" && valueString != "null") {
//                        println("valueString"+valueString)
//                        val value = JSONObject(valueString)
//                        val max = Integer.parseInt(value.optString("max"))
//                        val min = Integer.parseInt(value.optString("min"))
//                        Store.config.limitPayment = MaxminPayment(min, max)
//                        println("limit.param.amount. all: "+min)
//                        println("limit.param.amount.all: "+Store.config.limitPayment.min)
//                    }
                    if (key == "limit.param.amount.payment" && valueString != "null") {
                        val value = JSONObject(valueString)
                        val max = Integer.parseInt(value.optString("max"))
                        val min = Integer.parseInt(value.optString("min"))
                        Store.config.limitPayment = MaxminPayment(min, max)
                        println("limit.param.amount.payment: $min")
                        println("limit.param.amount.payment: " + Store.config.limitPayment.min)
                    }
                    if (key == "limit.payment.password" && valueString != "null") {
                        val value = JSONObject(valueString)
                        val max = Integer.parseInt(value.optString("max"))
                        val min = Integer.parseInt(value.optString("min"))
                        Store.config.limitPaymentPassword = MaxminPayment(min, max)
                        println("limit.payment.passwordt: $min")
                        println("limit.payment.password" + Store.config.limitPaymentPassword.max)
                    }
                    println("Store.config.limitPayment: " + Store.config.limitPayment.min)
                    if (key == "service.main.visible" && valueString != "null") {
                        val value = JSONObject(valueString)
                        Store.paymentInfo.listService = arrayListOf()
                        val listService = value.optJSONArray("listService")
                        for (i in 0 until listService.length()) {
                            val json = listService.optJSONObject(i)
                            if (json !== null) {
                                val code = json.optString("code")
                                val description = json.optString("description")
                                val disable = json.optBoolean("disable")
                                val enable = json.optBoolean("enable")
                                if (enable && !disable) {
                                    Store.paymentInfo.listService.add(Service(code, description))
                                }
                            }
                        }
                    }
                }
                onSuccess()
            },
            onError
        )

    }

    public fun login(
        onSuccess: (AccountStatus) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        PayME.onError = onError
        getSetting(onSuccess = {
            checkRegisterClient(onSuccess = {
                loginAccount(onSuccess, onError)
            }, onError)

        }, onError)
    }


    fun openWallet(
        fragmentManager: FragmentManager,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        PayME.fragmentManager = fragmentManager
        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN, onError)) {
            Store.config.closeWhenDone = false
            openWalletActivity(Action.OPEN, 0, "", null, null, onSuccess, onError)
        }
    }

    fun openKYC(
        fragmentManager: FragmentManager,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        setLanguage(PayME.context, Store.config.language)

        PayME.onSuccess = onSuccess
        PayME.onError = onError
        PayME.fragmentManager = fragmentManager
        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE, onError)) {
            getAccountInfo(onSuccess = { jsonObject ->
                val Account = jsonObject.getJSONObject("Account")
                val kyc = Account.optJSONObject("kyc")
                if (kyc != null) {
                    val state = kyc.getString("state")
                    if (state == "APPROVED") {
                        Store.userInfo.accountKycSuccess = true
                        onError(kyc, ERROR_CODE.SYSTEM, "Tài khoản đã kyc")
                    } else if (state == "REJECTED" || state == "CANCELED" || state == "null") {
                        val details = kyc.optJSONObject("details")
                        var stateFace = "REJECTED"
                        var stateImage = "REJECTED"
                        var stateVideo = "REJECTED"
                        if (details != null) {
                            val face = details.optJSONObject("face")
                            val image = details.optJSONObject("image")
                            val video = details.optJSONObject("video")

                            if (face != null) {
                                stateFace = face.optString("state")
                            }
                            if (image != null) {
                                stateImage = image.optString("state")
                            }
                            if (video != null) {
                                stateVideo = video.optString("state")
                            }
                        }

                        val kycVideo = Store.config.enlableKycVideo && stateVideo == "REJECTED"
                        val kycIdentity =
                            Store.config.enlableKycIdentify && stateImage == "REJECTED"
                        val kycFace = Store.config.enlableKycFace && stateFace == "REJECTED"
                        onPopupKyc(fragmentManager, kycVideo, kycIdentity, kycFace)
                        onSuccess(null)
                    } else if (state == "PENDING") {
                        openWallet(fragmentManager, onSuccess, onError)
                    }

                }
            }, onError)
        }

    }

    fun onPopupKyc(
        fragmentManager: FragmentManager,
        kycVideo: Boolean,
        kycIdentity: Boolean,
        kycFace: Boolean
    ) {
        CameraKycPopup.updateOnlyIdentify = false
        Store.config.kycVideo = kycVideo
        Store.config.kycIdentify = kycIdentity
        Store.config.kycFace = kycFace
        val bundle: Bundle = Bundle()
        bundle.putBoolean("openKycActivity", true)
        if (kycIdentity) {
            val popupTakeIdentify = PopupTakeIdentify()
            popupTakeIdentify.arguments = bundle
            popupTakeIdentify.show(fragmentManager, "ModalBottomSheet")
        } else if (kycFace) {
            val popupTakeFace = PopupTakeFace()
            popupTakeFace.arguments = bundle
            popupTakeFace.show(fragmentManager, "ModalBottomSheet")

        } else if (kycVideo) {
            val popupTakeVideo = PopupTakeVideo()
            popupTakeVideo.arguments = bundle
            popupTakeVideo.show(fragmentManager, "ModalBottomSheet")
        }
    }

    private fun openWalletActivity(
        action: Action,
        amount: Int?,
        content: String,
        extraData: String?,
        service: Service?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        Store.paymentInfo.service = service
        Store.paymentInfo.action = action
        Store.paymentInfo.content = content
        Store.paymentInfo.extraData = extraData
        if (amount != null) {
            Store.paymentInfo.amount = amount
        } else {
            Store.paymentInfo.amount = 0
        }

        Companion.onSuccess = onSuccess
        Companion.onError = onError
        payMEOpenSDKPopup.show(fragmentManager, null)
    }

    fun openHistory(
        fragmentManager: FragmentManager,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        setLanguage(PayME.context, Store.config.language)

        PayME.fragmentManager = fragmentManager

        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
            this.openWalletActivity(
                Action.OPEN_HISTORY,
                0,
                "",
                "",
                null,
                onSuccess,
                onError
            )
        }
    }


    fun deposit(
        fragmentManager: FragmentManager,
        amount: Int?,
        closeDepositResult: Boolean,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        setLanguage(PayME.context, Store.config.language)

        PayME.fragmentManager = fragmentManager

        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
            Store.config.closeWhenDone = closeDepositResult
            if (amount != null) {
                Store.paymentInfo.amount = amount
            } else {
                Store.paymentInfo.amount = 0
            }
            this.openWalletActivity(
                Action.DEPOSIT,
                amount,
                "",
                "",
                null,
                onSuccess,
                onError
            )
        }
    }

    fun transfer(
        fragmentManager: FragmentManager,
        amount: Int?,
        description: String,
        closeTransferResult: Boolean,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit,
        ) {
        setLanguage(PayME.context, Store.config.language)
        PayME.fragmentManager = fragmentManager
        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
            Store.config.closeWhenDone = closeTransferResult
            if (amount != null) {
                Store.paymentInfo.amount = amount
            } else {
                Store.paymentInfo.amount = 0
            }

            this.openWalletActivity(
                Action.TRANSFER,
                amount,
                description,
                "",
                null,
                onSuccess,
                onError
            )
        }
    }

    private fun transferInSDK(
        fragmentManager: FragmentManager,
        amount: Int?,
        description: String,
        closeTransferResult: Boolean,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit,
        data: String? = ""
    ) {
        setLanguage(PayME.context, Store.config.language)
        PayME.fragmentManager = fragmentManager
        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
            Store.config.closeWhenDone = closeTransferResult
            if (amount != null) {
                Store.paymentInfo.amount = amount
            } else {
                Store.paymentInfo.amount = 0
            }

            this.openWalletActivity(
                Action.TRANSFER,
                amount,
                description,
                data,
                null,
                onSuccess,
                onError
            )
        }
    }

    public fun withdraw(
        fragmentManager: FragmentManager,
        amount: Int?,
        closeWithdrawResult: Boolean,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        setLanguage(PayME.context, Store.config.language)

        PayME.fragmentManager = fragmentManager

        if (amount != null) {
            Store.paymentInfo.amount = amount
        } else {
            Store.paymentInfo.amount = 0
        }
        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
            Store.config.closeWhenDone = closeWithdrawResult
            this.openWalletActivity(
                Action.WITHDRAW,
                amount,
                "",
                "",
                null,
                onSuccess,
                onError
            )
        }
    }

    fun openService(
        fragmentManager: FragmentManager,
        service: Service,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        setLanguage(PayME.context, Store.config.language)
        PayME.fragmentManager = fragmentManager
        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
            this.openWalletActivity(
                Action.UTILITY,
                0,
                "",
                "",
                service,
                onSuccess,
                onError
            )
        }
    }


    private fun payInSDK(
        fragmentManager: FragmentManager,
        payCode: String,
        infoPayment: InfoPayment,
    ) {
        val payment = PayFunction()
        Store.config.disableCallBackResult = true
        payment.pay(fragmentManager, infoPayment, true, payCode, onSuccess, onError)
    }

    fun pay(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        payCode: String,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        setLanguage(context, Store.config.language)
        val payment = PayFunction()
        Store.config.disableCallBackResult = false
        payment.pay(fragmentManager, infoPayment, isShowResultUI, payCode, onSuccess, onError)
    }


    fun getAccountInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE, onError)) {
            val accountApi = AccountApi()
            accountApi.getAccountInfo(onSuccess = { jsonObject ->
                val account = jsonObject.getJSONObject("Account")
                val kyc = account.optJSONObject("kyc")
                if (kyc != null) {
                    val state = kyc.getString("state")
                    if (state == "APPROVED") {
                        Store.userInfo.accountKycSuccess = true
                    }
                }
                onSuccess(jsonObject)
            }, onError)
        }
    }

    fun getRemainingQuota(
        onSuccess: (Int?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val paymentApi = PaymentApi()
        paymentApi.getQuotaRemaining(onSuccess = {jsonObject ->
            val utility = jsonObject.optJSONObject("Utility")
            val getPaymentMethod = utility?.optJSONObject("GetPaymentMethod")
            val message = getPaymentMethod?.optString("message")
            val succeeded = getPaymentMethod?.optBoolean("succeeded") ?: false
            if (succeeded) {
                val remainingQuota = getPaymentMethod?.optInt("remainingQuota")
                onSuccess(remainingQuota)
            } else {
                onError(getPaymentMethod,ERROR_CODE.OTHER,message)
            }
        }, onError)
    }

    private fun loginAccount(
        onSuccess: (AccountStatus) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val accountApi = AccountApi()
        accountApi.intAccount(onSuccess = { jsonObject ->
            val openEWallet = jsonObject.getJSONObject("OpenEWallet")
            val init = openEWallet.getJSONObject("Init")
            Store.userInfo.dataInit = init
            val kyc = init.optJSONObject("kyc")
            val appEnv = init.optString("appEnv")
            val succeeded = init.optBoolean("succeeded")
            val message = init.optString("message")
            val accessToken = init.optString("accessToken")
            val handShake = init.optString("handShake")
            val phone = init.optString("phone")
            var stateKYC = ""
            Store.userInfo.accountActive = succeeded
            if (!succeeded && (phone == "null" || handShake == "null")) {
                if (handShake !== "null" && phone == "null") {
                    onError(
                        null,
                        ERROR_CODE.ACCOUNT_ERROR,
                        PayME.context.getString(R.string.please_enter_the_phone_number)
                    )
                } else {
                    onError(null, ERROR_CODE.ACCOUNT_ERROR, message)
                }
            } else {
                Store.config.openPayAndKyc = appEnv != Env.SANDBOX.toString()
                if (kyc != null) {
                    val state = kyc.optString("state")
                    stateKYC = state
                    Store.userInfo.accountKycSuccess = state == "APPROVED"
                } else {
                    Store.userInfo.accountKycSuccess = false
                }


                if (!accessToken.equals("null")) {
                    Store.userInfo.accessToken = accessToken
                } else {
                    Store.userInfo.accessToken = ""
                }
                Store.config.handShake = handShake
                Store.userInfo.accountLoginSuccess = true

                if (Store.userInfo.accountActive) {
                    if (Store.userInfo.accountKycSuccess) {
                        onSuccess(AccountStatus.KYC_APPROVED)
                    } else {
                        if (stateKYC == "REJECTED") {
                            onSuccess(AccountStatus.KYC_REJECTED)

                        } else if (stateKYC == "PENDING") {
                            onSuccess(AccountStatus.KYC_REVIEW)

                        } else {
                            onSuccess(AccountStatus.NOT_KYC)
                        }
                    }
                } else {
                    onSuccess(AccountStatus.NOT_ACTIVATED)
                }
            }

        }, onError = { jsonObject, code, message ->
            onError(jsonObject, code, message)
        })
    }

    fun logout() {
        Store.userInfo.accountLoginSuccess = false
        this.close()
    }

    fun getSupportedServices(
        onSuccess: (ArrayList<Service>?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN, onError)) {
            onSuccess(Store.paymentInfo.listService)
        }
    }

    fun getWalletInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        if (CheckAccount().check(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
            val paymentApi = PaymentApi()
            paymentApi.getBalance(onSuccess, onError)
        }
    }
}


