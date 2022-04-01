package vn.payme.sdk.payment

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.cardmodules.ScanActivity
import vn.payme.sdk.cardmodules.ScanActivityImpl
import vn.payme.sdk.component.InputInfo
import vn.payme.sdk.evenbus.CheckInputAtm
import vn.payme.sdk.hepper.CardType
import vn.payme.sdk.kyc.PermissionCamera
import vn.payme.sdk.model.CardInfo

class EnterCreditCardFragment : Fragment() {
    private lateinit var inputCardNumber: InputInfo
    private lateinit var inputCardDate: InputInfo
    private lateinit var inputCardHolder: InputInfo
    private lateinit var inputCvv: InputInfo
    private var cardDate: String = ""
    private var cardNumberValue: String = ""
    private var bankShortName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View =
            inflater.inflate(R.layout.payme_payment_enter_credit_card_fragment, container, false)
        inputCardNumber = view.findViewById(R.id.inputCardNumber)
        inputCardHolder = view.findViewById(R.id.inputCardHolder)
        inputCardDate = view.findViewById(R.id.inputCardDate)
        inputCvv = view.findViewById(R.id.inputCvv)
        inputCardDate.imageRight.visibility = View.VISIBLE
        inputCvv.imageRight.visibility = View.VISIBLE
        inputCardDate.imageRight.setImageResource(R.drawable.ic_calendar)
        inputCvv.imageRight.setImageResource(R.drawable.ic_lock)

        inputCardNumber.input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val stringReplace = s?.replace("[^0-9]".toRegex(), "")
                val cardNumber = stringReplace?.subSequence(
                    0,
                    if (stringReplace.length < 16) stringReplace.length else 16
                )
                val cardType = CardType.detect(cardNumber.toString())

                when (cardType) {
                    CardType.JCB -> {
                        inputCardNumber.imageRight.visibility = View.VISIBLE
                        inputCardNumber.imageRight.setImageResource(R.drawable.ic_logo_jcb)
                    }
                    CardType.VISA -> {
                        inputCardNumber.imageRight.visibility = View.VISIBLE
                        inputCardNumber.imageRight.setImageResource(R.drawable.ic_logo_visa)
                    }
                    CardType.MASTER -> {
                        inputCardNumber.imageRight.visibility = View.VISIBLE
                        inputCardNumber.imageRight.setImageResource(R.drawable.ic_logo_mastercard)
                    }
                    else -> {
                        inputCardNumber.imageRight.visibility = View.GONE
                    }
                }
                bankShortName = cardType.toString()
                if(cardNumber?.length!! > 0){
                    if(cardType == CardType.UNKNOWN){
                        inputCardNumber.setError(getString(R.string.number_card_error))
                    }else{
                        inputCardNumber.setDefault(null)
                    }
                }else{
                    inputCardNumber.setDefault(null)
                }
                if (cardNumber?.length!! >= 6) {
                    var cardNew = ""
                    if (cardNumber.length >16) {
                        for (i in cardNumber.indices) {
                            if ((i == 7 || i == 15) && (i + 1 < cardNumber.length)) {
                                cardNew += cardNumber[i] + " "
                            } else {
                                cardNew += cardNumber[i]
                            }
                        }
                    } else {
                        for (i in cardNumber.indices) {
                            if ((i == 3 || i == 7 || i == 11) && (i + 1 < cardNumber.length)) {
                                cardNew += cardNumber[i] + " "
                            } else {
                                cardNew += cardNumber[i]
                            }
                        }
                    }
                    cardNumberValue = cardNew.replace("[^0-9]".toRegex(), "")

                    if(cardNew!=s.toString()){
                        inputCardNumber.input.removeTextChangedListener(this)
                        val cursorPosition: Int = inputCardNumber.input.selectionStart
                        val newCursorPosition = cursorPosition + (cardNew.length - s.length)
                        inputCardNumber.input.setText(cardNew)
                        val check = newCursorPosition
                        inputCardNumber.input.setSelection(check!!)
                        inputCardNumber.input.addTextChangedListener(this)
                    }
                }
                validateCard()
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
                if(newDate!=s.toString()) {
                    inputCardDate.input.removeTextChangedListener(this)
                    val cursorPosition: Int = inputCardDate.input.selectionStart
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
                        inputCardDate.setError(getString(R.string.release_date_error))
                    } else {
                        cardDate = "20${yead}-${newDate.substring(0, 2)}-12T12:08:32.860Z"
                        inputCardDate.setDefault(null)
                    }
                } else {
                    cardDate = ""
                    inputCardDate.setDefault(null)
                }
                validateCard()

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })
        inputCvv.input.addTextChangedListener {
            validateCard()
        }
        inputCardHolder.input.addTextChangedListener {
            validateCard()
        }
        return view
    }

    fun validateCard() {
        var validateNumberCard = false
        if(bankShortName == CardType.VISA.toString() &&  ( cardNumberValue.length==13 ||  cardNumberValue.length ==16)){
            validateNumberCard  = true

        }
        if((bankShortName == CardType.JCB.toString() || bankShortName == CardType.MASTER.toString()) && cardNumberValue.length ==16){
            validateNumberCard  = true
        }

        if (inputCardHolder.input.text.isNotEmpty() && cardDate.isNotEmpty() && validateNumberCard && inputCvv.input.text.length == 3) {
            val cardInfo = CardInfo(
                inputCardDate.input.text.toString(),
                inputCardNumber.input.text.toString(),
                bankShortName,
                cardNumberValue,
                inputCardHolder.input.text.toString(),
                cardDate,
                inputCvv.input.text.toString()
            )
            EventBus.getDefault().post(CheckInputAtm(true, cardInfo))
        } else {
            EventBus.getDefault().post(CheckInputAtm(false, CardInfo("", "", "", "", "", "", "")))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51234) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val number = data?.getStringExtra("cardNumber")
                    inputCardNumber.input.setText(number)
                }
                Activity.RESULT_CANCELED -> {
                    Log.d("HIEU", "Scan canceled")
                }
                else -> {
                    Log.d("HIEU", "Scan failed")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid) {
            if (requestCode == PermissionCamera().CAMERA_REQUEST_CODE) {
                ScanActivity.warmUp(requireContext())
                val intent = Intent(context, ScanActivityImpl::class.java)
                startActivityForResult(intent, 51234)
                return
            }
        } else {
            if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(
                    permissions[0]
                )
            ) {
                PayME.enableSetting = true
            }
        }
    }
}