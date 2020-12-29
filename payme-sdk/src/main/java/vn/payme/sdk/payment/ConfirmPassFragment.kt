package vn.payme.sdk.payment

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.PinView

class ConfirmPassFragment : Fragment() {
    private lateinit var buttonClose: ImageView
    private lateinit var pinView: PinView
    private var loading: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater?.inflate(R.layout.confirm_pass, container, false)
        buttonClose = view!!.findViewById(R.id.buttonClose)
        pinView = view!!.findViewById(R.id.otp_view)
        pinView.setAnimationEnable(true)
        pinView.requestFocus()
        pinView.isPasswordHidden = true


        pinView.addTextChangedListener { text ->
            if (text?.length!! >= 6) {
                if (!loading) {
                    loading = true
                    val paymentApi = PaymentApi()

                    paymentApi.postTransferPVCBVerify(arguments?.getString("transferId")!!,
                        pinView.text.toString(),
                        onSuccess = { jsonObject ->
                            val fragment = fragmentManager?.beginTransaction()
                            fragment?.replace(R.id.frame_container, ResultPaymentFragment())
                            fragment?.commit()

                        },
                        onError = { jsonObject, code, message ->
                            loading = false
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
                                loading = false
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

        }


//        buttonSubmit.setOnClickListener {
//            if (pinView.text?.length === 6 && !buttonSubmit.isLoadingShowing) {
//                buttonSubmit.enableLoading()

//
//            }
//
//        }
        buttonClose.setOnClickListener {
            if (!loading) {
                val fragment = fragmentManager?.beginTransaction()
                fragment?.replace(R.id.frame_container, SelectMethodFragment())
                fragment?.commit()
            }

        }



        return view
    }
}