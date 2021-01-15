package vn.payme.sdk.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.chaos.view.PinView
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.model.ERROR_CODE

class ConfirmOtpFragment : Fragment() {
    private lateinit var buttonSubmit: Button
    private lateinit var buttonClose: ImageView
    private lateinit var pinView: PinView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater?.inflate(R.layout.confirm_otp_layout, container, false)
        buttonSubmit = view!!.findViewById(R.id.buttonSubmit)
        buttonClose = view!!.findViewById(R.id.buttonClose)
        pinView = view!!.findViewById(R.id.otp_view)
        buttonSubmit.background = PayME.colorApp.backgroundColorRadius
        pinView.setItemBackgroundColor(ContextCompat.getColor(PayME.context, R.color.ice))
        pinView.setAnimationEnable(true)
        pinView.requestFocus()
        pinView.isPasswordHidden = false


        pinView.addTextChangedListener { text ->
            if(text?.length==6){
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
                val fragment = fragmentManager?.beginTransaction()
                fragment?.replace(R.id.frame_container, SelectMethodFragment())
                fragment?.commit()
            }

        }




        return view
    }

    fun checkPassword(pass: String) {
        buttonSubmit.enableLoading()
        val paymentApi = PaymentApi()
        val transaction = arguments?.getString("transaction")
        paymentApi.payment(PayME.methodSelected,
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
                val message = Pay.optString("message")
                if (succeeded) {
                    val fragment = fragmentManager?.beginTransaction()
                    fragment?.replace(R.id.frame_container, ResultPaymentFragment())
                    fragment?.commit()
                } else {
                    println("payment:"+payment)

                    if (payment != null) {
                        val statePaymentLinkedResponsed = payment.optString("statePaymentLinkedResponsed")
                        if(statePaymentLinkedResponsed=="INVALID_OTP"){
                            println("statePaymentLinkedResponsed=INVALID_OTP")
                            pinView.setText("")
                            PayME.showError(message)
                        }else{
                            val bundle: Bundle = Bundle()
                            bundle.putString("message", message)
                            val resultPaymentFragment = ResultPaymentFragment()
                            resultPaymentFragment.arguments = bundle
                            val fragment = fragmentManager?.beginTransaction()
                            fragment?.replace(R.id.frame_container, resultPaymentFragment)
                            fragment?.commit()
                        }


                    } else {
                        val bundle: Bundle = Bundle()
                        bundle.putString("message", message)
                        val resultPaymentFragment = ResultPaymentFragment()
                        resultPaymentFragment.arguments = bundle
                        val fragment = fragmentManager?.beginTransaction()
                        fragment?.replace(R.id.frame_container, resultPaymentFragment)
                        fragment?.commit()
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