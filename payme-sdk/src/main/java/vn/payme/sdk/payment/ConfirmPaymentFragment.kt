package vn.payme.sdk.payment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.component.InfoPayment
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.model.Info
import vn.payme.sdk.store.Store
import java.text.DecimalFormat


class ConfirmPaymentFragment : Fragment() {
    private lateinit var buttonClose: ImageView
    private lateinit var buttonBack: ImageView

    private lateinit var buttonSubmit: Button
    private lateinit var infoTop: InfoPayment
    private lateinit var infoBottom: InfoPayment
    private  var getFeeSuccess = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.confirm_payment_fragment, container, false)
        buttonClose = view.findViewById(R.id.buttonClose)
        buttonBack = view.findViewById(R.id.buttonBack)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        infoTop = view.findViewById(R.id.infoTop)
        infoBottom = view.findViewById(R.id.infoBottom)

        buttonClose.setOnClickListener {
            if (!buttonSubmit.isLoadingShowing) {
                PayME.onError(null, ERROR_CODE.USER_CANCELLED, "")
                EventBus.getDefault().post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT,null))
//                PaymePayment.closePopup(requireContext())
            }
        }
        buttonBack.setOnClickListener {
            val fragment = parentFragmentManager.beginTransaction()
            fragment.replace(R.id.frame_container,SelectMethodFragment())
            fragment.commit()
        }
        if (Store.paymentInfo.methodSelected?.type  != TYPE_PAYMENT.BANK_CARD && !Store.paymentInfo.isChangeMethod){
            buttonBack.visibility = View.GONE
        }
        getFee()
        buttonSubmit.setOnClickListener {
            if(getFeeSuccess){
                onPay()

            }else{
                getFee()
            }
        }
        return view
    }
    fun getFee(){
        var listInfoTop = arrayListOf<Info>()
        var listInfoBottom = arrayListOf<Info>()
        val decimal = DecimalFormat("#,###")
        val method = Store.paymentInfo.methodSelected
        val storeName = Store.userInfo.dataInit?.optString("storeName")
        listInfoTop.add(Info("Dịch vụ", storeName, null, null, false))
        listInfoTop.add(
            Info(
                "Số tiền thanh toán",
                "${decimal.format(Store.paymentInfo.infoPayment?.amount)} đ",
                null,
                Color.parseColor(Store.config.colorApp.startColor),
                false
            )
        )
        listInfoTop.add(Info("Nội dung", Store.paymentInfo.infoPayment?.note, null, null, true))

        if (method?.type == TYPE_PAYMENT.LINKED) {
            listInfoBottom.add(Info("Phương thức", "Tài khoản liên kết", null, null, false))
            listInfoBottom.add(
                Info(
                    "Số tài khoản",
                    Store.paymentInfo.methodSelected?.title + Store.paymentInfo.methodSelected?.label,
                    null,
                    null,
                    false
                )
            )
        } else {
            listInfoBottom.add(
                Info(
                    "Phương thức",
                    Store.paymentInfo.methodSelected?.title,
                    null,
                    null,
                    false
                )
            )
            if (method?.type == TYPE_PAYMENT.BANK_CARD){
                val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
                listInfoBottom.add(Info("Ngân hàng", event.cardInfo?.bankShortName, null, null, false))
                listInfoBottom.add(Info("Số thẻ ATM", event.cardInfo?.cardNumberView, null, null, false))
                listInfoBottom.add(Info("Họ tên chủ thẻ", event.cardInfo?.cardHolder, null, null, false))
                listInfoBottom.add(Info("Ngày phát hành", event.cardInfo?.cardDateView, null, null, false))

            }
        }
        val paymentApi = PaymentApi()
        buttonSubmit.enableLoading()
        paymentApi.getFee(Store.paymentInfo.amount, onSuccess = { jsonObject ->
            val Utility = jsonObject.getJSONObject("Utility")
            val GetFee = Utility.getJSONObject("GetFee")
            val succeeded = GetFee.getBoolean("succeeded")
            val message = GetFee.getString("message")
            val decimal = DecimalFormat("#,###")
            buttonSubmit.disableLoading()
            if (succeeded) {
                val feeObject = GetFee.getJSONObject("fee")
                val fee = feeObject.getInt("fee")
                listInfoBottom.add(Info("Phí", "${decimal.format(fee)} đ", null, null, false))
                listInfoBottom.add(
                    Info(
                        "Tổng thanh toán", "${
                            decimal.format(
                                Store.paymentInfo.infoPayment?.amount?.plus(
                                    fee
                                )
                            )
                        } đ", null, resources.getColor(R.color.red), true
                    )
                )
                infoTop.updateData(listInfoTop)
                infoBottom.updateData(listInfoBottom)
                EventBus.getDefault().postSticky(PaymentInfoEvent(listInfoTop,listInfoBottom,null))
                getFeeSuccess = true
                buttonSubmit.setText("Xác nhận")
            } else {
                buttonSubmit.setText("Thử lại")
                PayME.showError(message)
            }
        }, onError = { jsonObject: JSONObject?, code: Int?, message: String ->
            buttonSubmit.disableLoading()
            buttonSubmit.setText("Thử lại")
            PayME.showError(message)
        })

    }

    private fun onPay() {
        val paymentApi = PaymentApi()
        buttonSubmit.enableLoading()
        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_CARD) {
            val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
            paymentApi.payment(
                Store.paymentInfo.methodSelected!!,
                "",
                event.cardInfo?.cardNumber,
                event.cardInfo?.cardHolder,
                event.cardInfo?.cardDate,
                "",
                "",
                onSuccess = { jsonObject ->
                    buttonSubmit.disableLoading()
                    val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                    val Payment = OpenEWallet.optJSONObject("Payment")
                    val Pay = Payment.optJSONObject("Pay")
                    val succeeded = Pay.optBoolean("succeeded")
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
                    val statePaymentBankCardResponsed =
                        payment.optString("statePaymentBankCardResponsed")
                    if (statePaymentBankCardResponsed == "REQUIRED_VERIFY") {
                        val html = payment.optString("html")
                        EventBus.getDefault()
                            .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CONFIRM_NAPAS, html))
                    } else {
                        PayME.showError(message)

                    }
                },
                onError = { jsonObject, code, message ->
                    PayME.showError(message)
                    buttonSubmit.disableLoading()
                })

        } else {
            EventBus.getDefault()
                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CONFIRM_PASS, null))
        }

    }
}