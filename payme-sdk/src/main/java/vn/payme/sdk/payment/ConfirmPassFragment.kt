package vn.payme.sdk.payment

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.PinView
import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.evenbus.ChangeTypePayment
import vn.payme.sdk.model.ERROR_CODE
import vn.payme.sdk.model.Method
import java.security.MessageDigest

class ConfirmPassFragment : Fragment() {
    private lateinit var buttonClose: ImageView
    private lateinit var pinView: PinView
    private lateinit var loading: ProgressBar


    fun SHA256(text: String): String? {
        val charset = Charsets.UTF_8
        val byteArray = text.toByteArray(charset)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(byteArray)
        return hash.fold("", { str, it -> str + "%02x".format(it) })
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
        loading.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(Color.parseColor(PayME.colorApp.startColor), PorterDuff.Mode.SRC_ATOP)
        pinView.setAnimationEnable(true)
        pinView.isPasswordHidden = true


        pinView.addTextChangedListener { text ->
            if (text?.length!! >= 6) {
                if (loading.visibility != View.VISIBLE) {
                    loading.visibility = View.VISIBLE
                    pinView.visibility = View.GONE
                    val paymentApi = PaymentApi()
                    val pass: String? = SHA256(text.toString())
                    paymentApi.getSecuriryCode(pass!!,
                        onSuccess = { jsonObject ->
                            val Account = jsonObject.optJSONObject("Account")
                            val SecurityCode = Account.optJSONObject("SecurityCode")
                            val CreateCodeByPassword =
                                SecurityCode.optJSONObject("CreateCodeByPassword")
                            val message = CreateCodeByPassword.optString("message")
                            val securityCode = CreateCodeByPassword.optString("securityCode")
                            val succeeded = CreateCodeByPassword.optBoolean("succeeded")
                            if (succeeded) {
                                val method = Method(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    TYPE_PAYMENT.WALLET
                                )
                                val paymentApi = PaymentApi()
                                paymentApi.payment(method,
                                    securityCode,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    onSuccess = { jsonObject ->
                                        val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                                        val Payment = OpenEWallet.optJSONObject("Payment")
                                        val Pay = Payment.optJSONObject("Pay")

                                        val succeeded = Pay.optBoolean("succeeded")
                                        val payment = Pay.optJSONObject("OpenEWallet")
                                        val message = Pay.optString("message")
                                        if (succeeded) {
                                            val history = Pay.optJSONObject("history")
                                            val payment = history.optJSONObject("payment")
                                            val transaction = payment.optString("transaction")
                                            PayME.transaction = transaction
                                            val fragment = fragmentManager?.beginTransaction()
                                            fragment?.replace(
                                                R.id.frame_container,
                                                ResultPaymentFragment()
                                            )
                                            fragment?.commit()

                                        } else {
                                            println("THAIBAI G")

                                            val bundle: Bundle = Bundle()
                                            bundle.putString("message", message)
                                            val resultPaymentFragment: ResultPaymentFragment =
                                                ResultPaymentFragment()
                                            resultPaymentFragment.arguments = bundle
                                            val fragment = fragmentManager?.beginTransaction()
                                            fragment?.replace(
                                                R.id.frame_container,
                                                resultPaymentFragment
                                            )
                                            fragment?.commit()
                                        }
                                        pinView.visibility = View.VISIBLE
                                        loading.visibility = View.GONE


                                    },
                                    onError = { jsonObject, i, s ->
                                        loading.visibility = View.GONE
                                        pinView.visibility = View.VISIBLE
                                        pinView.requestFocus()
                                        PayME.showError(message)

                                    }
                                )

                            } else {
                                pinView.visibility = View.VISIBLE
                                pinView.requestFocus()
                                loading.visibility = View.GONE
                                PayME.showError(message)
                            }
                            pinView.setText("")


                        },
                        onError = { jsonObject, code, message ->
                            pinView.visibility = View.VISIBLE
                            pinView.requestFocus()
                            loading.visibility = View.GONE
                            if (code == ERROR_CODE.EXPIRED) {
                                PayME.onExpired()
                                PayME.onError(jsonObject, code, message)

                            } else {
                                PayME.showError(message)
                            }

                        }

                    )

                }

            }

        }



        buttonClose.setOnClickListener {
            if (loading.visibility != View.VISIBLE) {
                val fragment = fragmentManager?.beginTransaction()
                fragment?.replace(R.id.frame_container, SelectMethodFragment())
                fragment?.commit()
            }

        }



        return view
    }
}