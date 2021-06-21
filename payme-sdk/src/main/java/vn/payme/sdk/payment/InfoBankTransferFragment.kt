package vn.payme.sdk.payment

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.R
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.hepper.Clipboard
import vn.payme.sdk.model.BankTransferInfo
import vn.payme.sdk.store.Store
import java.text.DecimalFormat

class InfoBankTransferFragment : Fragment() {
    private lateinit var textBankName: TextView
    private lateinit var textAmount: TextView
    private lateinit var textAccountNumber: TextView
    private lateinit var textAccountHolder: TextView
    private lateinit var textChangeBank: TextView
    private lateinit var textNote: TextView
    private lateinit var buttonChangeBank: CardView
    private lateinit var buttonCopiAccountNumber: LinearLayout
    private lateinit var buttonCopiNote: LinearLayout
    private lateinit var imageCopiAccountNumber: ImageView
    private lateinit var imageCopiNote: ImageView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            inflater?.inflate(R.layout.payment_bank_transfer_fragment, container, false)
        textBankName = view.findViewById(R.id.txtBankName)
        textAmount = view.findViewById(R.id.txtMoneyTransfer)
        textAccountNumber = view.findViewById(R.id.txtAccountNumber)
        textAccountHolder = view.findViewById(R.id.txtAccountHolder)
        textChangeBank = view.findViewById(R.id.txtChangeBank)
        textNote = view.findViewById(R.id.txtNote)
        buttonChangeBank = view.findViewById(R.id.buttonChangeBank)
        buttonCopiAccountNumber = view.findViewById(R.id.container_account_number)
        buttonCopiNote = view.findViewById(R.id.container_note)
        imageCopiAccountNumber = view.findViewById(R.id.imageCopiAccountNumber)
        imageCopiNote = view.findViewById(R.id.imageCopiNote)
        setupUi()
        EventBus.getDefault().register(this)
        val bank  = EventBus.getDefault().getStickyEvent(BankTransferInfo::class.java)

        setInfoBank(bank)
        setAmount()
        onClick()
        return view
    }
    fun onClick(){
        buttonCopiAccountNumber.setOnClickListener {
            Clipboard().setClipboard(requireContext(),textAccountNumber.text.toString())
        }
        buttonCopiNote.setOnClickListener {
            Clipboard().setClipboard(requireContext(),textNote.text.toString())
        }
        buttonChangeBank.setOnClickListener {
            val popupSearchBank = PopupSearchBank()
            popupSearchBank.show(parentFragmentManager,null)
        }

    }
    fun setupUi(){
        ChangeColorImage().changeColor(requireContext(),imageCopiAccountNumber!!,R.drawable.ic_copi2,1)
        ChangeColorImage().changeColor(requireContext(),imageCopiNote!!,R.drawable.ic_copi2,1)
        textChangeBank.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
    }
    fun setAmount (){
        var iStart = 0
        var iEnd = 0
        val decimal = DecimalFormat("#,###")
        val event = EventBus.getDefault().getStickyEvent(PaymentInfoEvent::class.java)
        val amount = "${decimal.format(event.fee + Store.paymentInfo.infoPayment!!.amount)} đ"
        val spannable: Spannable = SpannableString("Vui lòng chuyển khoản ${amount} tới thông tin tài khoản bên dưới :")
        val str = spannable.toString()
        iStart = str.indexOf(amount)
        iEnd = iStart + amount.length
        val ssText = SpannableString(spannable)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                //your code at here.
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.setUnderlineText(false);
                ds.color = resources.getColor(R.color.red)
            }
        }
        ssText.setSpan(clickableSpan, iStart, iEnd, Spanned.SPAN_USER)
        textAmount.setText(ssText)
//        textAmount.setMovementMethod(LinkMovementMethod.getInstance())
//        textAmount.setEnabled(true)
    }

    @Subscribe
    fun changeBank(event : BankTransferInfo){
            setInfoBank(event)
    }
    fun setInfoBank(bank:BankTransferInfo){
        textBankName.text = bank.bankName
        textAccountNumber.text = bank.bankAccountNumber
        textAccountHolder.text = bank.bankAccountName
        textNote.text = bank.content
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }


}

