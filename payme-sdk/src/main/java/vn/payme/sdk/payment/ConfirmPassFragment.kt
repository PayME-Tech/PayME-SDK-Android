package vn.payme.sdk.payment

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.PinView
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.ChangeTypePayment
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store
import java.security.MessageDigest

class ConfirmPassFragment : Fragment() {
    private lateinit var buttonClose: ImageView
    private lateinit var pinView: PinView
    private lateinit var loading: ProgressBar
    private lateinit var textForgotPassword: TextView
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
                if (code == ERROR_CODE.EXPIRED) {
                    PayME.onExpired()
                    PayME.onError(jsonObject, code, s)
                } else {
                    disableLoading()
                    PayME.showError(s)
                }


            }
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.confirm_pass, container, false)
        buttonClose = view.findViewById(R.id.buttonClose)
        pinView = view.findViewById(R.id.otp_view)
        loading = view.findViewById(R.id.loading)
        textForgotPassword = view.findViewById(R.id.txtForgotPassword)

        loading.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )
        textForgotPassword.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        textForgotPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        pinView.setAnimationEnable(true)
        pinView.requestFocus()
        pinView.isPasswordHidden = true
        Keyboard.showKeyboard(requireContext())
        textForgotPassword.setOnClickListener {
            val paymeSDK = PayME(
                PayME.context,
                Store.config.appToken,
                Store.config.publicKey,
                Store.config.connectToken,
                Store.config.appPrivateKey,
                Store.config.configColor!!,
                Store.config.language,
                Store.config.env!!,
                Store.config.showLog
            )
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
                    paymentApi.getSecuriryCode(pass!!,
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

                            if (code == ERROR_CODE.EXPIRED) {
                                PayME.onExpired()
                                PayME.onError(jsonObject, code, message)
                            } else {
                                PayME.showError(message)
                            }
                        })

                }

            }
        }
        buttonClose.setOnClickListener {
            if (loading.visibility != View.VISIBLE) {
                PayME.onError(null, ERROR_CODE.USER_CANCELLED, "")
                EventBus.getDefault()
                    .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT, null))

//                PaymePayment.closePopup(requireContext())
            }
        }
        return view
    }
}
