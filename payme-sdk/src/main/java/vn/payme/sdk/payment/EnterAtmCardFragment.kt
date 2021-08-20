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
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.InputInfo
import vn.payme.sdk.model.BankInfo
import vn.payme.sdk.evenbus.CheckInputAtm
import vn.payme.sdk.evenbus.ListBankAtm
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
    fun checkValidate() {
        if (bankSelected != null && cardDate.length > 0 && cardHolder.length > 0 && (cardNumberValue.length == 16 || cardNumberValue.length == 19)) {
            val cardInfo = CardInfo(
                inputCardDate.input.text.toString(),
                inputCardNumber.input.text.toString(),
                bankSelected?.shortName!!,
                cardNumberValue,
                cardHolder,
                cardDate,
                ""
            )
            EventBus.getDefault().post(CheckInputAtm(true, cardInfo))
        } else {
            EventBus.getDefault().post(CheckInputAtm(false, CardInfo("", "", "", "", "", "", "")))
        }

    }


    fun detechCardHoder(cardNumber: String, count: Int) {

        val paymentApi = PaymentApi()
        inputCardNumber.txtTitleRight.text = getString(R.string.checking)
        paymentApi.detectCardHolder(bankSelected?.swiftCode!!,
            cardNumber,
            onSuccess = { jsonObject ->
                if (count != this.count) return@detectCardHolder
                if (!isVisible) return@detectCardHolder
                inputCardNumber.progressBar.visibility = View.GONE
                val Utility = jsonObject.optJSONObject("Utility")
                val GetBankName = Utility.optJSONObject("GetBankName")
                val accountName = GetBankName.optString("accountName")
                val message = GetBankName.optString("message")
                val succeeded = GetBankName.optBoolean("succeeded")
                inputCardNumber.txtTitleRight.text = ""

                if (succeeded) {
                    inputCardHolder.input.setText(accountName)
                    if (accountName.length >= 19) {
                        cardHolder = accountName
                    } else {
                        cardHolder = accountName
                    }
                }

            },
            onError = { jsonObject, code, message ->
                if (count != this.count) return@detectCardHolder
                if (!isVisible) return@detectCardHolder
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
        val view: View =
            inflater?.inflate(R.layout.payme_payment_enter_atm_card_fragment, container, false)
        inputCardNumber = view.findViewById(R.id.inputCardNumber)
        inputCardHolder = view.findViewById(R.id.inputCardHolder)
        inputCardDate = view.findViewById(R.id.inputCardDate)
        val listBanks = EventBus.getDefault()
            .getStickyEvent(ListBankAtm::class.java).listBankATM.filter { bankInfo -> bankInfo.depositable }
        inputCardNumber.input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                count = count + 1
                inputCardNumber.txtTitleRight.text = ""
                inputCardNumber.imageRight.visibility = View.GONE


                val stringReplace = s?.replace("[^0-9]".toRegex(), "")
                var cardNumber = stringReplace?.subSequence(
                    0,
                    if (stringReplace.length < 19) stringReplace.length else 19
                )
                bankSelected = null


                if (cardNumber?.length!! >= 6) {
                    val cardPrefix = cardNumber.substring(0, 6)
                    var bankVerify = false
                    var cardPrefixBank = ""
                    for (i in 0 until listBanks.size) {
                        cardPrefixBank = listBanks[i].cardPrefix
                        if (cardPrefix == cardPrefixBank) {
                            inputCardNumber.imageRight.visibility = View.VISIBLE
                            bankSelected = listBanks[i]
                            if (bankSelected?.requiredDate == "EXPIRED_DATE") {
                                inputCardDate.txtTitle.setText(R.string.release_date_expired)
                            } else {
                                inputCardDate.txtTitle.setText(R.string.release_issue_date)
                            }
                            val picasso = Picasso.get()
                            picasso.setIndicatorsEnabled(false)
                            picasso.load("https://firebasestorage.googleapis.com/v0/b/vn-mecorp-payme-wallet.appspot.com/o/image_bank%2Fimage_method%2Fmethod${bankSelected?.swiftCode}.png?alt=media&token=28cdb30e-fa9b-430c-8c0e-5369f500612e")
                                .resize(50, 50)
                                .centerInside()
                                .into(inputCardNumber.imageRight)
                            bankVerify = true


                        }
                    }
                    if (bankSelected?.cardNumberLength == 16) {
                        val filters = arrayOfNulls<InputFilter>(1)
                        filters[0] = InputFilter.LengthFilter(19)
                        inputCardNumber.input.filters = filters
                        cardNumber = stringReplace?.subSequence(
                            0,
                            if (stringReplace.length < 16) stringReplace.length else 16
                        )
                    } else {
                        val filters = arrayOfNulls<InputFilter>(1)
                        filters[0] = InputFilter.LengthFilter(21)
                        inputCardNumber.input.filters = filters
                    }


                    if (!bankVerify) {
                        inputCardNumber.setError(getString(R.string.number_card_error))
                    } else {
                        if (cardNumber?.length == 16 || cardNumber?.length == 19) {
                            detechCardHoder(cardNumber as String, count)
                        }
                        inputCardNumber.setDefault(null)
                    }
                    cardNumberValue = cardNumber.toString()
                    var cardNew = ""
                    if (cardNumber?.length!! > 16) {
                        for (i in 0 until cardNumber!!.length) {
                            if ((i == 7 || i == 15) && (i + 1 < cardNumber?.length)) {
                                cardNew += cardNumber[i] + " "
                            } else {
                                cardNew += cardNumber[i]
                            }
                        }
                    } else {
                        for (i in 0 until cardNumber!!.length) {
                            if ((i == 3 || i == 7 || i == 11) && (i + 1 < cardNumber.length)) {
                                cardNew += cardNumber[i] + " "
                            } else {
                                cardNew += cardNumber[i]
                            }
                        }
                    }
                    if (cardNew != s.toString()) {
                        inputCardNumber.input.removeTextChangedListener(this)
                        val cursorPosition: Int = inputCardNumber.input.getSelectionStart()
                        val newCursorPosition = cursorPosition + (cardNew.length - s!!.length)
                        inputCardNumber.input.setText(cardNew)
                        val check = newCursorPosition
                        inputCardNumber.input.setSelection(check!!)
                        inputCardNumber.input.addTextChangedListener(this)
                    }

                    checkValidate()

                } else {
                    inputCardNumber.setDefault(null)
                    inputCardDate.txtTitle.setText(R.string.release_issue_date)
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
            checkValidate()

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
                var title = ""
                if (bankSelected?.requiredDate == "ISSUE_DATE") {
                    title = getString(R.string.release_issue_date)
                } else {
                    title = getString(R.string.release_date_expired)
                }
                if (newDate != s.toString()) {
                    inputCardDate.input.removeTextChangedListener(this)
                    val cursorPosition: Int = inputCardDate.input.getSelectionStart()
                    val newCursorPosition = cursorPosition + (newDate.length - s.length)
                    inputCardDate.input.setText(newDate)
                    inputCardDate.input.setSelection(if (newCursorPosition > 5) 5 else newCursorPosition)
                    inputCardDate.input.addTextChangedListener(this)
                }

                if (newDate.length == 5) {
                    val month = Integer.parseInt(newDate.substring(0, 2))
                    val yead = newDate.substring(3, 5)
                    if (month < 1 || month > 12) {
                        cardDate = ""
                        inputCardDate.setError(
                            inputCardDate.txtTitle.text.toString() + " " + getString(
                                R.string.release_date_error
                            )
                        )
                    } else {
                        cardDate = "20${yead}-${newDate.substring(0, 2)}-12T12:08:32.860Z"
                        inputCardDate.setDefault(title)
                    }
                } else {
                    cardDate = ""
                    inputCardDate.setDefault(title)
                }
                checkValidate()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })

        return view
    }


    override fun onDestroy() {
        super.onDestroy()

    }

}