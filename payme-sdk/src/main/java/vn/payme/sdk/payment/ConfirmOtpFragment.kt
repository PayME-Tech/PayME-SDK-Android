package vn.payme.sdk.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.component.PinView
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TypeCallBack

class ConfirmOtpFragment : Fragment() {
    private lateinit var buttonSubmit: Button
    private lateinit var buttonClose: ImageView
    private lateinit var pinView: PinView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.confirm_otp_layout, container, false)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        buttonClose = view.findViewById(R.id.buttonClose)
        pinView = view.findViewById(R.id.otp_view)
        buttonSubmit.background = PayME.colorApp.backgroundColorRadius
        pinView.setItemBackgroundColor(ContextCompat.getColor(PayME.context, R.color.ice))
        pinView.setAnimationEnable(true)
        pinView.requestFocus()
        pinView.isPasswordHidden = false
        Keyboard.showKeyboard(requireContext())
        pinView.addTextChangedListener { text ->
            if (text?.length == 6) {
                checkPassword(text.toString())
            }
        }
        buttonSubmit.setOnClickListener {
            if (pinView.text?.length === 6 && !buttonSubmit.isLoadingShowing) {
                checkPassword(pinView.text.toString())
            }

        }
        buttonClose.setOnClickListener {
            if (!buttonSubmit.isLoadingShowing) {
                PayME.onError(null, ERROR_CODE.USER_CANCELLED,"")
                PaymePayment.closePopup(requireContext())
            }
        }
        return view
    }


    fun checkPassword(pass: String) {
        buttonSubmit.enableLoading()
        val paymentApi = PaymentApi()
        val transaction = arguments?.getString("transaction")
        PayME.methodSelected?.let {
            paymentApi.payment(
                it,
                null,
                null,
                null,
                null,
                pass,
                transaction,
                onSuccess = { jsonObject ->
                    buttonSubmit.disableLoading()
                    val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                    val Payment = OpenEWallet.optJSONObject("Payment")
                    val Pay = Payment.optJSONObject("Pay")
                    val succeeded = Pay.optBoolean("succeeded")
                    val payment = Pay.optJSONObject("payment")
                    val history = Pay.optJSONObject("history")
                    val message = Pay.optString("message")
                    if (succeeded) {
                        Keyboard.closeKeyboard(requireContext())
                        PaymePayment.onPaymentSuccess(history,requireContext(), requireFragmentManager())

                    } else {
                        if (payment != null) {
                            val statePaymentLinkedResponsed =
                                payment.optString("statePaymentLinkedResponsed")
                            if (statePaymentLinkedResponsed == "INVALID_OTP") {
                                pinView.setText("")
                                PayME.showError(message)
                            } else {
                                Keyboard.closeKeyboard(requireContext())
                                PaymePayment.onPaymentError(message,requireContext(), requireFragmentManager())
                            }
                        } else {
                            Keyboard.closeKeyboard(requireContext())
                            PaymePayment.onPaymentError(message,requireContext(), requireFragmentManager())

                        }
                    }

                },
                onError = { jsonObject, code, message ->
                    buttonSubmit.disableLoading()
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