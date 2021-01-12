package vn.payme.sdk.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.apache.commons.codec.digest.DigestUtils
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.PinView
import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.model.Method
import java.security.MessageDigest
import java.util.*

class ConfirmPassFragment : Fragment() {
    private lateinit var buttonClose: ImageView
    private lateinit var pinView: PinView
    private var loading: Boolean = false


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
                                paymentApi.payment(method, securityCode, null, null, null, null,null,
                                    onSuccess = { jsonObject ->
                                        val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                                        val Payment = OpenEWallet.optJSONObject("Payment")
                                        val Pay = Payment.optJSONObject("Pay")
                                        val succeeded = Pay.optBoolean("succeeded")
                                        val payment = Pay.optJSONObject("OpenEWallet")
                                        val message = Pay.optString("message")
                                        if (succeeded) {
                                            println("THANH TOAN THANH XONG")
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
                                        loading = false

                                    },
                                    onError = { jsonObject, i, s ->
                                        loading = false
                                        PayME.showError(message)

                                    }
                                )

                            } else {
                                loading = false
                                PayME.showError(message)
                            }
                            pinView.setText("")


                        },
                        onError = { jsonObject, code, message ->
                            loading = false
                            PayME.showError(message)

                        }

                    )

//                    paymentApi.postTransferPVCBVerify(arguments?.getString("transferId")!!,
//                        pinView.text.toString(),
//                        onSuccess = { jsonObject ->
//                            val fragment = fragmentManager?.beginTransaction()
//                            fragment?.replace(R.id.frame_container, ResultPaymentFragment())
//                            fragment?.commit()
//
//                        },
//                        onError = { jsonObject, code, message ->
//                            loading = false
//                            if (code == 1008) {
//                                pinView.setText("")
//                                val toast: Toast =
//                                    Toast.makeText(PayME.context, message, Toast.LENGTH_SHORT)
//                                toast.view?.setBackgroundColor(
//                                    ContextCompat.getColor(
//                                        PayME.context,
//                                        R.color.scarlet
//                                    )
//                                )
//                                toast.show()
//
//
//                            } else {
//                                loading = false
//                                val bundle: Bundle = Bundle()
//                                bundle.putString("message", message)
//                                val resultPaymentFragment: ResultPaymentFragment =
//                                    ResultPaymentFragment()
//                                resultPaymentFragment.arguments = bundle
//                                val fragment = fragmentManager?.beginTransaction()
//                                fragment?.replace(R.id.frame_container, resultPaymentFragment)
//                                fragment?.commit()
//                            }
//
//
//                        })
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