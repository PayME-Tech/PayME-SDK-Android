package vn.payme.sdk.payment

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.PinView
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.store.Store

class ConfirmOtpFragment : Fragment() {
    private lateinit var buttonClose: ImageView
    private lateinit var pinView: PinView
    private lateinit var textNote: TextView
    private lateinit var textCountDownTimer: TextView
    private lateinit var loadingProgress: ProgressBar
    var isResend = false
    var loading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.confirm_otp_layout, container, false)
        loadingProgress = view.findViewById(R.id.loading)
        buttonClose = view.findViewById(R.id.buttonClose)
        textNote = view.findViewById(R.id.text_note_otp)
        textCountDownTimer = view.findViewById(R.id.counterOtp)
        pinView = view.findViewById(R.id.otp_view)
        pinView.setItemBackgroundColor(ContextCompat.getColor(PayME.context, R.color.ice))
        pinView.setAnimationEnable(true)
        pinView.requestFocus()
        pinView.isPasswordHidden = false
        Keyboard.showKeyboard(requireContext())
        textNote.text =
            "Nhập mã OTP ${Store.paymentInfo.methodSelected?.title} đã được gửi qua số điện thoại đăng ký thẻ"
        pinView.addTextChangedListener { text ->
            if (text?.length == 6 && !loading) {
                checkPassword(text.toString())
            }
        }
        loadingProgress.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )

        buttonClose.setOnClickListener {
            if (!loading) {
                PayME.onError(null, ERROR_CODE.USER_CANCELLED, "")
                EventBus.getDefault()
                    .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT, null))

            }
        }
//        timer.start()
        textCountDownTimer.setOnClickListener {
            if(isResend){
                isResend = false
                resendOtp()
            }
        }
        return view

    }
    private fun resendOtp() {
        showLoading(true)
        val securityCode = arguments?.getString("securityCode")
        val paymentApi = PaymentApi()
        paymentApi.payment(Store.paymentInfo.methodSelected!!,
            securityCode,
            null,
            null,
            null,
            null,
            null,
            onSuccess = { jsonObject ->
                showLoading(false)
                val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                val Payment = OpenEWallet.optJSONObject("Payment")
                val Pay = Payment.optJSONObject("Pay")
                val payment = Pay.optJSONObject("payment")
                val message = Pay.optString("message")
                if(payment!=null){
                    val statePaymentLinkedResponsed =
                        payment.optString("statePaymentLinkedResponsed")
                    if (statePaymentLinkedResponsed == "REQUIRED_OTP") {
                        val transaction = payment.optString("transaction")
                        Store.paymentInfo.transaction = transaction
//                        timer.start()
                    } else {
                        EventBus.getDefault()
                            .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                    }
                }else{
                    EventBus.getDefault()
                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                }

            },
            onError = { jsonObject, code, s ->
                showLoading(false)
                if (code == ERROR_CODE.EXPIRED) {
                    PayME.onExpired()
                    PayME.onError(jsonObject, code, s)
                } else {
                    PayME.showError(s)
                }


            }
        )
    }

//      val timer =  object : CountDownTimer(120000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                val time =  (millisUntilFinished / 1000)
//                if(time > 60){
//                    textCountDownTimer.text = "${getString(R.string.resend_otp)} (01:${(time-60).toString()})"
//                }else{
//                    textCountDownTimer.text = "${getString(R.string.resend_otp)} (00:${time.toString()})"
//                }
//            }
//            override fun onFinish() {
//                isResend = true
//                textCountDownTimer.text = getString(R.string.resend_otp)
//                textCountDownTimer.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
//            }
//        }


    fun showLoading(state: Boolean) {
        loading = state
        if(state){
            loadingProgress.visibility = View.VISIBLE
            textCountDownTimer.visibility = View.GONE
        }else{
            loadingProgress.visibility = View.GONE
            textCountDownTimer.visibility = View.GONE
        }

    }


    fun checkPassword(pass: String) {
        showLoading(true)
        val paymentApi = PaymentApi()
        val transaction = Store.paymentInfo.transaction
        paymentApi.payment(
            Store.paymentInfo.methodSelected!!,
            null,
            null,
            null,
            null,
            pass,
            transaction,
            onSuccess = { jsonObject ->
                showLoading(false)
                val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                val Payment = OpenEWallet.optJSONObject("Payment")
                val Pay = Payment.optJSONObject("Pay")
                val succeeded = Pay.optBoolean("succeeded")
                val payment = Pay.optJSONObject("payment")
                val history = Pay.optJSONObject("history")
                val message = Pay.optString("message")
                if (succeeded) {
                    Keyboard.closeKeyboard(requireContext())
                    val payment = history.optJSONObject("payment")
                    val transaction = payment.optString("transaction")
                    Store.paymentInfo.transaction = transaction
                    EventBus.getDefault()
                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, null))
                } else {
                    if (payment != null) {
                        val statePaymentLinkedResponsed =
                            payment.optString("statePaymentLinkedResponsed")
                        if (statePaymentLinkedResponsed == "INVALID_OTP") {
                            pinView.setText("")
                            PayME.showError(message)
                        } else {
                            Keyboard.closeKeyboard(requireContext())
                            EventBus.getDefault()
                                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                        }
                    } else {
                        Keyboard.closeKeyboard(requireContext())
                        EventBus.getDefault()
                            .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))

                    }
                }

            },
            onError = { jsonObject, code, message ->
                showLoading(false)
                if (code == ERROR_CODE.EXPIRED) {
                    PayME.onExpired()
                    PayME.onError(jsonObject, code, message)
                } else {
                    PayME.showError(message)
                }


            })
    }

}