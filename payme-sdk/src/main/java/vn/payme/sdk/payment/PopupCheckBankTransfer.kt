package vn.payme.sdk.payment

import android.app.Dialog
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.SimpleLottieValueCallback
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.store.Store


class PopupCheckBankTransfer : DialogFragment() {
    lateinit var buttonClose: LinearLayout
    lateinit var containerCheck: ConstraintLayout
    lateinit var containerResult: ConstraintLayout
    lateinit var buttonSubmit: Button
    lateinit var lottie: LottieAnimationView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        val view = inflater.inflate(R.layout.payment_popup_check_transaction, container)
        lottie = view.findViewById(R.id.lottie_check)
        buttonClose = view.findViewById(R.id.buttonClose)
        containerCheck = view.findViewById(R.id.containerCheck)
        containerResult = view.findViewById(R.id.containerResult)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)

        buttonClose.setOnClickListener {
            dismiss()
        }
        buttonSubmit.setOnClickListener {
            dismiss()
        }
        onPay()

        loadAnimation()
        EventBus.getDefault().register(this)

        return view

    }

    private fun onPay() {
        val paymentApi = PaymentApi()
        Keyboard.closeKeyboard(requireContext())
        paymentApi.payment(
            Store.paymentInfo.methodSelected!!,
            "",
            null,
            "",
            "",
            true,
            onSuccess = { jsonObject ->
                if (!isVisible) return@payment
                val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                val Payment = OpenEWallet.optJSONObject("Payment")
                val Pay = Payment.optJSONObject("Pay")
                val succeeded = Pay.optBoolean("succeeded")
                val history = Pay.optJSONObject("history")
                if (history != null) {
                    val payment = history.optJSONObject("payment")
                    if (payment != null) {
                        val transaction = payment.optString("transaction")
                        Store.paymentInfo.transaction = transaction
                    }
                }
                val payment = Pay.optJSONObject("payment")
                val message = Pay.optString("message")
                if (succeeded) {
                    dismiss()
                    EventBus.getDefault()
                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, null))
                } else {
                    if (payment != null) {
                        val statePaymentBankTransferResponsed =
                            payment.optString("statePaymentBankTransferResponsed")
                        if (statePaymentBankTransferResponsed == "FAILED") {
                            containerCheck.visibility = View.GONE
                            containerResult.visibility = View.VISIBLE
                        } else if (statePaymentBankTransferResponsed == "SUCCEEDED") {
                            dismiss()
                            EventBus.getDefault()
                                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, null))

                        }else{
                            dismiss()
                            EventBus.getDefault()
                                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                        }
                    } else {
                        dismiss()
                        EventBus.getDefault()
                            .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                    }
                }


            },
            onError = { jsonObject, code, message ->
                if (!isVisible) return@payment
                PayME.showError(message)
                dismiss()
            })
    }

    @Subscribe
    fun onText(myEven: MyEven) {
        if (myEven.type == TypeCallBack.onExpired) {
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.isCancelable = false
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, R.layout.payment_layout, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

    fun loadAnimation() {
        lottie.addValueCallback<ColorFilter>(
            KeyPath("Muiten", "**"),
            LottieProperty.COLOR_FILTER,
            SimpleLottieValueCallback<ColorFilter?> {
                PorterDuffColorFilter(
                    Color.parseColor(Store.config.colorApp.startColor),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        )
        lottie.addValueCallback<ColorFilter>(
            KeyPath("D_xanh", "Group 3", "**"),
            LottieProperty.COLOR_FILTER,
            SimpleLottieValueCallback<ColorFilter?> {
                PorterDuffColorFilter(
                    Color.parseColor(Store.config.colorApp.startColor),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        )

        lottie.playAnimation()

    }
}