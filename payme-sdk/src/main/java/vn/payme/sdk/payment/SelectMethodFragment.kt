package vn.payme.sdk.payment

import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.component.InfoPayment
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.model.Info
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store
import java.text.DecimalFormat

class SelectMethodFragment : Fragment() {
    private lateinit var buttonClose: ImageView
    private lateinit var textAmount: TextView
    private lateinit var textNote: TextView
    private lateinit var textPersonReserving: TextView
    private lateinit var textIdService: TextView
    private lateinit var textTitleMethodSelected: TextView
    private lateinit var textNoteMethodSelected: TextView
    private lateinit var textFeeMethodSelected: TextView
    private lateinit var layout: ConstraintLayout
    private lateinit var buttonChangeMethod: CardView
    private lateinit var textChangeMethod: TextView
    private lateinit var imageMethod: ImageView
    private lateinit var infoFee: InfoPayment
    private lateinit var frameLayout: FrameLayout
    private var loading: Boolean = false
    private lateinit var buttonSubmit: Button



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater?.inflate(R.layout.select_method_layout, container, false)

        textAmount = view.findViewById(R.id.money)
        textNote = view.findViewById(R.id.note)
        layout = view.findViewById(R.id.content)

        textPersonReserving = view.findViewById(R.id.txtPersonReserving)
        textIdService = view.findViewById(R.id.txtIdService)

        textChangeMethod = view.findViewById(R.id.txtChangeMethod)
        textTitleMethodSelected = view.findViewById(R.id.txtTitle)
        textNoteMethodSelected = view.findViewById(R.id.txtNote)
        textFeeMethodSelected = view.findViewById(R.id.txtFee)
        buttonChangeMethod = view.findViewById(R.id.wrapButtonChangeMethod)
        imageMethod = view.findViewById(R.id.imageMethod)

        infoFee = view.findViewById(R.id.infoFee)
        frameLayout = view.findViewById(R.id.frame_container_select_method)

        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        EventBus.getDefault().register(this)



        layout.background = Store.config.colorApp.backgroundColorRadiusTop

        val decimal = DecimalFormat("#,###")

        textAmount.text = "${decimal.format(Store.paymentInfo.infoPayment?.amount)} đ"
        textIdService.text = Store.paymentInfo.infoPayment?.orderId
        textPersonReserving.text = Store.userInfo.dataInit?.optString("storeName")
        textNote.text = Store.paymentInfo.infoPayment?.note

        buttonChangeMethod.setOnClickListener {
            Store.paymentInfo.methodSelected = null
            buttonChangeMethod.visibility = View.GONE
            buttonSubmit.visibility = View.GONE
            if(frameLayout.visibility != View.VISIBLE){
                frameLayout.visibility = View.VISIBLE
            }
            buttonSubmit.visibility = View.GONE


            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, ListMethodPaymentFragment())
            fragment?.commit()
        }
        buttonSubmit.setOnClickListener {
//            if (!buttonSubmit.isLoadingShowing && cardDate.length > 0 && cardHolder.length > 0 && cardNumberValue.length > 0) {

//                checkFee()
//            }
        }
        if(Store.paymentInfo.methodSelected !=null){
            buttonChangeMethod.visibility = View.VISIBLE
            addImage(Store.paymentInfo.methodSelected!!,imageMethod)
            setTitle(Store.paymentInfo.methodSelected!!,textTitleMethodSelected,textNoteMethodSelected)
        }

        textChangeMethod.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        if (!Store.paymentInfo.isChangeMethod) {
            buttonChangeMethod.visibility = View.GONE
        }

        val fragmentManager: FragmentManager
        fragmentManager = childFragmentManager
        val fragment = fragmentManager.beginTransaction()
        fragment.add(R.id.frame_container_select_method, ListMethodPaymentFragment())
        fragment.commit()

        return view
    }
    fun setTitle(method: Method, titleText: TextView, noteMethod: TextView) {
        if (method.type == TYPE_PAYMENT.BANK_CARD) {
            titleText.text = method.title
        } else if (method.type == TYPE_PAYMENT.WALLET) {
            titleText.text = method.title
            val decimal = DecimalFormat("#,###")
            if(Store.userInfo.accountKycSuccess){
                noteMethod.text = "(${decimal.format(Store.userInfo.balance?.toLong())}đ)"
            }

            return
        } else if (method.type == TYPE_PAYMENT.NAPAS || method.type == TYPE_PAYMENT.LINKED) {
            titleText.text = method.title!!
            noteMethod.text = method.label!!
            return
        } else {
            titleText.text = method.title
        }
    }


    private fun addImage(method: Method, imageView: ImageView) {
        if (method.type == TYPE_PAYMENT.BANK_QR_CODE) {
            imageView.setImageResource(R.drawable.ic_qr_code)
        }else if (method.type == TYPE_PAYMENT.BANK_CARD) {
            imageView.setImageResource(R.drawable.ic_atm)
        } else if (method.type == TYPE_PAYMENT.WALLET) {
            imageView.setImageResource(R.drawable.ic_payme)
        } else if (method.type == TYPE_PAYMENT.NAPAS || method.type == TYPE_PAYMENT.LINKED) {
            val picasso = Picasso.get()
            picasso.setIndicatorsEnabled(false)
            picasso.load("https://firebasestorage.googleapis.com/v0/b/vn-mecorp-payme-wallet.appspot.com/o/image_bank%2Fimage_method%2Fmethod${method.data?.swiftCode}.png?alt=media&token=28cdb30e-fa9b-430c-8c0e-5369f500612e")
                .resize(50, 50)
                .centerInside()
                .into(imageView)

        } else {
            imageView.setImageResource(R.drawable.ic_payme)
        }

    }

    fun getFee() {
        var listInfoBottom = arrayListOf<Info>()
        val decimal = DecimalFormat("#,###")
        val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
        val fee = event.fee
        val valueFree = if (fee > 0) "${decimal.format(fee)} đ" else "Miễn phí"
        listInfoBottom.add(Info("Phí", valueFree, null, null, false))
        listInfoBottom.add(
            Info(
                "Tổng thanh toán", "${
                    decimal.format(
                        Store.paymentInfo.infoPayment?.amount?.plus(
                            fee
                        )
                    )
                } đ", null, null, true
            )
        )
        infoFee.updateData(listInfoBottom)
    }
    @Subscribe
    fun onChangeFragment(method: Method) {
        Store.paymentInfo.methodSelected = method
        buttonChangeMethod.visibility = View.VISIBLE
        addImage(method,imageMethod)
        setTitle(method,textTitleMethodSelected,textNoteMethodSelected)
        if(method.type != TYPE_PAYMENT.BANK_CARD){
            frameLayout.visibility = View.GONE
        }
        getFee()
        infoFee.visibility = View.VISIBLE
        buttonSubmit.visibility = View.VISIBLE
    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)

        super.onDestroy()
    }





}