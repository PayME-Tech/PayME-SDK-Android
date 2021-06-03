package vn.payme.sdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.spongycastle.jce.provider.BouncyCastleProvider
import vn.payme.sdk.api.AccountApi
import vn.payme.sdk.api.ENV_API
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.enums.*
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.kyc.CameraKycActivity
import vn.payme.sdk.model.*
import vn.payme.sdk.payment.PaymePayment
import vn.payme.sdk.payment.PopupTakeFace
import vn.payme.sdk.payment.PopupTakeIdentify
import vn.payme.sdk.payment.PopupTakeVideo
import vn.payme.sdk.store.Config
import vn.payme.sdk.store.PaymentInfo
import vn.payme.sdk.store.Store
import vn.payme.sdk.store.UserInfo
import java.security.Security
import java.text.DecimalFormat

enum class RULE_CHECK_ACCOUNT {
    LOGGIN_ACTIVE_KYC,
    LOGGIN_ACTIVE,
    LOGGIN,
}

public class PayME {
    companion object {
        lateinit var context: Context
        lateinit var onSuccess: ((JSONObject?) -> Unit)
        lateinit var onError: (JSONObject?, Int?, String) -> Unit
        lateinit var fragmentManager: FragmentManager
        fun showError(message: String) {
            Toasty.error(PayME.context, message, Toast.LENGTH_SHORT, true).show();
        }

        internal fun onExpired() {
            Store.userInfo.accountLoginSuccess = false
            var even: EventBus = EventBus.getDefault()
            var myEven2: MyEven = MyEven(TypeCallBack.onExpired, "")
            even.post(myEven2)
            EventBus.getDefault()
                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT, null))
        }
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
        Store.paymentInfo = PaymentInfo(null, 0, "", null, null, null, null,  arrayListOf(), null, true,true)
        Store.userInfo = UserInfo(0, false, false, false, "", null)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        ENV_API.updateEnv()
    }

    constructor() {

    }

    private fun checkAccount(
        rule: RULE_CHECK_ACCOUNT,
        onError: (JSONObject?, Int?, String) -> Unit
    ): Boolean {
        if (rule == RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC) {
            if (!Store.userInfo.accountLoginSuccess) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_LOGIN, "Tài khoản chưa đăng nhập")
                return false
            } else if (!Store.userInfo.accountActive) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVATED, "Tài khoản chưa kích hoạt")
                return false
            } else if (!Store.userInfo.accountKycSuccess) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, "Tài khoản chưa định danh")
                return false
            }
            return true
        }
        if (rule == RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE) {
            if (!Store.userInfo.accountLoginSuccess) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_LOGIN, "Tài khoản chưa đăng nhập")
                return false
            } else if (!Store.userInfo.accountActive) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVATED, "Tài khoản chưa kích hoạt")
                return false
            }
            return true
        }
        if (rule == RULE_CHECK_ACCOUNT.LOGGIN) {
            if (!Store.userInfo.accountLoginSuccess) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_LOGIN, "Tài khoản chưa đăng nhập")
                return false
            }
            return true
        }
        return true

    }

    public fun onForgotPassword() {
        openWalletActivity(Action.FORGOT_PASSWORD, 0, "", null, null, onSuccess, onError)
    }

    public fun login(
        onSuccess: (AccountStatus) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val paymentApi = PaymentApi()


        paymentApi.getSettings(onSuccess = { jsonObject ->
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

                if (key == "limit.param.amount.payment" && valueString != null) {
                    val value = JSONObject(valueString)
                    val max = Integer.parseInt(value.optString("max"))
                    val min = Integer.parseInt(value.optString("min"))
                    Store.config.limitPayment = MaxminPayment(min, max)
                }
                if (key == "limit.param.amount.all" && valueString != null) {
                    val value = JSONObject(valueString)
                    val max = Integer.parseInt(value.optString("max"))
                    val min = Integer.parseInt(value.optString("min"))
                    Store.config.limitPayment = MaxminPayment(min, max)

                }
                if (key == "service.main.visible" && valueString != null) {
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
            checkClientInfo(onSuccess, onError)
        },
            onError = { jsonObject, code, message ->
                onError(jsonObject, code, message)
            }
        )


    }

    private fun checkClientInfo(
        onSuccess: (AccountStatus) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val accountApi = AccountApi()
        val pref = PayME.context.getSharedPreferences("PayME_SDK", Context.MODE_PRIVATE)
        val clientId = pref.getString("clientId", "")
        val dataRegisterClientInfo = pref.getString("dataRegisterClientInfo", "")
        if (clientId?.length!! <= 0 || !dataRegisterClientInfo.equals(
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
                    loginAccount(onSuccess = { jsonObject ->
                        onSuccess(jsonObject)
                    }, onError = { jsonObject, code, message ->
                        onError(jsonObject, code, message)
                    })
                },
                onError = { jsonObject, code, message ->
                    onError(jsonObject, code, message)
                }
            )
        } else {
            Store.config.clientId = clientId.toString()
            loginAccount(onSuccess = { accountInfo ->
                onSuccess(accountInfo)
            }, onError = { jsonObject, code, message ->
                onError(jsonObject, code, message)
            })

        }
    }


    fun openWallet(
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (checkAccount(RULE_CHECK_ACCOUNT.LOGGIN, onError)) {
            Store.config.closeWhenDone = false
            openWalletActivity(Action.OPEN, 0, "", null, null, onSuccess, onError)
        }

    }

    fun openKYC(
        fragmentManager: FragmentManager,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (checkAccount(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE, onError)) {
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

                        val kycVideo = Store.config.enlableKycFace && stateVideo == "REJECTED"
                        val kycIdentity =
                            Store.config.enlableKycIdentify && stateImage == "REJECTED"
                        val kycFace = Store.config.enlableKycFace && stateFace == "REJECTED"
                        onSuccess(null)
                        onPopupKyc(fragmentManager, kycVideo, kycIdentity, kycFace)
                    } else if (state == "PENDING") {
                        openWallet(onSuccess, onError)
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
        CameraKycActivity.updateOnlyIdentify  = false
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
        onError: (JSONObject?, Int?, String) -> Unit
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
        val intent = Intent(context, PaymeWaletActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context?.startActivity(intent)
        Companion.onSuccess = onSuccess
        Companion.onError = onError
    }


    public fun deposit(
        amount: Int,
        closeDepositResult: Boolean,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (checkAccount(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
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

    public fun transfer(
        amount: Int,
        description: String,
        closeTransferResult: Boolean,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (checkAccount(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
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

    public fun openService(
        service: Service,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (checkAccount(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
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


    public fun withdraw(
        amount: Int,
        closeWithdrawResult: Boolean,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (checkAccount(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
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

    fun payInSDK(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
    ) {
        if (checkAccount(RULE_CHECK_ACCOUNT.LOGGIN, onError= {jsonObject, code, m ->
                PayME.showError(m)
            })) {
            payRoot(fragmentManager, infoPayment, true, null, onSuccess, onError)
            Store.config.disableCallBackResult = true
        }
    }

    fun pay(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        method: Method?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if(method!=null && method.type == TYPE_PAYMENT.WALLET ) {
            if(checkAccount(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC,onError)){
                getWalletInfo(onSuccess={jsonObject ->
                    val walletBalance = jsonObject.getJSONObject("Wallet")
                    val balance = walletBalance.getDouble("balance")
                    if (balance < infoPayment.amount){
                        onError(null,ERROR_CODE.BALANCE_ERROR,"Số dư trong ví không đủ")
                    }else{
                        payWithMethod(fragmentManager, infoPayment, isShowResultUI, method, onSuccess, onError)
                    }
                }, onError)
            }

        }else if (checkAccount(RULE_CHECK_ACCOUNT.LOGGIN, onError)) {
            payWithMethod(fragmentManager, infoPayment, isShowResultUI, method, onSuccess, onError)

        }

    }
    private fun payWithMethod(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        method: Method?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ){
        Store.config.disableCallBackResult = false
        if (method == null || method.type == TYPE_PAYMENT.BANK_CARD) {
            payRoot(fragmentManager, infoPayment, isShowResultUI, method, onSuccess, onError)
        } else {
            checkFee(
                fragmentManager,
                infoPayment,
                isShowResultUI,
                method,
                onSuccess,
                onError,
            )
        }

    }

    private fun checkFee(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        method: Method?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val paymentApi = PaymentApi()
        val method = method
        val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
        paymentApi.getFee(
            infoPayment!!.amount,
            method!!,
            onSuccess = { jsonObject ->
                val Utility = jsonObject.getJSONObject("Utility")
                val GetFee = Utility.getJSONObject("GetFee")
                val succeeded = GetFee.getBoolean("succeeded")
                val message = GetFee.getString("message")
                if (succeeded) {
                    val feeObject = GetFee.getJSONObject("fee")
                    val fee = feeObject.getInt("fee")
                    val state = GetFee.optString("state")
                    if (state == "null") {
                        EventBus.getDefault().postSticky(PaymentInfoEvent(null, null, null, fee))
                        payRoot(
                            fragmentManager,
                            infoPayment,
                            isShowResultUI,
                            method,
                            onSuccess,
                            onError
                        )

                    } else {
                        onError(GetFee, ERROR_CODE.PAYMENT_ERROR, message)
                    }
                } else {
                    onError(GetFee, ERROR_CODE.PAYMENT_ERROR, message)
                }
            },
            onError
        )
    }


    fun payRoot(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        method: Method?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit,
    ) {
        if (method != null && !((method.type == TYPE_PAYMENT.WALLET) || (method.type == TYPE_PAYMENT.BANK_CARD) || (method.type == TYPE_PAYMENT.LINKED))) {
            onError(null, null, "Phương thức chưa được hỗ trợ")
        }else if(method != null && method?.type != TYPE_PAYMENT.WALLET && !Store.config.openPayAndKyc) {
            onError(null, ERROR_CODE.OTHER, "Chức năng chỉ có thể thao tác môi trường production")
        }else{
            Store.paymentInfo.transaction = ""
            Store.paymentInfo.isChangeMethod = method == null
            Store.paymentInfo.methodSelected = method
            Store.paymentInfo.isShowResultUI = isShowResultUI
            Companion.onSuccess = onSuccess
            Companion.onError = onError
            PayME.fragmentManager = fragmentManager
            Store.paymentInfo.infoPayment = infoPayment
            val decimal = DecimalFormat("#,###")
            if (infoPayment.amount!! < Store.config.limitPayment.min) {
                onError(
                    null,
                    ERROR_CODE.LITMIT,
                    "Số tiền giao dịch tối thiểu ${decimal.format(Store.config.limitPayment.min)} VND"
                )
            } else if (infoPayment.amount!! > Store.config.limitPayment.max) {
                onError(
                    null,
                    ERROR_CODE.LITMIT,
                    "Số tiền giao dịch tối đa ${decimal.format(Store.config.limitPayment.max)} VND"
                )
            } else {
                val paymePayment: PaymePayment = PaymePayment()
                paymePayment.show(
                    fragmentManager,
                    "ModalBottomSheet"
                )
            }
        }
    }

    fun closeOpenWallet() {
        var even: EventBus = EventBus.getDefault()
        var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
        even.post(myEven)
        var closeWebview: MyEven = MyEven(TypeCallBack.onExpired, "")
        even.post(myEven)
        even.post(closeWebview)
    }

    public fun getAccountInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {

        if (checkAccount(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE,onError)) {

            val accountApi = AccountApi()
            accountApi.getAccountInfo(onSuccess = { jsonObject ->
                val Account = jsonObject.getJSONObject("Account")
                val kyc = Account.optJSONObject("kyc")
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

    private fun loginAccount(
        onSuccess: (AccountStatus) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val accountApi = AccountApi()
        accountApi.intAccount(onSuccess = { jsonObject ->
            val OpenEWallet = jsonObject.getJSONObject("OpenEWallet")
            val Init = OpenEWallet.getJSONObject("Init")
            Store.userInfo.dataInit = Init
            val kyc = Init.optJSONObject("kyc")
            val appEnv = Init.optString("appEnv")
            val succeeded = Init.optBoolean("succeeded")
            Store.userInfo.accountActive = succeeded
            if (appEnv == Env.SANDBOX.toString()) {
                Store.config.openPayAndKyc = false
            } else {
                Store.config.openPayAndKyc = true
            }
            if (kyc != null) {
                val state = kyc.optString("state")
                println("state"+state)

                if (state == "APPROVED") {
                    Store.userInfo.accountKycSuccess = true
                } else {
                    Store.userInfo.accountKycSuccess = false
                }
            } else {
                Store.userInfo.accountKycSuccess = false
            }

            val accessToken = Init.optString("accessToken")
            val handShake = Init.optString("handShake")
            if (!accessToken.equals("null")) {
                Store.userInfo.accessToken = accessToken
            } else {
                Store.userInfo.accessToken = ""
            }
            Store.config.handShake = handShake
            Store.userInfo.accountLoginSuccess = true
            Log.d("LOGIN","login:"+Store.userInfo.accountKycSuccess)

            if (Store.userInfo.accountActive) {
                if (Store.userInfo.accountKycSuccess) {
                    onSuccess(AccountStatus.KYC_APPROVED)
                } else {
                    onSuccess(AccountStatus.NOT_KYC)
                }
            } else {
                onSuccess(AccountStatus.NOT_ACTIVATED)
            }
        }, onError = { jsonObject, code, message ->
            onError(jsonObject, code, message)
        })
    }

    public fun logout() {
        this.closeOpenWallet()
    }

    public fun getSupportedServices(onSuccess: (ArrayList<Service>?) -> Unit,onError: (JSONObject?, Int?, String) -> Unit)  {
        if(checkAccount(RULE_CHECK_ACCOUNT.LOGGIN,onError)){
            onSuccess(Store.paymentInfo.listService)
        }
    }
    public fun getPaymentMethods(
        storeId: Long,
        onSuccess: (ArrayList<Method>) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if(checkAccount(RULE_CHECK_ACCOUNT.LOGGIN,onError)) {
            val paymentApi = PaymentApi()
            val listMethod: ArrayList<Method> = ArrayList<Method>()
            paymentApi.getTransferMethods(
                storeId,
                onSuccess = { jsonObject ->
                    val Utility = jsonObject.optJSONObject("Utility")
                    val GetPaymentMethod = Utility.optJSONObject("GetPaymentMethod")
                    val message = GetPaymentMethod.optString("message")
                    val succeeded = GetPaymentMethod.optBoolean("succeeded")
                    val methods = GetPaymentMethod.optJSONArray("methods")
                    if (succeeded) {
                        for (i in 0 until methods.length()) {
                            val jsonObject = methods.getJSONObject(i)
                            var data = jsonObject.optJSONObject("data")
                            var dataMethod = DataMethod(null, "")
                            if (data != null) {
                                val linkedId = data.optString("linkedId")
                                val swiftCode = data.optString("swiftCode")
                                dataMethod = DataMethod(linkedId, swiftCode)
                            }
                            var fee = jsonObject.optInt("fee")
                            var label = jsonObject.optString("label")
                            var methodId = jsonObject.optInt("methodId")
                            var minFee = jsonObject.optInt("minFee")
                            var title = jsonObject.optString("title")
                            var type = jsonObject.optString("type")
                            listMethod.add(
                                Method(
                                    dataMethod,
                                    fee,
                                    label,
                                    methodId,
                                    minFee,
                                    title,
                                    type,
                                )
                            )
                        }
                        onSuccess(listMethod)
                    } else {
                        onError(null, null, message)
                    }
                },
                onError
            )
        }
    }

    fun getWalletInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (checkAccount(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC,onError)) {
            val paymentApi = PaymentApi()
            paymentApi.getBalance(onSuccess, onError)
        }
    }
}


