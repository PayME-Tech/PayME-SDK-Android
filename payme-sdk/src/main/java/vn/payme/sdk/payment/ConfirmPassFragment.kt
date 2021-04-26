package vn.payme.sdk.payment

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
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
import vn.payme.sdk.evenbus.ChangeTypePayment
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
   fun showLoading(){
       loading.visibility = View.VISIBLE
       pinView.visibility = View.GONE
    }
    fun  disableLoading(){
        pinView.visibility = View.VISIBLE
        pinView.requestFocus()
        loading.visibility = View.GONE
    }
    private fun paymentLinkedBank(method: Method) {
        val paymentApi = PaymentApi()
        showLoading()
        var even: EventBus = EventBus.getDefault()
        var myEven: ChangeTypePayment = ChangeTypePayment(TYPE_PAYMENT.PAYMENT_RESULT, "",null)
        paymentApi.payment(method, null, null, null, null, null, null,
            onSuccess = { jsonObject ->
                disableLoading()
                val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                val Payment = OpenEWallet.optJSONObject("Payment")
                val Pay = Payment.optJSONObject("Pay")
                val succeeded = Pay.optBoolean("succeeded")
                val payment = Pay.optJSONObject("payment")
                val message = Pay.optString("message")
                val history = Pay.optJSONObject("history")
                myEven.data = history
                if (succeeded) {
                    Keyboard.closeKeyboard(requireContext())
                    val payment = history.optJSONObject("payment")
                    val transaction = payment.optString("transaction")
                    PayME.transaction = transaction
                    even.post(myEven)
                } else {
                    if (payment != null) {
                        val statePaymentLinkedResponsed =
                            payment.optString("statePaymentLinkedResponsed")
                        if (statePaymentLinkedResponsed == "REQUIRED_VERIFY") {
                            val html = payment.optString("html")
                            var changeFragmentOtp: ChangeTypePayment =
                                ChangeTypePayment(TYPE_PAYMENT.CONFIRM_OTP_BANK_NAPAS, html,null)
                            even.post(changeFragmentOtp)
                        } else if (statePaymentLinkedResponsed == "REQUIRED_OTP") {
                            val transaction = payment.optString("transaction")
                            PayME.transaction = transaction
                            var changeFragmentOtp: ChangeTypePayment =
                                ChangeTypePayment(TYPE_PAYMENT.CONFIRM_OTP_BANK, transaction,null)
                            even.post(changeFragmentOtp)
                        } else {
                            myEven.value = message
                            even.post(myEven)
                        }
                    } else {
                        myEven.value = message
                        even.post(myEven)
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
        loading.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(Color.parseColor(PayME.colorApp.startColor), PorterDuff.Mode.SRC_ATOP)
        pinView.setAnimationEnable(true)
        pinView.requestFocus()
        pinView.isPasswordHidden = true
        Keyboard.showKeyboard(requireContext())
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
                                val paymentApi = PaymentApi()
                                if(PayME.methodSelected?.type!=TYPE_PAYMENT.WALLET){
                                    paymentLinkedBank(PayME.methodSelected!!)
                                }else{

                                paymentApi.payment(PayME.methodSelected!!,
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
                                            Keyboard.closeKeyboard(requireContext())
                                            val history = Pay.optJSONObject("history")
                                            val payment = history.optJSONObject("payment")
                                            val transaction = payment.optString("transaction")
                                            PayME.transaction = transaction
                                            Keyboard.closeKeyboard(requireContext())
                                            PaymePayment.onPaymentSuccess(history,requireContext(),requireFragmentManager())

                                        } else {
                                            Keyboard.closeKeyboard(requireContext())
                                            PaymePayment.onPaymentError(message,requireContext(),requireFragmentManager())
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
                                }


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
                PayME.onError(null, ERROR_CODE.USER_CANCELLED,"")
                PaymePayment.closePopup(requireContext())
            }
        }
        return view
    }
}