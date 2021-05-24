package vn.payme.sdk.payment

import android.graphics.Color
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
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.model.BankInfo
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.model.Info
import vn.payme.sdk.store.Store
import java.text.DecimalFormat
import java.util.*


class EnterAtmCardFragment : Fragment() {
    private lateinit var buttonSubmit: Button
    private lateinit var buttonChangeMethod: ConstraintLayout
    private var listBanks: ArrayList<BankInfo> = ArrayList<BankInfo>()
    private lateinit var textInputCardNumber: EditText
    private lateinit var textInputCardHolder: EditText
    private lateinit var textInputCardDate: EditText
    private lateinit var textErrorCard: TextView
    private lateinit var textErrorDate: TextView
    private lateinit var textChangeMethod: TextView
    private lateinit var textNoteInputCardDate: TextView
    private lateinit var textDetectCardHolder: TextView
    private lateinit var textTitle: TextView
    private lateinit var containerInputCardHolder: CardView
    private lateinit var containerInputCardDate: CardView
    private lateinit var containerInputCardNumber: CardView
    private var bankSelected: BankInfo? = null
    private var cardHolder: String = ""
    private var cardDate: String = ""
    private var cardNumberValue: String = ""


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
                    if (accountName.length >= 19) {
                        containerInputCardHolder.visibility = View.VISIBLE
                        textInputCardHolder.setText(accountName)
                        cardHolder = accountName
                    } else {
                        textDetectCardHolder.text = accountName.toUpperCase()
                        cardHolder = accountName
                    }

                } else {
                    containerInputCardHolder.visibility = View.VISIBLE
                    textNoteInputCardDate.visibility = View.VISIBLE
//                    PayME.showError(message)
                }


            },
            onError = { jsonObject, code, message ->
                if (code == ERROR_CODE.EXPIRED) {
                    PayME.onExpired()
                    PayME.onError(jsonObject, code, message)
                } else {
                    PayME.showError(message)
                }

            }
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.enter_atm_card_fragment, container, false)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        buttonChangeMethod = view.findViewById(R.id.contentButtonChangeMethod)

        textInputCardNumber = view.findViewById(R.id.inputCardNumber)
        textInputCardHolder = view.findViewById(R.id.inputCardHolder)
        textInputCardDate = view.findViewById(R.id.inputCardDate)

        textErrorCard = view.findViewById(R.id.txtErrorCard)
        textErrorDate = view.findViewById(R.id.txtErrorDate)
        textTitle = view.findViewById(R.id.title_select_method)
        textChangeMethod = view.findViewById(R.id.txtChangeMethod)

        containerInputCardHolder = view.findViewById(R.id.containerInputCardHolder)
        containerInputCardDate = view.findViewById(R.id.containerInputCardDate)
        containerInputCardNumber = view.findViewById(R.id.containerInputCardNumber)

        textNoteInputCardDate = view.findViewById(R.id.txtNoteInputCardDate)
        textDetectCardHolder = view.findViewById(R.id.txtCardHolderDetect)

        containerInputCardHolder.setOnClickListener {
            textInputCardHolder.requestFocus()
           Keyboard.showKeyboard(requireContext())
        }
        containerInputCardNumber.setOnClickListener {
            textInputCardNumber.requestFocus()
           Keyboard.showKeyboard(requireContext())
        }
        containerInputCardDate.setOnClickListener {
            textInputCardDate.requestFocus()
            Keyboard.showKeyboard(requireContext())
        }

        textInputCardNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val cardNumber = s?.replace("[^0-9]".toRegex(), "")
                cardHolder = ""
                cardNumberValue = ""
                textDetectCardHolder.text = ""
                if (containerInputCardHolder.visibility != View.GONE) {
                    textInputCardHolder.setText("")
                    containerInputCardHolder.visibility = View.GONE
                    textNoteInputCardDate.visibility = View.GONE
                }
                var maxLength = 16


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
                            maxLength = cardNumberLength + 3
                            val filters = arrayOfNulls<InputFilter>(1)
                            filters[0] = LengthFilter(maxLength)
                            textInputCardNumber.filters = filters
                            bankVerify = true
                            if (cardNumber.length == bankSelected?.cardNumberLength) {
                                detechCardHoder(cardNumber)
                                cardNumberValue = cardNumber
                            }

                        }
                    }

                    if (!bankVerify) {
                        textErrorCard.visibility = View.VISIBLE
                    } else {
                        textErrorCard.visibility = View.GONE
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
                                cardNew += cardNumber[i] + "-"
                            } else {
                                cardNew += cardNumber[i]
                            }
                        }


                    }

                    textInputCardNumber.removeTextChangedListener(this)
                    val cursorPosition: Int = textInputCardNumber.getSelectionStart()
                    val newCursorPosition = cursorPosition + (cardNew.length - s.length)
                    textInputCardNumber.setText(cardNew)
                    val check = if(newCursorPosition>maxLength) maxLength else newCursorPosition
                    textInputCardNumber.setSelection(check)
                    textInputCardNumber.addTextChangedListener(this)

                } else {
                    textErrorCard.visibility = View.GONE
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
            }
        })
        textInputCardHolder.setFilters(arrayOf<InputFilter>(AllCaps()))
        textInputCardHolder.addTextChangedListener { text ->
            cardHolder = text.toString()
        }


        textInputCardDate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val number = s?.replace("[^0-9]".toRegex(), "")
                val date = number?.substring(0,if(number.length<4)number.length else 4)
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
                textInputCardDate.setSelection(if(newCursorPosition>5) 5 else newCursorPosition)
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
            Store.paymentInfo.methodSelected = null
            val fragment = fragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, ListMethodPaymentFragment())
            fragment?.commit()
        }

        buttonChangeMethod.background = Store.config.colorApp.backgroundColorRadiusBorder
        textChangeMethod.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        if(!Store.paymentInfo.isChangeMethod){
            buttonChangeMethod.visibility = View.GONE
        }
        buttonSubmit.setOnClickListener {
            if (!buttonSubmit.isLoadingShowing && cardDate.length > 0 && cardHolder.length > 0 && cardNumberValue.length > 0) {
                val cardInfo = CardInfo(
                    textInputCardDate.text.toString(),
                    textInputCardNumber.text.toString(),
                    bankSelected?.shortName!!,
                    cardNumberValue,
                    cardHolder,
                    cardDate
                )
                EventBus.getDefault().postSticky(PaymentInfoEvent(null,null,cardInfo,0))
                EventBus.getDefault().post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CONFIRM_PAYMENT,null))
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
                if (code == ERROR_CODE.EXPIRED) {
                    PayME.onExpired()
                    PayME.onError(jsonObject, code, message)
                } else {
                    PayME.showError(message)
                }
            }
        )
        return view
    }

}