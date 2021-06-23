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
import androidx.constraintlayout.widget.ConstraintLayout
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
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.PaymentInfoEvent
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
    private lateinit var textHotline: TextView
    private lateinit var lottie: LottieAnimationView
    private lateinit var infoTop: InfoPayment
    private lateinit var infoBottom: InfoPayment
    private lateinit var transitionCodeContainer: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.payment_result_fragment, container, false)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        textAmount = view.findViewById(R.id.money)
        textError = view.findViewById(R.id.note_error)
        textTransactionCode = view.findViewById(R.id.transition_code_value)
        textTransactionTime = view.findViewById(R.id.transition_time_value)
        transitionCodeContainer = view.findViewById(R.id.transition_code_container)
        textHotline = view.findViewById(R.id.txtHotline)
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
        val state = arguments?.getString("state")

        val decimal = DecimalFormat("#,###")
        textAmount.text = "${decimal.format(Store.paymentInfo.infoPayment?.amount)} đ"
        val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
        var listInfoTop = arrayListOf<Info>()
        listInfoTop.add(Info("Người nhận", Store.paymentInfo.storeName, null, null, false))
        listInfoTop.add(Info("Mã dịch vụ", Store.paymentInfo.infoPayment?.orderId, null, null, false))
        listInfoTop.add(Info("Nội dung", Store.paymentInfo.infoPayment?.note, null, null, true))

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
        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.CREDIT_CARD) {
            val lengthCard = event.cardInfo?.cardNumber?.length
            val cardNumber = event.cardInfo?.cardNumber?.substring(
                lengthCard!! - 4,
                lengthCard!!
            )
            listInfoBottom.add(
                Info(
                    "Số thẻ",
                    event.cardInfo?.bankShortName + "-" + cardNumber,
                    null,
                    null,
                    false
                )
            )
        }
        textAmount.text = "${decimal.format(Store.paymentInfo.infoPayment?.amount)} đ"
        val feeString = if(event.fee==0) "Miễn phí" else "${decimal.format(event.fee)} đ"
        val totalString ="${decimal.format(event.fee + Store.paymentInfo.infoPayment!!.amount)} đ"
        listInfoBottom.add(Info("Phí",  feeString, null, null, false))
        listInfoBottom.add(Info("Tổng thanh toán",  totalString, null, ContextCompat.getColor(requireContext(),R.color.red), true))
        if(state =="PENDING"){
            infoTop.updateData(listInfoTop)
            lottie.setAnimation(R.raw.cho_xu_ly)
            textResult.text = getString(R.string.payment_pending)
            textError.text = getString(R.string.payment_pending_description)
            textError.visibility = View.VISIBLE
            textHotline.visibility = View.VISIBLE
            buttonSubmit.textView.text = getString(R.string.understood)
            infoBottom.visibility = View.GONE
            var backgroundColorRadius = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(ContextCompat.getColor(requireContext(),R.color.red60),ContextCompat.getColor(requireContext(),R.color.red60)))
            backgroundColorRadius.cornerRadius = 60F
            buttonSubmit.background = backgroundColorRadius
            lottie.playAnimation()
        }else if(message != null || state=="FAILED"){
            infoTop.updateData(listInfoTop)
            textError.text = message
            textError.visibility = View.VISIBLE
            lottie.setAnimation(R.raw.thatbai)
            textResult.text = getString(R.string.payment_fail)
            buttonSubmit.textView.text = getString(R.string.understood)
            infoBottom.visibility = View.GONE
           var backgroundColorRadius = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(ContextCompat.getColor(requireContext(),R.color.red),ContextCompat.getColor(requireContext(),R.color.red)))
            backgroundColorRadius.cornerRadius = 60F
            buttonSubmit.background = backgroundColorRadius
            lottie.playAnimation()
        }else{
            //Thành công
                if(Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_TRANSFER){
                    transitionCodeContainer.visibility = View.GONE
                    infoBottom.updateData(listInfoTop)
                    var listInfoTopBankTransfer = arrayListOf<Info>()
                    listInfoTopBankTransfer.add(Info("Mã giao dịch", Store.paymentInfo.transaction, null, null, false))
                    listInfoTopBankTransfer.add(Info("Thời gian giao dịch", DateStr, null, null, false))
                    listInfoTopBankTransfer.add(Info("Phương thức", Store.paymentInfo.methodSelected?.title, null, null, false))
                    listInfoTopBankTransfer.add(Info("Phí giao dịch",feeString, null, null, false))
                    listInfoTopBankTransfer.add(Info("Tổng thanh toán",totalString, null, ContextCompat.getColor(requireContext(),R.color.red), true))
                    infoTop.updateData(listInfoTopBankTransfer)
                }else{
                    infoTop.updateData(listInfoTop)
                    infoBottom.updateData(listInfoBottom)
                }

            loadAnimation()
            textAmount.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        }

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