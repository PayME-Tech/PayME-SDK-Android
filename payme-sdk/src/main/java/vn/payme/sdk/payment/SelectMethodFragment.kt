package vn.payme.sdk.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TypeCallBack
import java.text.DecimalFormat

class SelectMethodFragment : Fragment() {
    private lateinit var buttonClose: ImageView
    private lateinit var textAmount: TextView
    private lateinit var textNote: TextView
    private lateinit var layout: ConstraintLayout
    private var loading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater?.inflate(R.layout.select_method_layout, container, false)
        buttonClose = view.findViewById(R.id.buttonClose)
        textAmount = view.findViewById(R.id.money)
        textNote = view.findViewById(R.id.note)
        layout = view.findViewById(R.id.content)
        layout.background = PayME.colorApp.backgroundColor
        textNote.text = PayME.infoPayment?.note
        val decimal = DecimalFormat("#,###")
        textAmount.text = "${decimal.format(PayME.infoPayment?.amount)} đ"
        val fragmentManager: FragmentManager
        fragmentManager = childFragmentManager
        val fragment = fragmentManager.beginTransaction()
        fragment.add(R.id.frame_container_select_method, ListMethodPaymentFragment())
        fragment.commit()

        buttonClose.setOnClickListener {
            if (!loading) {
                PayME.onError(null, ERROR_CODE.USER_CANCELLED,"")
                var even: EventBus = EventBus.getDefault()
                var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
                even.post(myEven)
            }
        }

        return view
    }





}