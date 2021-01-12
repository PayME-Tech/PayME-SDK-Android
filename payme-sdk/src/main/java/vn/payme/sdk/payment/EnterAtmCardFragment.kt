package vn.payme.sdk.payment

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.evenbus.ChangeTypePayment
import vn.payme.sdk.model.BankInfo
import java.util.*


class EnterAtmCardFragment : Fragment() {
    private lateinit var buttonSubmit: Button
    private lateinit var contentButtonChangeMethod: ConstraintLayout
    private lateinit var buttonChangeMethod: ConstraintLayout
    private var listBanks: ArrayList<BankInfo> = ArrayList<BankInfo>()
    private lateinit var textInputCardNumber: EditText
    private lateinit var textInputCardHolder: EditText
    private lateinit var textInputCardDate: EditText
    private lateinit var textErrorCard: TextView
    private lateinit var textNoteCard: TextView
    private lateinit var textErrorDate: TextView
    private lateinit var containerInputCardHolder: ConstraintLayout
    private var bankSelected: BankInfo? = null
    private var cardHolder: String = ""
    private var cardDate: String = ""
    private var cardNumberValue: String = ""
    fun checkVerifyCardNumber(text: String) {


    }

    fun detechCardHoder(cardNumber: String) {
        val paymentApi = PaymentApi()
        paymentApi.detechCardHolder(bankSelected?.swiftCode!!,
            cardNumber,
            onSuccess = { jsonObject ->
                val Utility = jsonObject.optJSONObject("Utility")
                val GetBankName = Utility.optJSONObject("GetBankName")
                val accountName = GetBankName.optString("accountName")
                val message = GetBankName.optString("message")
                val succeeded = GetBankName.optBoolean("succeeded")
                if (succeeded) {
                    textNoteCard.setText(bankSelected?.shortName + " " + accountName.toUpperCase())
                    cardHolder = accountName
                } else {
                    containerInputCardHolder.visibility = View.VISIBLE
//                    PayME.showError(message)
                }


            },
            onError = { jsonObject, i, message ->
                PayME.showError(message)

            }
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater?.inflate(R.layout.enter_atm_card_fragment, container, false)
        buttonSubmit = view!!.findViewById(R.id.buttonSubmit)
        contentButtonChangeMethod = view!!.findViewById(R.id.contentButtonChangeMethod)
        buttonChangeMethod = view!!.findViewById(R.id.buttonChangeMethod)

        textInputCardNumber = view!!.findViewById(R.id.textInputCardNumber)
        textInputCardHolder = view!!.findViewById(R.id.textInputCardHolder)
        textInputCardDate = view!!.findViewById(R.id.textInputCardDate)

        textErrorCard = view!!.findViewById(R.id.textErrorCard)
        textNoteCard = view!!.findViewById(R.id.textNoteCard)
        textErrorDate = view!!.findViewById(R.id.textErrorDate)

        containerInputCardHolder = view!!.findViewById(R.id.containerInputCardHolder)

        textInputCardNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val cardNumber = s?.replace("[^0-9]".toRegex(), "")
                cardHolder = ""
                cardNumberValue = ""
                textNoteCard.setText(R.string.enter_the_number_card_on_the_front_of_the_card)
                containerInputCardHolder.visibility = View.GONE

                if (cardNumber?.length!! >= 6) {
                    val cardPrefix = cardNumber.substring(0, 6)
                    var bankVerify = false
                    var cardNumberLength = 16
                    var cardPrefixBank = ""
                    for (i in 0 until listBanks.size) {
                        cardPrefixBank = listBanks[i].cardPrefix
                        if (cardPrefix == cardPrefixBank) {
                            bankSelected = listBanks[i]
                            cardNumberLength = listBanks[i].cardNumberLength
                            val maxLength = cardNumberLength + 3
                            val filters = arrayOfNulls<InputFilter>(1)
                            filters[0] = LengthFilter(maxLength)
                            textInputCardNumber.filters = filters
                            bankVerify = true
                            textNoteCard.setText(bankSelected?.shortName)
                            if (cardNumber.length == bankSelected?.cardNumberLength) {
                                detechCardHoder(cardNumber)
                                cardNumberValue = cardNumber

                            }

                        }
                    }

                    if (!bankVerify) {
                        textErrorCard.visibility = View.VISIBLE
                        textNoteCard.visibility = View.GONE
                    } else {
                        textErrorCard.visibility = View.GONE
                        textNoteCard.visibility = View.VISIBLE

                    }
                    var cardNew = ""


                    if (cardNumberLength == 19) {
                        var cardNew = ""
                        for (i in 0 until cardNumber.length) {
                            if ((i == 7 || i == 15) && (i + 1 < cardNumber.length)) {
                                cardNew += cardNumber[i] + " "
                            } else {
                                cardNew += cardNumber[i]
                            }
                        }

                    } else {
                        for (i in 0 until cardNumber.length) {
                            if ((i == 3 || i == 7 || i == 11) && (i + 1 < cardNumber.length)) {
                                cardNew += cardNumber[i] + " "
                            } else {
                                cardNew += cardNumber[i]
                            }
                        }


                    }

                    textInputCardNumber.removeTextChangedListener(this)
                    val cursorPosition: Int = textInputCardNumber.getSelectionStart()
                    val newCursorPosition = cursorPosition + (cardNew.length - s.length)
                    textInputCardNumber.setText(cardNew)
                    textInputCardNumber.setSelection(newCursorPosition)
                    textInputCardNumber.addTextChangedListener(this)

                } else {
                    textErrorCard.visibility = View.GONE
                    textNoteCard.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
//                textInputCardHolder.setText(s.toString().toUpperCase())
            }
        })
        textInputCardHolder.setFilters(arrayOf<InputFilter>(AllCaps()))


        textInputCardDate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val date = s?.replace("[^0-9]".toRegex(), "")
                var newDate = ""
                for (i in 0 until date?.length!!) {
                    if ((i == 1) && (i + 1 < date.length)) {
                        newDate += date[i] + "/"
                    } else {
                        newDate += date[i]
                    }
                }
                textInputCardDate.removeTextChangedListener(this)
                val cursorPosition: Int = textInputCardDate.getSelectionStart()
                val newCursorPosition = cursorPosition + (newDate.length - s.length)
                textInputCardDate.setText(newDate)
                textInputCardDate.setSelection(newCursorPosition)
                textInputCardDate.addTextChangedListener(this)
                if (newDate.length == 5) {
                    val month = Integer.parseInt(newDate.substring(0, 2))
                    val yead = newDate.substring(3, 5)
                    if (month < 1 || month > 12) {
                        cardDate = ""
                        textErrorDate.visibility = View.VISIBLE
                    } else {
                        cardDate = "20${yead}-${newDate.substring(0, 2)}-12T12:08:32.860Z"
                        textErrorDate.visibility = View.GONE
                    }
                } else {
                    cardDate = ""
                    textErrorDate.visibility = View.GONE
                }


            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })


        buttonChangeMethod.setOnClickListener {
            val fragment = fragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, ListMethodPaymentFragment())
            fragment?.commit()
        }
        contentButtonChangeMethod.background = PayME.colorApp.backgroundColorRadiusAlpha
        buttonSubmit.setOnClickListener {
            if (!buttonSubmit.isLoadingShowing && cardDate.length > 0 && cardHolder.length > 0 && cardNumberValue.length > 0) {
                buttonSubmit.enableLoading()
                val paymentApi = PaymentApi()
                var even: EventBus = EventBus.getDefault()

                var myEven: ChangeTypePayment = ChangeTypePayment(TYPE_PAYMENT.PAYMENT_RESULT, "")
                val method  = PayME.methodSelected
                paymentApi.payment(method, null, cardNumberValue, cardHolder, cardDate, null, null,
                    onSuccess = { jsonObject ->
                        buttonSubmit.disableLoading()
                        val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                        val Payment = OpenEWallet.optJSONObject("Payment")
                        val Pay = Payment.optJSONObject("Pay")
                        val succeeded = Pay.optBoolean("succeeded")
                        val payment = Pay.optJSONObject("payment")
                        val message = Pay.optString("message")
                        PayME.numberAtmCard = bankSelected?.shortName+"-"+cardNumberValue.substring(cardNumberValue.length-4)
                        if (succeeded) {
                            println("THANH CONG")

                            even.post(myEven)
                        } else {
                            println("payment" + payment)
                            if (payment != null) {
                                val statePaymentBankCardResponsed =
                                    payment.optString("statePaymentBankCardResponsed")
                                if (statePaymentBankCardResponsed == "REQUIRED_VERIFY") {
                                    val html = payment.optString("html")
                                    var changeFragmentOtp: ChangeTypePayment =
                                        ChangeTypePayment(TYPE_PAYMENT.CONFIRM_OTP_BANK_NAPAS, html)
                                    even.post(changeFragmentOtp)
                                } else if (statePaymentBankCardResponsed == "REQUIRED_OTP") {
                                    println("REQUIRED_OTP" + "1111111")
                                    val transaction = payment.optString("transaction")
                                    var changeFragmentOtp: ChangeTypePayment =
                                        ChangeTypePayment(
                                            TYPE_PAYMENT.CONFIRM_OTP_BANK,
                                            transaction
                                        )
                                    even.post(changeFragmentOtp)
                                } else {
                                    myEven.value = message
                                    even.post(myEven)
                                }
                            } else {
                                println("THAT BAI ")
                                myEven.value = message
                                even.post(myEven)
                            }

                        }
                        buttonSubmit.disableLoading()

                    },
                    onError = { jsonObject, i, s ->
                        buttonSubmit.disableLoading()
                        PayME.showError(s)

                    }

                )
            }
        }
        val paymentApi = PaymentApi()
        paymentApi.getListBanks(onSuccess = { jsonObject ->
            val Setting = jsonObject.optJSONObject("Setting")
            val banks = Setting.optJSONArray("banks")
            for (i in 0 until banks.length()) {
                val bank = banks.optJSONObject(i)
                val cardPrefix = bank.optString("cardPrefix")
                val depositable = bank.optBoolean("depositable")
                val cardNumberLength = bank.optInt("cardNumberLength")
                val shortName = bank.optString("shortName")
                val swiftCode = bank.optString("swiftCode")
                if (depositable) {
                    val bankInfo = BankInfo(
                        depositable,
                        cardPrefix,
                        cardNumberLength,
                        shortName,
                        swiftCode
                    )
                    this.listBanks.add(bankInfo)
                }

            }

        },
            onError = { jsonObject, code, message ->
                PayME.showError(message)

            }
        )
        return view
    }

}