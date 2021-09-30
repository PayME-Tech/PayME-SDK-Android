package vn.payme.sdk.payment

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.enums.PAY_CODE
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.*
import vn.payme.sdk.hepper.AddInfoMethod
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.model.Info
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store
import java.text.DecimalFormat


class SelectMethodFragment : Fragment() {
    private lateinit var textAmount: TextView
    private lateinit var textAmountHiden: TextView
    private lateinit var textNote: TextView
    private lateinit var textMessageError: TextView
    private lateinit var textPersonReserving: TextView
    private lateinit var textIdService: TextView
    private lateinit var textTitleMethodSelected: TextView
    private lateinit var buttonChangeMethod: ConstraintLayout
    private lateinit var containerLoading: ConstraintLayout
    private lateinit var loading: ProgressBar
    private lateinit var layout: ConstraintLayout
    private lateinit var headerVisibility: ConstraintLayout
    private lateinit var headerHidden: ConstraintLayout
    private lateinit var imageMethod: ImageView
    private lateinit var imageLogoMC: ImageView
    private lateinit var containerLogoMC: CardView
    private lateinit var frameLayout: FrameLayout
    private lateinit var buttonSubmit: Button
    private lateinit var cardInfo: CardInfo
    var state= ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view: View =
            inflater?.inflate(R.layout.payme_payment_select_method_fragment, container, false)

        textAmount = view.findViewById(R.id.money)
        textAmountHiden = view.findViewById(R.id.money_hidden)
        headerVisibility = view.findViewById(R.id.containerIsVisibleHeader)
        headerHidden = view.findViewById(R.id.containerHiddenHeader)
        textNote = view.findViewById(R.id.note)
        textMessageError = view.findViewById(R.id.txtMessageError)
        layout = view.findViewById(R.id.content)
        containerLoading = view.findViewById(R.id.containerLoading)
        loading = view.findViewById(R.id.loading)


        textPersonReserving = view.findViewById(R.id.txtPersonReserving)
        textIdService = view.findViewById(R.id.txtIdService)

        textTitleMethodSelected = view.findViewById(R.id.txtTitle)
        buttonChangeMethod = view.findViewById(R.id.wrapButtonChangeMethod)
        imageMethod = view.findViewById(R.id.imageMethod)

        containerLogoMC = view.findViewById(R.id.wrapLogoMC)
        imageLogoMC = view.findViewById(R.id.imageLogoMC)

        frameLayout = view.findViewById(R.id.frame_container_select_method)

        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        buttonSubmit.iconLeft.visibility = View.VISIBLE
        EventBus.getDefault().register(this)
        layout.background = Store.config.colorApp.backgroundColorRadiusTop
        val decimal = DecimalFormat("#,###")
        val total = "${decimal.format(Store.paymentInfo.infoPayment?.amount)} đ"
        textAmount.text = total
        textAmountHiden.text = total


        val storeInfo = EventBus.getDefault().getStickyEvent(StoreInfo::class.java)
        if (storeInfo.isVisibleHeader) {
            headerVisibility.visibility = View.VISIBLE
            headerHidden.visibility = View.GONE
            textPersonReserving.text = storeInfo.storeName
            textNote.text = Store.paymentInfo.infoPayment?.note
            textIdService.text = Store.paymentInfo.infoPayment?.orderId
            if (storeInfo.storeImage != null && storeInfo.storeImage != "") {
                containerLogoMC.visibility = View.VISIBLE
                val picasso = Picasso.get()
                picasso.setIndicatorsEnabled(false)
                picasso.load(storeInfo.storeImage)
                    .resize(50, 50)
                    .centerInside()
                    .into(imageLogoMC)
            }
        }
        buttonSubmit.setOnClickListener {
            if (buttonSubmit.isActive) {
                if (buttonSubmit.isLoadingShowing) {
                    return@setOnClickListener
                }
                val method = Store.paymentInfo.methodSelected
                if (!(
                            method?.type == TYPE_PAYMENT.BANK_TRANSFER ||
                                    method?.type == TYPE_PAYMENT.WALLET ||
                                    method?.type == TYPE_PAYMENT.CREDIT_CARD ||
                                    method?.type == TYPE_PAYMENT.LINKED ||
                                    method?.type == TYPE_PAYMENT.BANK_QR_CODE ||
                                    method?.type == TYPE_PAYMENT.BANK_CARD
                            )
                ) {
                    PayME.showError(getString(R.string.method_not_supported))
                    return@setOnClickListener

                }
                if(method?.type == TYPE_PAYMENT.BANK_QR_CODE){
                    EventBus.getDefault()
                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT, null))
                    return@setOnClickListener
                }

                if (ListMethodPaymentFragment.isVisible) {
                    val payFunction = PayFunction()
                    if (method?.type == TYPE_PAYMENT.BANK_CARD) {
                        buttonSubmit.enableLoading()
                        payFunction.getListBank(onSuccess = {
                            buttonSubmit.disableLoading()
                            changeMethod(Store.paymentInfo.methodSelected!!)

                        }, onError = { jsonObject, i, s ->
                            buttonSubmit.disableLoading()
                            PayME.showError(s)
                        })
                        return@setOnClickListener

                    }
                    if (method?.type == TYPE_PAYMENT.BANK_TRANSFER) {
                        buttonSubmit.enableLoading()
                        payFunction.getListBank(onSuccess = {
                            payFunction.getListBankTransfer(onSuccess = {
                                buttonSubmit.disableLoading()
                                changeMethod(Store.paymentInfo.methodSelected!!)
                            }, Store.paymentInfo.methodSelected!!, onError = { jsonObject, i, s ->
                                buttonSubmit.disableLoading()
                                PayME.showError(s)
                            })
                        }, onError = { jsonObject, i, s ->
                            buttonSubmit.disableLoading()
                            PayME.showError(s)
                        })
                        return@setOnClickListener

                    }
                }

                if (method?.type == TYPE_PAYMENT.BANK_TRANSFER) {
                    val popupCheckBankTransfer = PopupCheckBankTransfer()
                    popupCheckBankTransfer.show(parentFragmentManager, null)
                } else if (method?.type == TYPE_PAYMENT.BANK_CARD || method?.type == TYPE_PAYMENT.CREDIT_CARD) {
                    EventBus.getDefault().postSticky(PaymentInfoEvent(cardInfo))
                    onSubmit(cardInfo)
                } else {
                    onSubmit(null)
                }
            }
        }

        if(Store.paymentInfo.payCode == PAY_CODE.VN_PAY){

            containerLoading.visibility = View.VISIBLE
            loading.getIndeterminateDrawable()
                .mutate()
                .setColorFilter(Color.parseColor(Store.config.colorApp.startColor), PorterDuff.Mode.SRC_ATOP)
            buttonSubmit.setButtonTypeBorder()
            buttonSubmit.textView.text = getString(R.string.cancel_the_transaction)
            buttonSubmit.iconLeft.visibility = View.GONE
            val qrContent = arguments?.getString("qrContent")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qrContent))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            requireActivity().startActivity(intent);


        } else if (Store.paymentInfo.payCode == PAY_CODE.PAYME) {
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, ListMethodPaymentFragment())
            fragment?.commit()
            if (Store.userInfo.accountKycSuccess && Store.userInfo.accountKycSuccess) {
                getFee()
            } else {
                buttonSubmit.setVisible(false)
            }
        } else {
            buttonChangeMethod.visibility = View.VISIBLE
            changeMethod(Store.paymentInfo.methodSelected!!)

        }

        return view
    }

    @Subscribe
    fun evenChangeMethod(event: Method) {
        getFee()
    }

    fun getFee() {
        var listInfoBottom = arrayListOf<Info>()
        val decimal = DecimalFormat("#,###")
        val event = EventBus.getDefault().getStickyEvent(FeeInfo::class.java)
        val fee = event.fee
        if (event.state == "OVER_DAY_QUOTA" || event.state == "OVER_MONTH_QUOTA") {
            textMessageError.text = event.message
            textMessageError.visibility = View.VISIBLE
            buttonSubmit.setVisible(false)
        } else {
            val total = Store.paymentInfo.infoPayment!!.amount

            if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.WALLET && total > Store.userInfo.balance) {
                buttonSubmit.setVisible(false)
            } else {
                textMessageError.visibility = View.GONE
                buttonSubmit.setVisible(true)
            }

        }


    }

    fun changeMethod(method: Method) {
        AddInfoMethod().addImage(method, imageMethod)
        AddInfoMethod().setTitle(
            method,
            textTitleMethodSelected,
            null, null
        )
        if (method.type == TYPE_PAYMENT.BANK_TRANSFER) {
            buttonSubmit.iconLeft.visibility = View.GONE
            buttonSubmit.setText(getString(R.string.confirm_transfer))
        } else {
            buttonSubmit.iconLeft.visibility = View.VISIBLE
            buttonSubmit.setText(getString(R.string.confirm))
        }
        if (method.type == TYPE_PAYMENT.CREDIT_CARD) {
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, EnterCreditCardFragment())
            fragment?.commit()
            buttonSubmit.setVisible(false)
        } else if (method.type == TYPE_PAYMENT.BANK_TRANSFER) {
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, InfoBankTransferFragment())
            fragment?.commit()
        } else if (method.type == TYPE_PAYMENT.BANK_CARD) {
            buttonSubmit.setVisible(false)
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, EnterAtmCardFragment())
            fragment?.commit()
        } else {
            frameLayout.visibility = View.GONE
        }
    }


    @Subscribe
    fun checkAtmResponse(event: CheckInputAtm) {
        buttonSubmit.setVisible(event.isCheck)
        cardInfo = event.cardInfo!!

    }

    fun authCreditCard(cardInfo: CardInfo?) {
        val paymentApi = PaymentApi()
        paymentApi.checkAuthCard(requireContext(),
            cardInfo?.cardDateView,
            cardInfo?.cardNumber,
            null,
            onSuccess = { ref ->
                if (!isVisible) return@checkAuthCard
                onPay(ref, cardInfo)
            }) { jsonObject, i, s ->
            if (!isVisible) return@checkAuthCard
            buttonSubmit.disableLoading()
            PayME.showError(s)
        }
    }

    fun onPay(referenceId: String?, cardInfo: CardInfo?) {
        val paymentApi = PaymentApi()
        paymentApi.payment(
            Store.paymentInfo.methodSelected!!,
            "",
            cardInfo,
            "",
            "",
            true,
            referenceId,
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
                if (succeeded) {
                    EventBus.getDefault()
                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, null))
                } else {
                    if (payment != null) {
                        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_CARD) {
                            val statePaymentBankCardResponsed =
                                payment.optString("statePaymentBankCardResponsed")
                            if (statePaymentBankCardResponsed == "REQUIRED_VERIFY") {
                                val html = payment.optString("html")
                                EventBus.getDefault()
                                    .post(
                                        ChangeFragmentPayment(
                                            TYPE_FRAGMENT_PAYMENT.CONFIRM_NAPAS,
                                            html
                                        )
                                    )
                            } else {
                                EventBus.getDefault()
                                    .post(
                                        ChangeFragmentPayment(
                                            TYPE_FRAGMENT_PAYMENT.RESULT,
                                            message
                                        )
                                    )
                            }
                        }
                        if (Store.paymentInfo?.methodSelected?.type == TYPE_PAYMENT.CREDIT_CARD) {
                            val statePaymentCreditCardResponsed =
                                payment.optString("statePaymentCreditCardResponsed")
                            if (statePaymentCreditCardResponsed == "SUCCEEDED") {
                                EventBus.getDefault()
                                    .post(
                                        ChangeFragmentPayment(
                                            TYPE_FRAGMENT_PAYMENT.RESULT,
                                            null
                                        )
                                    )
                            } else if (statePaymentCreditCardResponsed == "FAILED") {
                                EventBus.getDefault()
                                    .post(
                                        ChangeFragmentPayment(
                                            TYPE_FRAGMENT_PAYMENT.RESULT,
                                            message
                                        )
                                    )
                            } else if (statePaymentCreditCardResponsed == "REQUIRED_VERIFY") {
                                val html = payment.optString("html")
                                EventBus.getDefault()
                                    .post(
                                        ChangeFragmentPayment(
                                            TYPE_FRAGMENT_PAYMENT.CONFIRM_NAPAS,
                                            html
                                        )
                                    )
                            }
                        }

                    } else {
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

    }

    private fun onSubmit(cardInfo: CardInfo?) {
        buttonSubmit.enableLoading()
        Keyboard.closeKeyboard(requireContext())
        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.CREDIT_CARD) {
            authCreditCard(cardInfo)
            return
        }
        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_CARD ||
            Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.CREDIT_CARD ||
            Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_TRANSFER
        ) {
            onPay(null, cardInfo)
        } else {
            EventBus.getDefault()
                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CONFIRM_PASS, null))
        }

    }

    override fun onStop() {
        state= "onStop"
        super.onStop()
    }

    override fun onResume() {
        println("state"+state)
        if(state=="onStop"){
            loopCallApi()
        }
        state= "onResume"
        super.onResume()
    }
    fun loopCallApi() {
        val paymentApi = PaymentApi()
        paymentApi.checkVisa(onSuccess = { jsonObject ->
            val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
            val Payment = OpenEWallet.optJSONObject("Payment")
            val GetTransactionInfo = Payment.optJSONObject("GetTransactionInfo")
            val state = GetTransactionInfo.optString("state")
            val succeeded = GetTransactionInfo.optBoolean("succeeded")
            if (succeeded) {
                if (state == "SUCCEEDED") {
                    EventBus.getDefault()
                        .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.RESULT, null))
                } else if (state == "PENDING") {
                    if (state != "onStop") {
                        GlobalScope.launch {
                            delay(5000)
                            loopCallApi()
                        }
                    }


                } else {
                }

            } else {
            }


        }, onError = { jsonObject, code, s ->

        })

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }



}