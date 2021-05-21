package vn.payme.sdk.payment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.component.InfoPayment
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.enums.TYPE_PAYMENT

import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.model.Info
import vn.payme.sdk.store.Store
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ResultPaymentFragment : Fragment() {
    private lateinit var buttonSubmit: Button
    private lateinit var textAmount: TextView
    private lateinit var textResult: TextView
    private lateinit var textError: TextView
    private lateinit var textTransactionCode: TextView
    private lateinit var textTransactionTime: TextView
    private lateinit var lottie: LottieAnimationView
    private lateinit var infoTop: InfoPayment
    private lateinit var infoBottom: InfoPayment


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.result_payment_layout, container, false)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        textAmount = view.findViewById(R.id.money)
        textError = view.findViewById(R.id.note_error)
        textTransactionCode = view.findViewById(R.id.transition_code_value)
        textTransactionTime = view.findViewById(R.id.transition_time_value)
        infoTop = view.findViewById(R.id.infoTop)
        infoBottom = view.findViewById(R.id.infoBottom)

        val dateFormat = SimpleDateFormat("HH:mm DD/MM/yyyy")
        val GetDate = Date()
        var DateStr: String? = dateFormat.format(GetDate)
        textTransactionTime.text = DateStr
        textTransactionCode.setText(Store.paymentInfo.transaction)
        textResult = view.findViewById(R.id.title_result)
        lottie = view.findViewById(R.id.animation_view)
        val message = arguments?.getString("message")

        if (message != null) {

        } else {
        }

        val decimal = DecimalFormat("#,###")
        textAmount.text = "${decimal.format(Store.paymentInfo.infoPayment?.amount)} đ"
        val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
        event.infoTop?.let { infoTop.updateData(it) }
        val listInfoBottom: ArrayList<Info> = arrayListOf()
        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.LINKED) {
            listInfoBottom.add(Info("Phương thức", "Tài khoản liên kết", null, null, false))
            listInfoBottom.add(
                Info(
                    "Số tài khoản",
                    Store.paymentInfo.methodSelected?.title + Store.paymentInfo.methodSelected?.label,
                    null,
                    null,
                    false
                )
            )
        } else {
            listInfoBottom.add(
                Info(
                    "Phương thức",
                    Store.paymentInfo.methodSelected?.title,
                    null,
                    null,
                    false
                )
            )
        }
        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_CARD) {
            val lengthCard = event.cardInfo?.cardNumber?.length
            val cardNumber = event.cardInfo?.cardNumber?.substring(
                lengthCard!! - 4,
                lengthCard!!
            )
            listInfoBottom.add(
                Info(
                    "Số thẻ ATM",
                    event.cardInfo?.bankShortName + "-" + cardNumber,
                    null,
                    null,
                    false
                )
            )
        }

        if (event.fee > 0) {
            listInfoBottom.add(Info("Phí", "${decimal.format(event.fee)} đ", null, null, false))
        }
        listInfoBottom[listInfoBottom.size-1].isEnd = true
        textAmount.text = event.infoBottom!![event?.infoBottom!!.size -1].value

        if(message != null){
            textError.text = message
            textError.visibility = View.VISIBLE
            lottie.setAnimation(R.raw.thatbai)
            textResult.text = getString(R.string.payment_fail)
            buttonSubmit.textView.text = getString(R.string.understood)
            infoBottom.visibility = View.GONE
        }else{
            infoBottom.updateData(listInfoBottom)
            textAmount.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        }

        buttonSubmit.setOnClickListener {
            EventBus.getDefault()
                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT, null))
        }

        return view
    }
}