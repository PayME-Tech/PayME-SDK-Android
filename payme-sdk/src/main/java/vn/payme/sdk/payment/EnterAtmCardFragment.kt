package vn.payme.sdk.payment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.component.InputTest
import vn.payme.sdk.model.BankInfo
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.store.Store
import java.util.*


class EnterAtmCardFragment : Fragment() {
    private lateinit var buttonSubmit: Button
    private lateinit var buttonChangeMethod: ConstraintLayout
    private var listBanks: ArrayList<BankInfo> = ArrayList<BankInfo>()
    private lateinit var inputCardNumber: InputTest
    private lateinit var inputCardHolder: InputTest
    private lateinit var inputCardDate: InputTest
    private lateinit var textChangeMethod: TextView


    private var bankSelected: BankInfo? = null
    private var cardHolder: String = ""
    private var cardDate: String = ""
    private var cardNumberValue: String = ""
    private fun checkFee() {
        Keyboard.closeKeyboard(requireContext())
        val paymentApi = PaymentApi()
        val method = Store.paymentInfo.methodSelected
        val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
        buttonSubmit.enableLoading()
        val cardInfo = CardInfo(
            inputCardDate.input.text.toString(),
            inputCardNumber.input.text.toString(),
            bankSelected?.shortName!!,
            cardNumberValue,
            cardHolder,
            cardDate
        )
        paymentApi.getFee(
            Store.paymentInfo.infoPayment!!.amount,
            Store.paymentInfo.methodSelected!!,
            cardInfo,
            onSuccess = { jsonObject ->
                buttonSubmit.disableLoading()
                val Utility = jsonObject.getJSONObject("Utility")
                val GetFee = Utility.getJSONObject("GetFee")
                val succeeded = GetFee.getBoolean("succeeded")
                val message = GetFee.getString("message")
                if (succeeded) {
                    val feeObject = GetFee.getJSONObject("fee")
                    val fee = feeObject.getInt("fee")
                    val state = GetFee.optString("state")
                    if (state == "null") {
                        EventBus.getDefault().postSticky(PaymentInfoEvent(null, null, cardInfo, fee))
                        EventBus.getDefault().post(
                            ChangeFragmentPayment(
                                TYPE_FRAGMENT_PAYMENT.CONFIRM_PAYMENT,
                                null
                            )
                        )

                    } else {
                        PayME.showError(message)
                    }


                } else {
                    PayME.showError(message)
                }
            },
            onError = { jsonObject: JSONObject?, code: Int?, message: String ->
                buttonSubmit.disableLoading()
                if (code == ERROR_CODE.EXPIRED) {
                    PayME.onExpired()
                } else {
                    PayME.showError(message)
                }
            })
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
                    if (accountName.length >= 19) {
                        inputCardHolder.visibility = View.VISIBLE
                        inputCardHolder.txtTitleRight.text = accountName
                        cardHolder = accountName
                    } else {
                        inputCardNumber.txtTitleRight.text = accountName
                        cardHolder = accountName
                    }

                } else {
                    inputCardHolder.visibility = View.VISIBLE
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
        textChangeMethod = view.findViewById(R.id.txtChangeMethod)
        inputCardNumber = view.findViewById(R.id.inputCardNumber)
        inputCardHolder = view.findViewById(R.id.inputCardHolder)
        inputCardDate = view.findViewById(R.id.inputCardDate)
//
//
        inputCardNumber.input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val cardNumber = s?.replace("[^0-9]".toRegex(), "")
                cardHolder = ""
                cardNumberValue = ""
                inputCardHolder.visibility = View.GONE
                inputCardHolder.setDefault()
                inputCardNumber.txtTitleRight.text = ""
                inputCardHolder.input.setText("")


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
                            filters[0] = InputFilter.LengthFilter(maxLength)
                            inputCardNumber.input.filters = filters
                            bankVerify = true
                            if (cardNumber.length == bankSelected?.cardNumberLength) {
                                detechCardHoder(cardNumber)
                                cardNumberValue = cardNumber
                            }

                        }
                    }

                    if (!bankVerify) {
                        inputCardNumber.setError(getString(R.string.number_card_error))
                    } else {
                        inputCardNumber.setDefault()
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
                    inputCardNumber.input
                    inputCardNumber.input.removeTextChangedListener(this)
                    val cursorPosition: Int = inputCardNumber.input.getSelectionStart()
                    val newCursorPosition = cursorPosition + (cardNew.length - s.length)
                    inputCardNumber.input.setText(cardNew)
                    val check = if (newCursorPosition > maxLength) maxLength else newCursorPosition
                    inputCardNumber.input.setSelection(check)
                    inputCardNumber.input.addTextChangedListener(this)

                } else {
                    inputCardNumber.setDefault()
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
        inputCardHolder.input.addTextChangedListener { text ->
            cardHolder = text.toString()
        }



        inputCardDate.input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val number = s?.replace("[^0-9]".toRegex(), "")
                val date = number?.substring(0, if (number.length < 4) number.length else 4)
                var newDate = ""
                for (i in 0 until date?.length!!) {
                    if ((i == 1) && (i + 1 < date.length)) {
                        newDate += date[i] + "/"
                    } else {
                        newDate += date[i]
                    }
                }
                inputCardDate.input.removeTextChangedListener(this)
                val cursorPosition: Int = inputCardDate.input.getSelectionStart()
                val newCursorPosition = cursorPosition + (newDate.length - s.length)
                inputCardDate.input.setText(newDate)
                inputCardDate.input.setSelection(if (newCursorPosition > 5) 5 else newCursorPosition)
                inputCardDate.input.addTextChangedListener(this)
                if (newDate.length == 5) {
                    val month = Integer.parseInt(newDate.substring(0, 2))
                    val yead = newDate.substring(3, 5)
                    if (month < 1 || month > 12) {
                        cardDate = ""
                        inputCardDate.setError(getString(R.string.release_date_error))
                    } else {
                        cardDate = "20${yead}-${newDate.substring(0, 2)}-12T12:08:32.860Z"
                        inputCardDate.setDefault()
                    }
                } else {
                    cardDate = ""
                    inputCardDate.setDefault()
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
        if (!Store.paymentInfo.isChangeMethod) {
            buttonChangeMethod.visibility = View.GONE
        }
        buttonSubmit.setOnClickListener {
            if (!buttonSubmit.isLoadingShowing && cardDate.length > 0 && cardHolder.length > 0 && cardNumberValue.length > 0) {

                checkFee()
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