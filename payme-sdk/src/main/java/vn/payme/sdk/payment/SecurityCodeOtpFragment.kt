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
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.store.Store

class SecurityCodeOtpFragment : Fragment() {
    private lateinit var pinView: PinView
    private lateinit var textNote: TextView
    private lateinit var textCountDownTimer: TextView
    private lateinit var imageOtp: ImageView
    private lateinit var loadingProgress: ProgressBar
    var isResend = false
    var loading = false

    private val timer =  object : CountDownTimer(120000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val time =  (millisUntilFinished / 1000)
            if(time > 60){
                textCountDownTimer.text = "${getString(R.string.resend_otp)} (01:${(time-60).toString()})"
            }else{
                textCountDownTimer.text = "${getString(R.string.resend_otp)} (00:${time.toString()})"
            }
        }
        override fun onFinish() {
            isResend = true
            textCountDownTimer.text = getString(R.string.resend_otp)
            textCountDownTimer.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            inflater.inflate(R.layout.payme_payment_confirm_otp_layout, container, false)
        loadingProgress = view.findViewById(R.id.loading)
        textNote = view.findViewById(R.id.text_note_otp)
        textCountDownTimer = view.findViewById(R.id.counterOtp)
        pinView = view.findViewById(R.id.otp_view)
        imageOtp = view.findViewById(R.id.image_otp)
        ChangeColorImage().changeColor(requireContext(), imageOtp, R.drawable.ic_confirm_pass, 6)
        pinView.setItemBackgroundColor(ContextCompat.getColor(PayME.context, R.color.ice))
        pinView.setAnimationEnable(true)
        pinView.requestFocus()
        pinView.isPasswordHidden = false
        Keyboard.showKeyboard(requireContext())
        textNote.text =
            "${getString(R.string.enter_the_OTP_code)} ${getString(R.string.that_was_sent_to_the_phone_number_that_registered_PayME_WALLET)}"
        pinView.addTextChangedListener { text ->
            if (text?.length == 6 && !loading) {
                checkActiveCode(text.toString())
            }
        }
        loadingProgress.indeterminateDrawable
            .mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )

        timer.start()
        textCountDownTimer.setOnClickListener {
            if (isResend) {
                isResend = false
                resendOtp()
            }
        }
        return view

    }

    override fun onStop() {
        timer.cancel()
        super.onStop()
    }

    private fun resendOtp() {
        showLoading(true)
        val paymentApi = PaymentApi()
        paymentApi.requestOtpSecurityCode(
            onSuccess = { jsonObject ->
                val Account = jsonObject.optJSONObject("Account")
                val SecurityCode = Account.optJSONObject("SecurityCode")
                val SendOTPCreateCodeByOTP = SecurityCode.optJSONObject("SendOTPCreateCodeByOTP")
                val succeeded = SendOTPCreateCodeByOTP.optBoolean("succeeded", false)
                val message = SendOTPCreateCodeByOTP.optString("message")
                if (succeeded) {
                    if (!isVisible) return@requestOtpSecurityCode
                    showLoading(false)
                    timer.start()

                } else {
                    if (!isVisible) return@requestOtpSecurityCode
                    showLoading(false)
                    PayME.showError(message)
                }
            },
            onError = { _, _, s ->
                if (!isVisible) return@requestOtpSecurityCode
                showLoading(false)
                PayME.showError(s)
            })


    }

    private fun showLoading(state: Boolean) {
        loading = state
        if (state) {
            loadingProgress.visibility = View.VISIBLE
            textCountDownTimer.visibility = View.GONE
        } else {
            loadingProgress.visibility = View.GONE
            textCountDownTimer.visibility = View.VISIBLE
        }
    }

    private fun paymentSubmit(securityCode: String) {
        val paymentApi = PaymentApi()
        showLoading(true)
        paymentApi.payment(Store.paymentInfo.methodSelected!!,
            securityCode,
            null,
            null,
            null,
            null,
            null,
            onSuccess = { jsonObject ->
                if(!isVisible) return@payment
                showLoading(false)
                val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                val Payment = OpenEWallet.optJSONObject("Payment")
                val Pay = Payment.optJSONObject("Pay")
                val succeeded = Pay.optBoolean("succeeded")
                val payment = Pay.optJSONObject("payment")
                val message = Pay.optString("message")
                val history = Pay.optJSONObject("history")
                if (succeeded) {
                    Keyboard.closeKeyboard(requireContext())
                    val payment = history.optJSONObject("payment")
                    val transaction = payment.optString("transaction")
                    Store.paymentInfo.transaction = transaction
                    EventBus.getDefault()
                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, null))
                } else {
                    if (history != null) {
                        val payment = history.optJSONObject("payment")
                        if (payment != null) {
                            val transaction = payment.optString("transaction")
                            Store.paymentInfo.transaction = transaction
                        }
                    }
                    if (payment != null) {
                        when (payment.optString("statePaymentLinkedResponsed")) {
                          "REQUIRED_VERIFY" -> {
                              val html = payment.optString("html")
                              EventBus.getDefault().post(
                                  ChangeFragmentPayment(
                                      TYPE_FRAGMENT_PAYMENT.CONFIRM_NAPAS,
                                      html
                                  )
                              )
                          }
                          "REQUIRED_OTP" -> {
                              val transaction = payment.optString("transaction")
                              Store.paymentInfo.transaction = transaction
                              EventBus.getDefault().post(
                                  ChangeFragmentPayment(
                                      TYPE_FRAGMENT_PAYMENT.CONFIRM_OTP,
                                      securityCode
                                  )
                              )
                          }
                          else -> {
                              EventBus.getDefault()
                                  .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                          }
                        }
                    } else {
                        EventBus.getDefault()
                            .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                    }

                }

            },
            onError = { _, _, s ->
                if(!isVisible) return@payment
                showLoading(true)
                PayME.showError(s)
            }
        )
    }


    private fun checkActiveCode(activeCode: String) {
        showLoading(true)
        val paymentApi = PaymentApi()
        Keyboard.closeKeyboard(requireContext())
        paymentApi.createSecurityCodeByOtp(
            activeCode,
            onSuccess = { jsonObject ->
                val Account = jsonObject.optJSONObject("Account")
                val SecurityCode = Account.optJSONObject("SecurityCode")
                val CreateCodeByOTP = SecurityCode.optJSONObject("CreateCodeByOTP")
                val succeeded = CreateCodeByOTP.optBoolean("succeeded", false)
                val message = CreateCodeByOTP.optString("message")
                val securityCode = CreateCodeByOTP.optString("securityCode")
                if (succeeded) {
                    paymentSubmit(securityCode)
                } else {
                    if (!isVisible) return@createSecurityCodeByOtp
                    pinView.setText("")
                    showLoading(false)
                    PayME.showError(message)
                }
            },
            onError = { _, _, s ->
                if (!isVisible) return@createSecurityCodeByOtp
                pinView.setText("")
                showLoading(false)
                PayME.showError(s)
            }
        )
    }
}