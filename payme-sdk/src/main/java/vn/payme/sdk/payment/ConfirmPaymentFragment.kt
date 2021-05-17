package vn.payme.sdk.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus

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
import vn.payme.sdk.store.Store


class ConfirmPaymentFragment : Fragment() {
    private lateinit var buttonClose: ImageView
    private lateinit var buttonBack: ImageView

    private lateinit var buttonSubmit: Button
    private lateinit var infoTop: InfoPayment
    private lateinit var infoBottom: InfoPayment

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
                PaymePayment.closePopup(requireContext())
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
        val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
        infoTop.updateData(event.infoTop)
        infoBottom.updateData(event.infoBottom)
        buttonSubmit.setOnClickListener {
            onPay()
        }
        return view
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