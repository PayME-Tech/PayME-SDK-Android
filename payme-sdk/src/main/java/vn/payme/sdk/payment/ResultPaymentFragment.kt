package vn.payme.sdk.payment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TypeCallBack
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ResultPaymentFragment : Fragment() {
    private lateinit var buttonSubmit: Button
    private lateinit var buttonClose: ImageView
    private lateinit var textAmount: TextView
    private lateinit var textNote: TextView
    private lateinit var textResult: TextView
    private lateinit var textError: TextView
    private lateinit var textTransactionCode: TextView
    private lateinit var textTransactionTime: TextView
    private lateinit var textMethodTitle: TextView
    private lateinit var textMethodValue: TextView
    private lateinit var containerMethod: ConstraintLayout
    private lateinit var textNumberCard: TextView
    private lateinit var lottie: LottieAnimationView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater?.inflate(R.layout.result_payment_layout, container, false)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        buttonClose = view.findViewById(R.id.buttonClose)
        textAmount = view.findViewById(R.id.money)
        textNote = view.findViewById(R.id.note)
        textError = view.findViewById(R.id.note_error)

        textTransactionCode = view.findViewById(R.id.transition_code_value)
        textTransactionTime = view.findViewById(R.id.transition_time_value)
        textMethodValue = view.findViewById(R.id.method_value)
        textNumberCard = view.findViewById(R.id.number_card_value)
        containerMethod = view.findViewById(R.id.content_method)

        val dateFormat = SimpleDateFormat("HH:mm DD/MM/yyyy")
        val GetDate = Date()
        var DateStr: String? = dateFormat.format(GetDate)
        textTransactionTime.text = DateStr
        textMethodValue.text = PayME.methodSelected?.title
        if(PayME.methodSelected?.type==TYPE_PAYMENT.BANK_CARD){
            textNumberCard.text = PayME.numberAtmCard

        }else{
            if(PayME.methodSelected?.type==TYPE_PAYMENT.LINKED){
                textMethodValue.text = "Tài khoản liên kết"
                textNumberCard.text =PayME.methodSelected?.title+PayME.methodSelected?.label
            }else{
                textNumberCard.text = PayME.methodSelected?.label
            }

        }
        textTransactionCode.setText(PayME.transaction)
        textResult = view.findViewById(R.id.title_result)
        lottie = view.findViewById(R.id.animation_view)
        textNote.text = PayME.infoPayment?.note
        val message = arguments?.getString("message")

        if(message!=null){
            textError.text = message
            lottie.setAnimation(R.raw.result_that_bai)
            textResult.text = getString(R.string.payment_fail)
        }else{
        }
        if(PayME.methodSelected?.type == TYPE_PAYMENT.WALLET){
            containerMethod.visibility = View.GONE
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