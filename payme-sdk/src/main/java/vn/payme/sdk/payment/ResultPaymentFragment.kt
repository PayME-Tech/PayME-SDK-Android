package vn.payme.sdk.payment

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.SimpleLottieValueCallback
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.component.InfoPayment
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.*
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
    private lateinit var textHotline: TextView
    private lateinit var lottie: LottieAnimationView
    private lateinit var infoTop: InfoPayment
    private lateinit var infoBottom: InfoPayment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.payme_payment_result_fragment, container, false)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        textAmount = view.findViewById(R.id.money)
        textError = view.findViewById(R.id.note_error)
        textHotline = view.findViewById(R.id.txtHotline)
        infoTop = view.findViewById(R.id.infoTop)
        infoBottom = view.findViewById(R.id.infoBottom)

        val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy")
        val GetDate = Date()
        var DateStr: String? = dateFormat.format(GetDate)
        textResult = view.findViewById(R.id.title_result)
        lottie = view.findViewById(R.id.animation_view)
        val message = arguments?.getString("message")
        val state = arguments?.getString("state")

        val decimal = DecimalFormat("#,###")
        textAmount.text = "${decimal.format(Store.paymentInfo.infoPayment?.amount!!)} đ"
        val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
        var listInfoTop = arrayListOf<Info>()
        listInfoTop.add(Info(getString(R.string.transaction_code), Store.paymentInfo.transaction, null, null, false))
        listInfoTop.add(Info(getString(R.string.transaction_time), DateStr, null, null, false))


        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.LINKED) {
            listInfoTop.add(Info(getString(R.string.method), getString(R.string.affiliate_account), null, null, false))
            listInfoTop.add(
                Info(
                    getString(R.string.account_number),
                    Store.paymentInfo.methodSelected?.title + "-" + Store.paymentInfo.methodSelected?.label?.replace("[^0-9]".toRegex(), ""),
                    null,
                    null,
                    true
                )
            )
        } else if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.CREDIT_BALANCE) {
            listInfoTop.add(Info(getString(R.string.method), getString(R.string.credit_wallet), null, null, false))
        } else {
            listInfoTop.add(
                Info(
                    getString(R.string.method),
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
            listInfoTop.add(
                Info(
                    getString(R.string.card_number),
                    event.cardInfo?.bankShortName + "-" + cardNumber,
                    null,
                    null,
                    false
                )
            )
        }
        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.CREDIT_CARD) {
            val lengthCard = event.cardInfo?.cardNumber?.length
            val cardNumber = event.cardInfo?.cardNumber?.substring(
                lengthCard!! - 4,
                lengthCard!!
            )
            listInfoTop.add(
                Info(
                    getString(R.string.card_number),
                    event.cardInfo?.bankShortName + "-" + cardNumber,
                    null,
                    null,
                    false
                )
            )
        }
        val listInfoBottom: ArrayList<Info> = arrayListOf()
        val storeInfo = EventBus.getDefault().getStickyEvent(StoreInfo::class.java)
        if(storeInfo.storeName=="null"){
            listInfoBottom.add(Info(getString(R.string.receiver),storeInfo.merchantName , null, null, false))
        }else{
            listInfoBottom.add(Info(getString(R.string.receiver),storeInfo.storeName , null, null, false))
        }
        listInfoBottom.add(Info(getString(R.string.content), Store.paymentInfo.infoPayment?.note, null, null, true))
        if(state =="PENDING"){
            lottie.setAnimation(R.raw.cho_xu_ly)
            textResult.text = getString(R.string.payment_pending)
            textError.text = getString(R.string.payment_pending_description)
            textError.visibility = View.VISIBLE
            textHotline.visibility = View.VISIBLE
            buttonSubmit.textView.text = getString(R.string.understood)
            var backgroundColorRadius = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(ContextCompat.getColor(requireContext(),R.color.red60),ContextCompat.getColor(requireContext(),R.color.red60)))
            backgroundColorRadius.cornerRadius = 60F
            buttonSubmit.background = backgroundColorRadius
            lottie.playAnimation()
        }else if(message != null || state=="FAILED"){
            textError.text = message
            textError.visibility = View.VISIBLE
            lottie.setAnimation(R.raw.thatbai)
            textResult.text = getString(R.string.payment_fail)
            buttonSubmit.textView.text = getString(R.string.understood)
           var backgroundColorRadius = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(ContextCompat.getColor(requireContext(),R.color.red),ContextCompat.getColor(requireContext(),R.color.red)))
            backgroundColorRadius.cornerRadius = 60F
            buttonSubmit.background = backgroundColorRadius
            lottie.playAnimation()
        }else{
            textAmount.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
            loadAnimation()

        }
        listInfoTop[listInfoTop.size-1].isEnd  = true
        infoTop.updateData(listInfoTop)
        infoBottom.updateData(listInfoBottom)

        buttonSubmit.setOnClickListener {
            EventBus.getDefault()
                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT_NOT_CALL_BACK, ""))
        }

        return view
    }
    fun loadAnimation (){
        lottie.addValueCallback<ColorFilter>(
            KeyPath("Laplanh", "**"),
            LottieProperty.COLOR_FILTER,
            SimpleLottieValueCallback<ColorFilter?> {
                PorterDuffColorFilter(
                    Color.parseColor(Store.config.colorApp.startColor),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        )
        lottie.addValueCallback<ColorFilter>(
            KeyPath("Do", "**"),
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