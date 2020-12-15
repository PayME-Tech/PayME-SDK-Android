package vn.payme.sdk.payment

import android.graphics.Color
import android.graphics.ColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chaos.view.PinView
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button

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


        buttonSubmit.setOnClickListener {
            if (pinView.text?.length === 6 && !buttonSubmit.isLoadingShowing) {
                buttonSubmit.enableLoading()
                val paymentApi = PaymentApi()
                paymentApi.postTransferPVCBVerify(arguments?.getString("transferId")!!,
                    pinView.text.toString(),
                    onSuccess = { jsonObject ->
                        val fragment = fragmentManager?.beginTransaction()
                        fragment?.replace(R.id.frame_container, ResultPaymentFragment())
                        fragment?.commit()

                    },
                    onError = { jsonObject, code, message ->
                        buttonSubmit.disableLoading()
                        if (code == 1008) {
                            pinView.setText("")
                            val toast: Toast =
                                Toast.makeText(PayME.context, message, Toast.LENGTH_SHORT)
                            toast.view?.setBackgroundColor(
                                ContextCompat.getColor(
                                    PayME.context,
                                    R.color.scarlet
                                )
                            )
                            toast.show()


                        } else {
                            buttonSubmit.disableLoading()

                            val bundle: Bundle = Bundle()
                            bundle.putString("message", message)
                            val resultPaymentFragment: ResultPaymentFragment =
                                ResultPaymentFragment()
                            resultPaymentFragment.arguments = bundle
                            val fragment = fragmentManager?.beginTransaction()
                            fragment?.replace(R.id.frame_container, resultPaymentFragment)
                            fragment?.commit()
                        }


                    })

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
}