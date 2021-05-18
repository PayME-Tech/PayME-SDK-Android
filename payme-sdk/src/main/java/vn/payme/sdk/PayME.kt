package vn.payme.sdk

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.util.Base64
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.fragment.app.FragmentManager
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.spongycastle.jce.provider.BouncyCastleProvider
import vn.payme.sdk.api.AccountApi
import vn.payme.sdk.api.ENV_API
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.api.QueryBuilder
import vn.payme.sdk.enums.*
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.*
import vn.payme.sdk.payment.PaymePayment
import vn.payme.sdk.store.Config
import vn.payme.sdk.store.PaymentInfo
import vn.payme.sdk.store.Store
import vn.payme.sdk.store.UserInfo
import java.nio.charset.StandardCharsets
import java.security.Security
import java.text.DecimalFormat

public class PayME(
    context: Context,
    appToken: String,
    publicKey: String,
    connectToken: String,
    appPrivateKey: String,
    configColor: Array<String>,
    language: LANGUAGES,
    env: Env,
    showLog: Boolean,
) {
    companion object {
        lateinit var context: Context
        lateinit var onSuccess: ((JSONObject?) -> Unit)
        lateinit var onError: (JSONObject?, Int?, String) -> Unit
        lateinit var fragmentManager: FragmentManager
        fun showError(message: String) {
            Toasty.error(PayME.context, message, Toast.LENGTH_SHORT, true).show();
        }

        internal fun onExpired() {
            var even: EventBus = EventBus.getDefault()
            var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
            var myEven2: MyEven = MyEven(TypeCallBack.onExpired, "")
            even.post(myEven)
            even.post(myEven2)
        }
    }

    init {
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
            Store.config.clientId
        )
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        ENV_API.updateEnv()
    }
    public fun  onForgotPassword(){
        openWalletActivity(Action.FORGOT_PASSWORD, 0, null, null, null, onSuccess, onError)
    }

    public fun login(
        onSuccess: (AccountStatus) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val accountApi = AccountApi()
        val paymentApi = PaymentApi()
        val pref = PayME.context.getSharedPreferences("PayME_SDK", Context.MODE_PRIVATE)
        val clientId = pref.getString("clientId", "")

        val dataRegisterClientInfo = pref.getString("dataRegisterClientInfo", "")
        paymentApi.getSettings(onSuccess = { jsonObject ->
            val Setting = jsonObject.getJSONObject("Setting")
            val configs = Setting.getJSONArray("configs")
            for (i in 0 until configs.length()) {
                val config = configs.optJSONObject(i)
                val key = config.optString("key")
                val valueString = config.optString("value")

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
                        val json = listService.getJSONObject(i)
                        val code = json.optString("code")
                        val description = json.optString("description")
                        val disable = json.optBoolean("disable")
                        val enable = json.optBoolean("enable")
                        if (enable == true && disable == false) {
                            Store.paymentInfo.listService!!.add(Service(code, description))
                        }
                    }
                }
            }
        },
            onError = { jsonObject, code, message ->

            }
        )

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


    public fun openWallet(
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        openWalletActivity(Action.OPEN, 0, null, null, null, onSuccess, onError)

    }

    private fun openWalletActivity(
        action: Action,
        amount: Int?,
        content: String?,
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
        content: String?,
        extraData: String,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        Store.paymentInfo.content = content
        Store.paymentInfo.extraData = extraData
        if (amount != null) {
            Store.paymentInfo.amount = amount
        } else {
            Store.paymentInfo.amount = 0
        }
        if (!Store.userInfo.accountActive) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else if (!Store.userInfo.accountKycSuccess) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, "Tài khoản chưa định danh")
        } else {
            this.openWalletActivity(
                Action.DEPOSIT,
                amount,
                content,
                extraData,
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

        if (!Store.userInfo.accountActive) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else if (!Store.userInfo.accountKycSuccess) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, "Tài khoản chưa định danh")
        } else {
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
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {

        if (!Store.userInfo.accountActive) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else if (!Store.userInfo.accountKycSuccess) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, "Tài khoản chưa định danh")
        } else {
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

    public fun pay(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        method: Method?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit,
    ) {
        println("method"+method?.title)

        Store.paymentInfo.transaction = ""
        Store.paymentInfo.isChangeMethod = method == null
        Store.paymentInfo.methodSelected = method

        if (method != null && method?.type != TYPE_PAYMENT.WALLET && !Store.config.openPayAndKyc) {
            PayME.showError("Chức năng chỉ có thể thao tác môi trường production")
        } else {
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
        if (!Store.userInfo.accountActive) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else {
            val accountApi = AccountApi()
            accountApi.getAccountInfo(onSuccess, onError)
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
            if (Store.userInfo.accountActive) {
                if (Store.userInfo.accountKycSuccess) {
                    onSuccess(AccountStatus.KYC_OK)
                } else {
                    onSuccess(AccountStatus.NOT_KYC)
                }
            } else {
                onSuccess(AccountStatus.NOT_ACTIVED)
            }
        }, onError = { jsonObject, code, message ->
            onError(jsonObject, code, message)
        })
    }

    public fun logout() {
        this.closeOpenWallet()
    }

    public fun getSupportedServices(): ArrayList<Service> {
        return Store.paymentInfo.listService!!
    }

    public fun getPaymentMethods(
        onSuccess: (ArrayList<Method>) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val paymentApi = PaymentApi()
        val listMethod: ArrayList<Method> = ArrayList<Method>()
        paymentApi.getTransferMethods(
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

    public fun getWalletInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        if (!Store.userInfo.accountActive) {
            onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVETES, "Tài khoản chưa kích hoạt")
        } else {
            val paymentApi = PaymentApi()
            paymentApi.getBalance(onSuccess, onError)
        }
    }
}


