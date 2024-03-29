package vn.payme.sdk.payment

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import vn.payme.sdk.CheckAccount
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.RULE_CHECK_ACCOUNT
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.PAY_CODE
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.*
import vn.payme.sdk.model.*
import vn.payme.sdk.store.Store
import java.text.DecimalFormat

internal class PayFunction {
    val loading = SpinnerDialog()
    private fun getBalance(
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val payME = PayME()
        payME.getWalletInfo(
            onSuccess = { jsonObject ->
                val walletBalance = jsonObject.getJSONObject("Wallet")
                val balance = walletBalance.optLong("balance")
                Store.userInfo.balance = balance
                onSuccess(null)
            },
            onError
        )


    }

    private fun getStoreInfo(
        infoPayment: InfoPayment,
        onSuccess: () -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val paymentApi = PaymentApi()
        paymentApi.getInfoMerchant(infoPayment.storeId, onSuccess = { jsonObject ->
            val OpenEWallet = jsonObject.getJSONObject("OpenEWallet")
            val GetInfoMerchant = OpenEWallet.getJSONObject("GetInfoMerchant")
            val storeImage = GetInfoMerchant.optString("storeImage")
            val storeName = GetInfoMerchant.optString("storeName")
            val merchantName = GetInfoMerchant.optString("merchantName")
            val isVisibleHeader = GetInfoMerchant.optBoolean("isVisibleHeader")
            EventBus.getDefault().postSticky(StoreInfo(storeImage, storeName,merchantName, isVisibleHeader))
            onSuccess()
        }, onError)

    }

    internal fun getListBank(
        onSuccess: () -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val listBank =
            EventBus.getDefault().getStickyEvent(ListBankAtm::class.java)
        if (listBank != null && listBank.listBankATM.size > 0) {
            onSuccess()
        } else {
            val paymentApi = PaymentApi()
            paymentApi.getListBanks(
                onSuccess = { jsonObject ->
                    val Setting = jsonObject.optJSONObject("Setting")
                    val banks = Setting.optJSONArray("banks")
                    val listBanks = arrayListOf<BankInfo>()
                    for (i in 0 until banks.length()) {
                        val bank = banks.optJSONObject(i)
                        val cardPrefix = bank.optString("cardPrefix")
                        val depositable = bank.optBoolean("depositable")
                        val vietQRAccepted = bank.optBoolean("vietQRAccepted")
                        val cardNumberLength = bank.optInt("cardNumberLength")
                        val shortName = bank.optString("shortName")
                        val swiftCode = bank.optString("swiftCode")
                        val requiredDate = bank.optString("requiredDate")
                        val bankInfo = BankInfo(
                            depositable,
                            vietQRAccepted,
                            cardPrefix,
                            cardNumberLength,
                            shortName,
                            swiftCode,
                            requiredDate,
                        )
                        listBanks.add(bankInfo)

                    }
                    EventBus.getDefault().postSticky(ListBankAtm(listBanks))
                    onSuccess()

                },
                onError
            )
        }
    }

    internal fun getListBankTransfer(
        onSuccess: () -> Unit,
        method: Method, onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val listBank =
            EventBus.getDefault().getStickyEvent(ListBankTransfer::class.java)
        if (listBank != null && listBank.listBankTransferInfo.size > 0) {
            onSuccess()
        } else {
            val paymentApi = PaymentApi()
            paymentApi.payment(
                method,
                "",
                null,
                "",
                "",
                false,
                null,
                onSuccess = { jsonObject ->
                    val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                    val Payment = OpenEWallet.optJSONObject("Payment")
                    val Pay = Payment.optJSONObject("Pay")
                    val succeeded = Pay.optBoolean("succeeded")
                    val payment = Pay.optJSONObject("payment")
                    val message = Pay.optString("message")
                    if (succeeded) {
                        val listBank = arrayListOf<BankTransferInfo>()
                        val bankList = payment.optJSONArray("bankList")
                        for (i in 0 until bankList.length()) {
                            val bank = bankList.optJSONObject(i)
                            val bankAccountName = bank.optString("bankAccountName")
                            val bankAccountNumber = bank.optString("bankAccountNumber")
                            val bankBranch = bank.optString("bankBranch")
                            val bankCity = bank.optString("bankCity")
                            val bankName = bank.optString("bankName")
                            val content = bank.optString("content")
                            val swiftCode = bank.optString("swiftCode")
                            val qrContent = bank.optString("qrContent")
                            val qrImage = bank.optString("qrImage")
                            val bankTransferInfo = BankTransferInfo(
                                bankAccountName,
                                bankAccountNumber,
                                bankBranch,
                                bankCity,
                                bankName,
                                content,
                                swiftCode,
                                qrContent,
                                qrImage
                            )
                            listBank.add(bankTransferInfo)
                        }
                        if(listBank.size==0){
                            onError(null,ERROR_CODE.PAYMENT_ERROR,PayME.context.getString(R.string.error_manual_bank))
                        }else{
                            EventBus.getDefault().postSticky(listBank[0])
                            EventBus.getDefault().postSticky(ListBankTransfer(listBank))
                            onSuccess()
                        }
                    } else {
                        onError(null, ERROR_CODE.PAYMENT_ERROR, message)
                    }
                },
                onError
            )
        }
    }

    fun pay(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        isShowResultUI: Boolean,
        payCode: String,
        onSuccess: (JSONObject?) -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        loading.show(fragmentManager, null)
        Store.paymentInfo.isShowResultUI = isShowResultUI
        PayME.fragmentManager = fragmentManager
        Store.paymentInfo.infoPayment = infoPayment
        Store.paymentInfo.payCode = payCode
        val arrayBank = arrayListOf<BankTransferInfo>()
        EventBus.getDefault().postSticky(ListBankTransfer(arrayBank))
        EventBus.getDefault().postSticky(FeeInfo(0,0, "", ""))
        PayME.onSuccess = onSuccess
        PayME.onError = onError
        val checkAccount = CheckAccount()
        if (checkAccount.check(RULE_CHECK_ACCOUNT.LOGGIN, onError = { jsonObject, i, s ->
                onError(jsonObject, i, s)
                loading.dismiss()
            })) {
            getStoreInfo(infoPayment, onSuccess = {
                checkInfoPayment(
                    fragmentManager,
                    infoPayment,
                    payCode,
                    onError = { jsonObject, i, s ->
                        onError(jsonObject, i, s)
                        loading.dismiss()
                    })
            }, onError = { jsonObject, i, s ->
                onError(jsonObject, i, s)
                loading.dismiss()
            })
        }
    }

    private fun checkInfoPayment(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        payCode: String,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val decimal = DecimalFormat("#,###")
        if (!((payCode == PAY_CODE.PAYME) ||
                    (payCode == PAY_CODE.ATM) ||
                    (payCode == PAY_CODE.MANUAL_BANK) ||
                    (payCode == PAY_CODE.VIET_QR) ||
//                    (payCode == PAY_CODE.VN_PAY) ||
                    (payCode == PAY_CODE.CREDIT))
        ) {
            onError(
                null,
                ERROR_CODE.PAYMENT_ERROR,
                PayME.context.getString(R.string.method_not_supported)
            )
            return
        }
        if (payCode != PAY_CODE.PAYME && !Store.config.openPayAndKyc) {
            onError(
                null,
                ERROR_CODE.OTHER,
                PayME.context.getString(R.string.function_can_only_manipulate_production)
            )
            return
        }
        if (infoPayment.amount!! < Store.config.limitPayment.min) {
            onError(
                null,
                ERROR_CODE.LITMIT,
                "${PayME.context.getString(R.string.min_transaction_amount)} ${decimal.format(Store.config.limitPayment.min)} VND"
            )
            return
        }
        if (infoPayment.amount!! > Store.config.limitPayment.max) {
            onError(
                null,
                ERROR_CODE.LITMIT,
                "${PayME.context.getString(R.string.maximum_transaction_amount)} ${
                    decimal.format(
                        Store.config.limitPayment.max
                    )
                } VND"
            )
            return
        }
        getListMethod(
            fragmentManager,
            infoPayment,
            payCode,
            onError
        )

    }

    fun showPopupPayment(
        fragmentManager: FragmentManager,
        method: Method?,
        onError: (JSONObject?, Int, String?) -> Unit,
    ) {
        Store.paymentInfo.methodSelected = method
        Store.paymentInfo.transaction = ""
        when (method?.type) {
            TYPE_PAYMENT.BANK_CARD -> {
                getListBank(onSuccess = {
                    val popupPayment: PopupPayment = PopupPayment()
                    loading.dismiss()
                    popupPayment.show(
                        fragmentManager,
                        "ModalBottomSheet"
                    )
                }, onError)
            }
            TYPE_PAYMENT.BANK_TRANSFER -> {
                getListBankTransfer(onSuccess = {
                    val popupPayment: PopupPayment = PopupPayment()
                    loading.dismiss()
                    popupPayment.show(
                        fragmentManager,
                        "ModalBottomSheet"
                    )
                }, method, onError)
            }
            else -> {
                val popupPayment: PopupPayment = PopupPayment()
                loading.dismiss()
                popupPayment.show(
                    fragmentManager,
                    "ModalBottomSheet"
                )
            }
        }
    }

    private fun submitPayVietQR(
        fragmentManager: FragmentManager,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val paymentApi = PaymentApi()
        paymentApi.payment(
            Store.paymentInfo.methodSelected!!,
            "",
            null,
            "",
            "",
            false,
            null,
            onSuccess = { jsonObject ->
                val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                val Payment = OpenEWallet?.optJSONObject("Payment")
                val Pay = Payment?.optJSONObject("Pay")
                val history = Pay?.optJSONObject("history")
                if (history != null) {
                    val payment = history.optJSONObject("payment")
                    if (payment != null) {
                        val transaction = payment.optString("transaction")
                        Store.paymentInfo.transaction = transaction
                    }
                }
                val payment = Pay?.optJSONObject("payment")
                val message = Pay?.optString("message")

                if (payment != null) {
                    val state = payment.optString("vietQRState")
                    val qrContent = payment.optString("qrContent", "")
                    if (state == "REQUIRED_TRANSFER") {
                        val popupPayment = PopupPayment()
                        loading.dismiss()
                        EventBus.getDefault().postSticky(QRContentVietQR(qrContent))
                        val bankInfoArray = payment.optJSONArray("banks")
                        if (bankInfoArray != null && bankInfoArray.length() > 0) {
                            val bankInfo = bankInfoArray.optJSONObject(0)
                            val bankName = bankInfo?.optString("bankName")
                            val bankNumber = bankInfo?.optString("bankNumber")
                            val fullName = bankInfo?.optString("fullName")
                            val content = bankInfo?.optString("content")
                            val vietQRbankInfo = VietQrTransferInfo(bankNumber, bankName, fullName, content)
                            EventBus.getDefault().postSticky(vietQRbankInfo)
                        } else {
                            EventBus.getDefault().postSticky(VietQrTransferInfo(null, null, null, null))
                        }
                        popupPayment.show(
                            fragmentManager,
                            "ModalBottomSheet"
                        )
                    } else {
                        onError(null, ERROR_CODE.PAYMENT_ERROR, message)
                    }
                }else{
                    onError(null,ERROR_CODE.PAYMENT_ERROR,message)

                }
            },onError)


    }

   fun payVNPAY(
       fragmentManager: FragmentManager,
       onError: (JSONObject?, Int, String?) -> Unit,
       ){
       val paymentApi = PaymentApi()
       paymentApi.payment(
           Store.paymentInfo.methodSelected!!,
           "",
           null,
           "",
           "",
           true,
           null,
           onSuccess = { jsonObject ->
               val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
               val Payment = OpenEWallet.optJSONObject("Payment")
               val Pay = Payment.optJSONObject("Pay")
               val history = Pay.optJSONObject("history")
               if (history != null) {
                   val payment = history.optJSONObject("payment")
                   if (payment != null) {
                       val transaction = payment.optString("transaction")
                       Store.paymentInfo.transaction = transaction
                   }
               }
               val payment = Pay.optJSONObject("payment")
               val message = Pay.optString("message")

                   if (payment != null) {
                           val statePaymentBankQRCodeResponsed = payment.optString("statePaymentBankQRCodeResponsed")
                       val qrContent = payment.optString("qrContent")
                       if (statePaymentBankQRCodeResponsed == "REQUIRED_TRANSFER") {
                           val popupPayment: PopupPayment = PopupPayment()
                           val bundle = Bundle()
                           bundle.putString("qrContent",qrContent)
                           popupPayment.arguments = bundle
                           loading.dismiss()
                           popupPayment.show(
                               fragmentManager,
                               "ModalBottomSheet"
                           )
                       } else {
                               onError(null,ERROR_CODE.PAYMENT_ERROR,message)
                           }
                   }else{
                       onError(null,ERROR_CODE.PAYMENT_ERROR,message)

                   }
           },onError)

    }

    fun checkMethod(
        fragmentManager: FragmentManager,
        infoPayment: InfoPayment,
        payCode: String,
        onError: (JSONObject?, Int, String?) -> Unit,
    ) {
        val checkAccount = CheckAccount()
        if(payCode== PAY_CODE.VN_PAY){
            Store.paymentInfo.methodSelected = Store.paymentInfo.listMethod[0]
            payVNPAY(fragmentManager,onError)
            return

        }
        if (payCode == PAY_CODE.PAYME && Store.userInfo.accountActive && Store.userInfo.accountKycSuccess) {
            getBalance(onSuccess = {
                checkFee(infoPayment, Store.paymentInfo.listMethod[0], onSuccess = {
                    showPopupPayment(
                        fragmentManager,
                        Store.paymentInfo.listMethod[0],
                        onError
                    )
                }, onError)
            }, onError)
            return
        }

        if (payCode == PAY_CODE.VIET_QR) {
            Store.paymentInfo.methodSelected = Store.paymentInfo.listMethod[0]
            submitPayVietQR(fragmentManager, onError)
            return
        }

        showPopupPayment(
            fragmentManager,
            Store.paymentInfo.listMethod[0],
            onError
        )
    }


    fun checkFee(
        infoPayment: InfoPayment,
        method: Method?,
        onSuccess: () -> Unit,
        onError: (JSONObject?, Int, String?) -> Unit
    ) {
        val paymentApi = PaymentApi()
        val method = method
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
                    val message = GetFee.optString("message")
                    var feeWallet = EventBus.getDefault().getStickyEvent(FeeInfo::class.java).feeWallet
                    if(method.type == TYPE_PAYMENT.WALLET){
                        feeWallet =  fee
                    }
                    EventBus.getDefault().postSticky(FeeInfo(fee,feeWallet, state, message))
                    onSuccess()


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
        payCode: String,
        onError: (JSONObject?, Int, String?) -> Unit,
    ) {
        val paymentApi = PaymentApi()
        paymentApi.getTransferMethods(
            infoPayment.storeId,
            payCode,
            onSuccess = { jsonObject ->
                val Utility = jsonObject.optJSONObject("Utility")
                val GetPaymentMethod = Utility.optJSONObject("GetPaymentMethod")
                val message = GetPaymentMethod.optString("message")
                val succeeded = GetPaymentMethod.optBoolean("succeeded")
                val methods = GetPaymentMethod.optJSONArray("methods") ?: JSONArray()
                if (succeeded) {
                    Store.paymentInfo.listMethod = arrayListOf()
                    val listMethod = arrayListOf<Method>()
                    for (i in 0 until methods.length()) {
                        val jsonObject = methods.getJSONObject(i)
                        val data = jsonObject.optJSONObject("data")
                        var dataMethod = DataMethod(null, "", "", "")
                        if (data != null) {
                            val linkedId = data.optLong("linkedId")
                            val swiftCode = data.optString("swiftCode")
                            val issuer = data.optString("issuer")
                            val supplierLinkedId = data.optString("supplierLinkedId")
                            dataMethod = DataMethod(linkedId, swiftCode, issuer, supplierLinkedId)
                        }
                        val fee = jsonObject.optInt("fee")
                        val label = jsonObject.optString("label")
                        val feeDescription = jsonObject.optString("feeDescription")
                        val methodIdRes = jsonObject.optInt("methodId")
                        val minFee = jsonObject.optInt("minFee")
                        val title = jsonObject.optString("title")
                        val type = jsonObject.optString("type")
                        val methodRes = Method(
                            dataMethod,
                            fee,
                            label,
                            methodIdRes,
                            minFee,
                            feeDescription,
                            title,
                            type,
                        )
                        listMethod.add(methodRes)
                    }
                    Store.paymentInfo.listMethod = listMethod
                    if(payCode==PAY_CODE.CREDIT){
                        Store.paymentInfo.listMethod = listMethod.filter { method -> method.type == TYPE_PAYMENT.CREDIT_CARD  } as ArrayList<Method>
                    }
                    if(payCode==PAY_CODE.ATM){
                        Store.paymentInfo.listMethod = listMethod.filter { method -> method.type == TYPE_PAYMENT.BANK_CARD  } as ArrayList<Method>
                    }
                    if(payCode==PAY_CODE.MANUAL_BANK){
                        Store.paymentInfo.listMethod = listMethod.filter { method -> method.type == TYPE_PAYMENT.BANK_TRANSFER  } as ArrayList<Method>
                    }
                    if(payCode==PAY_CODE.VIET_QR){
                        Store.paymentInfo.listMethod = listMethod.filter { method -> method.type == TYPE_PAYMENT.VIET_QR  } as ArrayList<Method>
                    }
                    if(payCode==PAY_CODE.VN_PAY){
                        Store.paymentInfo.listMethod = listMethod.filter { method -> method.type == TYPE_PAYMENT.BANK_QR_CODE  } as ArrayList<Method>
                    }

                    if (Store.paymentInfo.listMethod.size == 0) {
                        onError(
                            null,
                            ERROR_CODE.PAYMENT_ERROR,
                            PayME.context.getString(R.string.no_payment_method_found)
                        )
                    } else {
                        checkMethod(
                            fragmentManager,
                            infoPayment,
                            payCode,
                            onError
                        )
                    }

                } else {
                    onError(null, ERROR_CODE.PAYMENT_ERROR, message)
                }
            },
            onError
        )
    }


}