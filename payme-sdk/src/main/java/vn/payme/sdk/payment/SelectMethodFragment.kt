package vn.payme.sdk.payment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.component.InfoPayment
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.CheckInputAtm
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.hepper.AddInfoMethod
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.model.Info
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store
import java.text.DecimalFormat

class SelectMethodFragment : Fragment() {
    private lateinit var textAmount: TextView
    private lateinit var textNote: TextView
    private lateinit var textPersonReserving: TextView
    private lateinit var textIdService: TextView
    private lateinit var textTitleMethodSelected: TextView
    private lateinit var textNoteMethodSelected: TextView
    private lateinit var textFeeMethodSelected: TextView
    private lateinit var buttonChangeMethod: CardView
    private lateinit var layout: ConstraintLayout
    private lateinit var textChangeMethod: TextView
    private lateinit var imageMethod: ImageView
    private lateinit var imageLogoMC: ImageView
    private lateinit var containerLogoMC: CardView
    private lateinit var infoFee: InfoPayment
    private lateinit var frameLayout: FrameLayout
    private var loading: Boolean = false
    private lateinit var buttonSubmit: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater?.inflate(R.layout.payment_select_method_fragment, container, false)

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

        containerLogoMC = view.findViewById(R.id.wrapLogoMC)
        imageLogoMC = view.findViewById(R.id.imageLogoMC)

        infoFee = view.findViewById(R.id.infoFee)
        frameLayout = view.findViewById(R.id.frame_container_select_method)

        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        EventBus.getDefault().register(this)

        val storeImage = Store.userInfo.dataInit?.optString("storeImage")
        if(storeImage!=null && storeImage!="null" && storeImage!=""){
            containerLogoMC.visibility = View.VISIBLE
            val picasso = Picasso.get()
            picasso.setIndicatorsEnabled(false)
            picasso.load(storeImage)
                .resize(50, 50)
                .centerInside()
                .into(imageLogoMC)
        }
        layout.background = Store.config.colorApp.backgroundColorRadiusTop
        val decimal = DecimalFormat("#,###")
        textAmount.text = "${decimal.format(Store.paymentInfo.infoPayment?.amount)} đ"
        textIdService.text = Store.paymentInfo.infoPayment?.orderId
        textPersonReserving.text = Store.userInfo.dataInit?.optString("storeName")
        textNote.text = Store.paymentInfo.infoPayment?.note

        buttonChangeMethod.setOnClickListener {
            if(Store.paymentInfo.isChangeMethod) {
                Store.paymentInfo.methodSelected = null
                buttonChangeMethod.visibility = View.GONE
                buttonSubmit.visibility = View.GONE
                buttonSubmit.visibility = View.GONE
                infoFee.visibility = View.GONE
                frameLayout.visibility = View.VISIBLE
                if(Store.paymentInfo.isChangeMethod){
                    val fragment = childFragmentManager?.beginTransaction()
                    fragment?.replace(R.id.frame_container_select_method, ListMethodPaymentFragment())
                    fragment?.commit()
                }

            }
        }
        buttonSubmit.setOnClickListener {
                if(buttonSubmit.isLoadingShowing){
                    return@setOnClickListener
                }
                if(Store.paymentInfo.methodSelected?.type==TYPE_PAYMENT.BANK_TRANSFER){
                    val popupCheckBankTransfer = PopupCheckBankTransfer()
                    popupCheckBankTransfer.show(parentFragmentManager,null)
                }else if(Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_CARD || Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.CREDIT_CARD ){
                    EventBus.getDefault().post(CheckInputAtm(true,null))
                }else{
                    onPay(null)
                }


        }
        buttonSubmit.iconLeft.visibility = View.VISIBLE
        if(Store.paymentInfo.methodSelected !=null){
            textChangeMethod.visibility = View.GONE
            changeMethod(Store.paymentInfo.methodSelected!!)
        }else{
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, ListMethodPaymentFragment())
            fragment?.commit()
        }
        textChangeMethod.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        return view
    }




    fun getFee() {
        var listInfoBottom = arrayListOf<Info>()
        val decimal = DecimalFormat("#,###")
        val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
        val fee = event.fee
        val valueFree = if (fee > 0) "${decimal.format(fee)} đ" else "Miễn phí"
        listInfoBottom.add(Info("Phí", valueFree, null, null, false))
        val infoTotal =  Info(
            "Tổng thanh toán", "${
                decimal.format(
                    Store.paymentInfo.infoPayment?.amount?.plus(
                        fee
                    )
                )
            } đ", null, ContextCompat.getColor(requireContext(),R.color.red), true
        )
        infoTotal.valueTextSize = 17f
        listInfoBottom.add(
            infoTotal
        )
        infoFee.updateData(listInfoBottom)
    }
    @Subscribe
    fun onChangeFragment(method: Method) {
        Store.paymentInfo.methodSelected = method
        changeMethod(method)
    }
    fun changeMethod(method: Method){
        buttonChangeMethod.visibility = View.VISIBLE
        AddInfoMethod().addImage(method,imageMethod)
        AddInfoMethod().setTitle(method,textTitleMethodSelected,textNoteMethodSelected,textFeeMethodSelected)
        if(method.type == TYPE_PAYMENT.BANK_TRANSFER){
            Store.paymentInfo.isChangeMethod = false
            textChangeMethod.visibility = View.GONE
            buttonSubmit.iconLeft.visibility = View.GONE
            buttonSubmit.setText(getString(R.string.confirm_transfer))
        }else{
            buttonSubmit.iconLeft.visibility = View.VISIBLE
            buttonSubmit.setText(getString(R.string.confirm))
        }
        if(method.type == TYPE_PAYMENT.CREDIT_CARD){
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, EnterCreditCardFragment())
            fragment?.commit()
        }else if(method.type == TYPE_PAYMENT.BANK_TRANSFER){
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, InfoBankTransferFragment())
            fragment?.commit()
        }else if(method.type == TYPE_PAYMENT.BANK_CARD){
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, EnterAtmCardFragment())
            fragment?.commit()
        }else{

            frameLayout.visibility = View.GONE

        }
        if(method.type == TYPE_PAYMENT.BANK_TRANSFER){
            infoFee.visibility = View.GONE
        }else{
            getFee()
            infoFee.visibility = View.VISIBLE
        }
        buttonSubmit.visibility = View.VISIBLE
    }

    @Subscribe
    fun  checkAtmResponse (event :CheckInputAtm) {
        if(!event.isCheck){
            val eventFee = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
            EventBus.getDefault().postSticky(PaymentInfoEvent(event.cardInfo,eventFee.fee))
            onPay(event.cardInfo)

        }
    }
    private fun onPay(cardInfo : CardInfo?) {
        val paymentApi = PaymentApi()
        buttonSubmit.enableLoading()
        Keyboard.closeKeyboard(requireContext())
        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_CARD ||
            Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.CREDIT_CARD ||
            Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_TRANSFER   ) {
            paymentApi.payment(
                Store.paymentInfo.methodSelected!!,
                "",
                cardInfo,
                "",
                "",
                true,
                onSuccess = { jsonObject ->
                    if (!isVisible) return@payment
                    buttonSubmit.disableLoading()
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
                    if(succeeded){
                        EventBus.getDefault()
                            .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, null))
                    }else{
                        if(payment!=null){
                            if(Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_CARD){
                                val statePaymentBankCardResponsed =
                                    payment.optString("statePaymentBankCardResponsed")
                                if (statePaymentBankCardResponsed == "REQUIRED_VERIFY") {
                                    val html = payment.optString("html")
                                    EventBus.getDefault()
                                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CONFIRM_NAPAS, html))
                                } else {
                                    EventBus.getDefault()
                                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                                }
                            }
                            if(Store.paymentInfo?.methodSelected?.type==TYPE_PAYMENT.CREDIT_CARD){
                                val statePaymentCreditCardResponsed =
                                    payment.optString("statePaymentCreditCardResponsed")
                                if(statePaymentCreditCardResponsed=="SUCCEEDED"){
                                    EventBus.getDefault()
                                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT,null ))
                                }else if(statePaymentCreditCardResponsed=="FAILED"){
                                    EventBus.getDefault()
                                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                                }else if(statePaymentCreditCardResponsed=="REQUIRED_VERIFY"){
                                    val html = payment.optString("html")
                                    EventBus.getDefault()
                                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CONFIRM_NAPAS, html))
                                }
                            }

                        }else{
                            EventBus.getDefault()
                                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, message))
                        }
                    }


                },
                onError = { jsonObject, code, message ->
                    if (!isVisible) return@payment
                    buttonSubmit.disableLoading()
                        PayME.showError(message)
                })

        } else {
            EventBus.getDefault()
                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CONFIRM_PASS, null))
        }

    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }





}