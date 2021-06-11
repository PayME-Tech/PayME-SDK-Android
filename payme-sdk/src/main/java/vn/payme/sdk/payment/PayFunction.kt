package vn.payme.sdk.payment

import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import vn.payme.sdk.CheckAccount
import vn.payme.sdk.PayME
import vn.payme.sdk.RULE_CHECK_ACCOUNT
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.model.DataMethod
import vn.payme.sdk.model.InfoPayment
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store
import java.text.DecimalFormat

internal class PayFunction {
    val loading = SpinnerDialog()
    private fun getBalance(
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val payME = PayME()
        payME.getWalletInfo(
            onSuccess = { jsonObject ->
                val walletBalance = jsonObject.getJSONObject("Wallet")
                val balance = walletBalance.getLong("balance")
                Store.userInfo.balance = balance
                onSuccess(null)
            },
            onError
        )


    }

    fun pay(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        method: Method?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {

        PayME.onSuccess = onSuccess
        PayME.onError = onError

        this.checkInfoPayment(
            fragmentManager,
            infoPayment,
            isShowResultUI,
            method,
            onSuccess,
            onError = { jsonObject, i, s ->
                onError(jsonObject, i, s)
                loading.dismiss()
            })
    }

    private fun checkInfoPayment(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        method: Method?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {

        loading.show(fragmentManager, null)
        Store.config.disableCallBackResult = false
        val checkAccount = CheckAccount()
        val decimal = DecimalFormat("#,###")
        if (infoPayment.amount!! < Store.config.limitPayment.min) {
            onError(
                null,
                ERROR_CODE.LITMIT,
                "Số tiền giao dịch tối thiểu ${decimal.format(Store.config.limitPayment.min)} VND"
            )
            return
        }
        if (infoPayment.amount!! > Store.config.limitPayment.max) {
            onError(
                null,
                ERROR_CODE.LITMIT,
                "Số tiền giao dịch tối đa ${decimal.format(Store.config.limitPayment.max)} VND"
            )
            return
        }
        // Pay Khong co phuong thuc

        if (method == null) {

            if (checkAccount.check(RULE_CHECK_ACCOUNT.LOGGIN, onError)) {
                if (Store.userInfo.accountActive && Store.userInfo.accountKycSuccess) {
                    getBalance(onSuccess = {
                        getListMethod(
                            fragmentManager,
                            infoPayment,
                            isShowResultUI,
                            method,
                            onSuccess,
                            onError
                        )
                    }, onError)
                } else {
                    getListMethod(
                        fragmentManager,
                        infoPayment,
                        isShowResultUI,
                        method,
                        onSuccess,
                        onError
                    )
                }
                return
            }
        }

        if (method?.type == TYPE_PAYMENT.WALLET) {

            if (checkAccount.check(RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC, onError)) {
                getBalance(onSuccess = {
                    checkFee(
                        fragmentManager,
                        infoPayment,
                        isShowResultUI,
                        method,
                        onSuccess,
                        onError
                    )
                }, onError)
            }
            return
        }
        if (!((method?.type == TYPE_PAYMENT.WALLET) || (method?.type == TYPE_PAYMENT.BANK_CARD) || (method?.type == TYPE_PAYMENT.LINKED))) {
            onError(null, ERROR_CODE.PAYMENT_ERROR, "Phương thức chưa được hỗ trợ")
            return
        }
        if (method?.type != TYPE_PAYMENT.WALLET && !Store.config.openPayAndKyc) {
            onError(
                null,
                ERROR_CODE.OTHER,
                "Chức năng chỉ có thể thao tác môi trường production"
            )
        }
        if (checkAccount.check(RULE_CHECK_ACCOUNT.LOGGIN, onError)) {
            checkFee(fragmentManager, infoPayment, isShowResultUI, method, onSuccess, onError)
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
                        if (method.type == TYPE_PAYMENT.WALLET && (Store.userInfo.balance < (infoPayment.amount + fee))) {
                            onError(null, ERROR_CODE.BALANCE_ERROR, "Số dư trong ví không đủ")
                        } else {
                            EventBus.getDefault().postSticky(PaymentInfoEvent(null, fee))
                            getListMethod(
                                fragmentManager,
                                infoPayment,
                                isShowResultUI,
                                method,
                                onSuccess,
                                onError
                            )
                        }

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

    private fun getListMethod(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        method: Method?,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit,
    ) {
        val paymentApi = PaymentApi()
        paymentApi.getTransferMethods(
            infoPayment!!.storeId,
            onSuccess = { jsonObject ->
                val Utility = jsonObject.optJSONObject("Utility")
                val GetPaymentMethod = Utility.optJSONObject("GetPaymentMethod")
                val message = GetPaymentMethod.optString("message")
                val succeeded = GetPaymentMethod.optBoolean("succeeded")
                val methods = GetPaymentMethod.optJSONArray("methods")
                if (succeeded) {
                    Store.paymentInfo.listMethod = arrayListOf()
                    var checkMethodNotFound = false
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
                        var feeDescription = jsonObject.optString("feeDescription")
                        var methodId = jsonObject.optInt("methodId")
                        var minFee = jsonObject.optInt("minFee")
                        var title = jsonObject.optString("title")
                        var type = jsonObject.optString("type")
                        Store.paymentInfo.listMethod.add(
                            Method(
                                dataMethod,
                                fee,
                                label,
                                methodId,
                                minFee,
                                feeDescription,
                                title,
                                type,
                            )
                        )
                        if (method?.methodId == methodId) {
                            checkMethodNotFound = true
                        }

                    }
                    if (method != null && !checkMethodNotFound) {
                        onError(
                            null,
                            ERROR_CODE.PAYMENT_ERROR,
                            "Không tìm thấy phương thức thanh toán"
                        )
                    } else {
                        Store.paymentInfo.transaction = ""
                        Store.paymentInfo.isChangeMethod = method == null
                        Store.paymentInfo.methodSelected = method
                        Store.paymentInfo.isShowResultUI = isShowResultUI

                        PayME.fragmentManager = fragmentManager
                        Store.paymentInfo.infoPayment = infoPayment
                        val paymePayment: PaymePayment = PaymePayment()
                        loading.dismiss()
                        paymePayment.show(
                            fragmentManager,
                            "ModalBottomSheet"
                        )
                    }

                } else {
                    onError(null,ERROR_CODE.PAYMENT_ERROR,message)
                }
            },
            onError
        )
    }


}