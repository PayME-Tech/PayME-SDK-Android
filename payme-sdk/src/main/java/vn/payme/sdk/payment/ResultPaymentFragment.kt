package vn.payme.sdk.payment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.component.InfoPayment
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.model.Info
import vn.payme.sdk.store.Store
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

        if(message!=null){
            textError.text = message
            textError.visibility = View.VISIBLE
            lottie.setAnimation(R.raw.result_that_bai)
            textResult.text = getString(R.string.payment_fail)
        }else{
        }

        val decimal = DecimalFormat("#,###")
        textAmount.text = "${decimal.format(Store.paymentInfo.infoPayment?.amount)} đ"
        buttonSubmit.background = Store.config.colorApp.backgroundColorRadius
        val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
        val infoTotal= event.infoBottom[event.infoBottom.size-1]
        event.infoBottom[event.infoBottom.size-2].isEnd = true
        event.infoBottom.removeAt(event.infoBottom.size-1)
        infoBottom.updateData(event.infoBottom)
        infoTop.updateData(event.infoTop)
        textAmount.text = infoTotal.value
        textAmount.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        buttonSubmit.setOnClickListener {
            var even: EventBus = EventBus.getDefault()
            var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
            even.post(myEven)
        }

        return view
    }
}