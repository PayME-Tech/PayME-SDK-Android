package vn.payme.sdk.payment

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.devs.vectorchildfinder.VectorChildFinder
import com.devs.vectorchildfinder.VectorDrawableCompat
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.PinView
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.store.Store
import java.security.MessageDigest


class ConfirmPassFragment : Fragment() {
    private lateinit var pinView: PinView
    private lateinit var loading: ProgressBar
    private lateinit var textForgotPassword: TextView
    private lateinit var imageConfirm: ImageView
    fun SHA256(text: String): String? {
        val charset = Charsets.UTF_8
        val byteArray = text.toByteArray(charset)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(byteArray)
        return hash.fold("", { str, it -> str + "%02x".format(it) })
    }

    fun showLoading() {
        loading.visibility = View.VISIBLE
        pinView.visibility = View.GONE
        textForgotPassword.visibility = View.GONE
    }

    fun disableLoading() {
        pinView.visibility = View.VISIBLE
        textForgotPassword.visibility = View.VISIBLE
        pinView.requestFocus()
        loading.visibility = View.GONE
    }

    private fun paymentSubmit(securityCode: String) {
        val paymentApi = PaymentApi()
        showLoading()
        paymentApi.payment(Store.paymentInfo.methodSelected!!,
            securityCode,
            null,
            null,
            null,
            null,
            null,
            onSuccess = { jsonObject ->
                disableLoading()
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
                        val statePaymentLinkedResponsed =
                            payment.optString("statePaymentLinkedResponsed")
                        if (statePaymentLinkedResponsed == "REQUIRED_VERIFY") {
                            val html = payment.optString("html")
                            EventBus.getDefault().post(
                                ChangeFragmentPayment(
                                    TYPE_FRAGMENT_PAYMENT.CONFIRM_NAPAS,
                                    html
                                )
                            )
                        } else if (statePaymentLinkedResponsed == "REQUIRED_OTP") {
                            val transaction = payment.optString("transaction")
                            Store.paymentInfo.transaction = transaction
                            EventBus.getDefault().post(
                                ChangeFragmentPayment(
                                    TYPE_FRAGMENT_PAYMENT.CONFIRM_OTP,
                                    securityCode
                                )
                            )
                        } else {
                            EventBus.getDefault()
                                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                        }
                    } else {
                        EventBus.getDefault()
                            .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                    }

                }

            },
            onError = { jsonObject, code, s ->
                    disableLoading()
                    PayME.showError(s)
            }
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.confirm_pass, container, false)
        pinView = view.findViewById(R.id.otp_view)
        imageConfirm = view.findViewById(R.id.image_otp)

        loading = view.findViewById(R.id.loading)
        textForgotPassword = view.findViewById(R.id.txtForgotPassword)

        loading.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )
        ChangeColorImage().changeColor(requireContext(),imageConfirm,R.drawable.ic_confirm_pass,6)

        textForgotPassword.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        textForgotPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        pinView.setAnimationEnable(true)
        pinView.requestFocus()
        pinView.isPasswordHidden = true
        pinView.background = Store.config.colorApp.backgroundColorRadiusBorder
        Keyboard.showKeyboard(requireContext())
        textForgotPassword.setOnClickListener {
            val paymeSDK = PayME()
            Keyboard.closeKeyboard(requireContext())
            paymeSDK.onForgotPassword()
            EventBus.getDefault()
                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT, null))

        }
        pinView.addTextChangedListener { text ->
            if (text?.length!! >= 6) {
                if (loading.visibility != View.VISIBLE) {
                    loading.visibility = View.VISIBLE
                    pinView.visibility = View.GONE
                    val paymentApi = PaymentApi()
                    val pass: String? = SHA256(text.toString())
                    showLoading()
                    paymentApi.getSecurityCode(pass!!,
                        onSuccess = { jsonObject ->
                            Keyboard.closeKeyboard(requireContext())
                            val Account = jsonObject.optJSONObject("Account")
                            val SecurityCode = Account.optJSONObject("SecurityCode")
                            val CreateCodeByPassword =
                                SecurityCode.optJSONObject("CreateCodeByPassword")
                            val message = CreateCodeByPassword.optString("message")
                            val securityCode = CreateCodeByPassword.optString("securityCode")
                            val succeeded = CreateCodeByPassword.optBoolean("succeeded")
                            if (succeeded) {
                                paymentSubmit(securityCode)
                            } else {

                                disableLoading()
                                pinView.setText("")
                                PayME.showError(message)
                            }

                        }, onError = { jsonObject, code, message ->
                            disableLoading()
                            PayME.showError(message)
                        })

                }

            }
        }
        return view
    }

}
