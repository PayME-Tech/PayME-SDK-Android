package vn.payme.sdk.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.TypeCallBack
import java.text.DecimalFormat

class ResultPaymentFragment : Fragment() {
    private lateinit var buttonSubmit: Button
    private lateinit var buttonClose: ImageView
    private lateinit var textAmount: TextView
    private lateinit var textNote: TextView
    private lateinit var textResult: TextView
    private lateinit var textError: TextView
    private lateinit var lottie: LottieAnimationView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View? = inflater?.inflate(R.layout.result_payment_layout, container, false)

        buttonSubmit = view!!.findViewById(R.id.buttonSubmit)
        buttonClose = view!!.findViewById(R.id.buttonClose)
        textAmount = view.findViewById(R.id.money)
        textNote = view.findViewById(R.id.note)
        textError = view.findViewById(R.id.note_error)
        textResult = view.findViewById(R.id.title_result)
        lottie = view.findViewById(R.id.animation_view)
        textNote.text = PayME.infoPayment?.note
        val message = arguments?.getString("message")

        if(message!=null){
            textError.text = message
            lottie.setAnimation(R.raw.result_that_bai)
            textResult.text = getString(R.string.payment_fail)

        }
        val decimal = DecimalFormat("#,###")
        textAmount.text = "${decimal.format(PayME.infoPayment?.amount)} đ"
        buttonSubmit.background = PayME.colorApp.backgroundColorRadius
        buttonSubmit.setOnClickListener {
            var even: EventBus = EventBus.getDefault()
            var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
            even.post(myEven)
        }
        buttonClose.setOnClickListener {
            var even: EventBus = EventBus.getDefault()
            var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
            even.post(myEven)

        }


        return view
    }
}