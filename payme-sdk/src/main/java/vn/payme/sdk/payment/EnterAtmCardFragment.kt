package vn.payme.sdk.payment

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.InputInfo
import vn.payme.sdk.model.BankInfo
import vn.payme.sdk.evenbus.CheckInputAtm
import vn.payme.sdk.evenbus.ListBankAtm
import vn.payme.sdk.model.BankTransferInfo
import vn.payme.sdk.model.CardInfo
import java.util.*


class EnterAtmCardFragment : Fragment() {

    private lateinit var inputCardNumber: InputInfo
    private lateinit var inputCardHolder: InputInfo
    private lateinit var inputCardDate: InputInfo

    private var bankSelected: BankInfo? = null
    private var cardHolder: String = ""
    private var cardDate: String = ""
    private var cardNumberValue: String = ""
    var count = 0



    fun detechCardHoder(cardNumber: String,count:Int) {

        val paymentApi = PaymentApi()
        inputCardNumber.progressBar.visibility = View.VISIBLE
        paymentApi.detectCardHolder(bankSelected?.swiftCode!!,
            cardNumber,
            onSuccess = { jsonObject ->
                if(count!=this.count) return@detectCardHolder
                if(!isVisible) return@detectCardHolder
                inputCardNumber.progressBar.visibility = View.GONE
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
                if(count!=this.count) return@detectCardHolder
                if(!isVisible) return@detectCardHolder
                inputCardNumber.progressBar.visibility = View.GONE
                PayME.showError(message)
            }
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.payment_enter_atm_card_fragment, container, false)
        EventBus.getDefault().register(this)
        inputCardNumber = view.findViewById(R.id.inputCardNumber)
        inputCardHolder = view.findViewById(R.id.inputCardHolder)
        inputCardDate = view.findViewById(R.id.inputCardDate)
//
//
        inputCardNumber.input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                count = count+1
                inputCardNumber.progressBar.visibility = View.GONE

                val cardNumber = s?.replace("[^0-9]".toRegex(), "")
                cardHolder = ""
                cardNumberValue = ""
                inputCardHolder.visibility = View.GONE
                inputCardHolder.setDefault()
                inputCardNumber.txtTitleRight.text = ""
                inputCardHolder.input.setText("")
                var maxLength = 16
                val listBanks = EventBus.getDefault().getStickyEvent(ListBankAtm::class.java).listBankATM
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
                                detechCardHoder(cardNumber,count)
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




        return view
    }
    @Subscribe
    fun checkAtm (event :CheckInputAtm){

        if (event.isCheck && cardDate.length > 0 && cardHolder.length > 0 && cardNumberValue.length > 0) {
            val cardInfo = CardInfo(
                inputCardDate.input.text.toString(),
                inputCardNumber.input.text.toString(),
                bankSelected?.shortName!!,
                cardNumberValue,
                cardHolder,
                cardDate,
                ""
            )
            EventBus.getDefault().post(CheckInputAtm(false,  cardInfo))
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }

}