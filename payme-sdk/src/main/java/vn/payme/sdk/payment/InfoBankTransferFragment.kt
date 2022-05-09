package vn.payme.sdk.payment

import android.graphics.*
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.R
import vn.payme.sdk.evenbus.ListBankTransfer
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.hepper.Clipboard
import vn.payme.sdk.model.BankTransferInfo
import vn.payme.sdk.store.Store
import java.text.DecimalFormat
import android.graphics.BitmapFactory
import android.graphics.Bitmap

class InfoBankTransferFragment : Fragment() {
    private lateinit var textBankName: TextView
    private lateinit var textBankName2: TextView
    private lateinit var textAmount: TextView
    private lateinit var textAccountNumber: TextView
    private lateinit var textAccountHolder: TextView
    private lateinit var textNoteVietQr: TextView
    private lateinit var textChangeBank: TextView
    private lateinit var textNote: TextView
    private lateinit var buttonChangeBank: CardView
    private lateinit var buttonCopiAccountNumber: LinearLayout
    private lateinit var buttonCopiNote: LinearLayout
    private lateinit var imageCopiAccountNumber: ImageView
    private lateinit var imageCopiNote: ImageView
    private lateinit var imageBankBottom: ImageView
    private lateinit var imageBankTop: ImageView
    private lateinit var lineTop: ImageView
    private lateinit var imageQR: ImageView
    private lateinit var containerQr: ConstraintLayout
    private lateinit var containerNote: LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            inflater.inflate(R.layout.payme_payment_bank_transfer_fragment, container, false)
        textBankName = view.findViewById(R.id.txtBankName)
        textBankName2 = view.findViewById(R.id.textBankName2)
        textAmount = view.findViewById(R.id.txtMoneyTransfer)
        textAccountNumber = view.findViewById(R.id.txtAccountNumber)
        textAccountHolder = view.findViewById(R.id.txtAccountHolder)
        textNoteVietQr = view.findViewById(R.id.txtNoteVietQr)
        textChangeBank = view.findViewById(R.id.txtChangeBank)
        containerNote = view.findViewById(R.id.container_note)
        imageBankBottom = view.findViewById(R.id.imageBankBottom)
        lineTop = view.findViewById(R.id.line1)
        imageBankTop = view.findViewById(R.id.imageBankTop)
        imageQR = view.findViewById(R.id.imageQR)
        textNote = view.findViewById(R.id.txtNote)
        buttonChangeBank = view.findViewById(R.id.buttonChangeBank)
        buttonCopiAccountNumber = view.findViewById(R.id.container_account_number)
        buttonCopiNote = view.findViewById(R.id.container_note)
        imageCopiAccountNumber = view.findViewById(R.id.imageCopiAccountNumber)
        imageCopiNote = view.findViewById(R.id.imageCopiNote)
        containerQr = view.findViewById(R.id.containerQr)
        setupUi()
        EventBus.getDefault().register(this)
        val bank = EventBus.getDefault().getStickyEvent(BankTransferInfo::class.java)
        if (bank != null) {
            setInfoBank(bank)
            setAmount()
        }
        onClick()
        return view
    }

    private fun onClick() {
        buttonCopiAccountNumber.setOnClickListener {
            Clipboard().setClipboard(requireContext(), textAccountNumber.text.toString())
        }
        buttonCopiNote.setOnClickListener {
            Clipboard().setClipboard(requireContext(), textNote.text.toString())
        }
        buttonChangeBank.setOnClickListener {
            val popupSearchBank = PopupSearchBank()
            popupSearchBank.show(parentFragmentManager, null)
        }
    }

    private fun setupUi() {
        val listBankInfo = EventBus.getDefault().getStickyEvent(ListBankTransfer::class.java)
        if (listBankInfo.listBankTransferInfo.size == 1) {
            buttonChangeBank.visibility = View.GONE
        } else {
            lineTop.visibility = View.GONE
        }
        ChangeColorImage().changeColor(
            requireContext(),
            imageCopiAccountNumber!!,
            R.drawable.ic_copi2,
            1
        )
        ChangeColorImage().changeColor(requireContext(), imageCopiNote!!, R.drawable.ic_copi2, 1)
        textChangeBank.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
    }

    private fun setAmount() {
        var iStart = 0
        var iEnd = 0
        val decimal = DecimalFormat("#,###")
        val amount = "${decimal.format(Store.paymentInfo.infoPayment!!.amount)} đ"
        val spannable: Spannable =
            SpannableString("${getString(R.string.amount)} ${amount} ${getString(R.string.go_to_the_account_information_below)} :")
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
    fun changeBank(event: BankTransferInfo) {
        if (event != null) {
            setInfoBank(event)
        }
    }

    fun mergeBitmaps(logo: Bitmap?, qrcode: Bitmap): Bitmap? {
        val combined = Bitmap.createBitmap(qrcode.width, qrcode.height, qrcode.config)
        val canvas = Canvas(combined)
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height
        canvas.drawBitmap(qrcode, Matrix(), null)
        val resizeLogo = Bitmap.createScaledBitmap(logo!!, canvasWidth / 6, canvasHeight / 6, true)
        val centreX = (canvasWidth - resizeLogo.width) / 2
        val centreY = (canvasHeight - resizeLogo.height) / 2
        canvas.drawBitmap(resizeLogo, centreX.toFloat(), centreY.toFloat(), null)
        return combined
    }

    private fun setInfoBank(bank: BankTransferInfo) {
        val picasso = Picasso.get()
        picasso.setIndicatorsEnabled(false)
        picasso.load("https://static.payme.vn/image_bank/icon_banks/icon${bank.swiftCode}@2x.png")
            .resize(150, 150)
            .centerInside()
            .into(imageBankBottom)
        picasso.load("https://static.payme.vn/image_bank/icon_banks/icon${bank.swiftCode}@2x.png")
            .resize(150, 150)
            .centerInside()
            .into(imageBankTop)
        if (bank.qrContent != "null") {
            val writer = QRCodeWriter()
            val hintMap: MutableMap<EncodeHintType, Any> = HashMap()
            hintMap[EncodeHintType.MARGIN] = 0
            val bitMatrix = writer.encode(bank.qrContent, BarcodeFormat.QR_CODE, 512, 512, hintMap)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            val overlay = BitmapFactory.decodeResource(resources, R.drawable.logo_vietqr_small)
            imageQR.setImageBitmap(mergeBitmaps(overlay, bitmap))
            containerQr.visibility = View.VISIBLE
            textNoteVietQr.visibility = View.VISIBLE
        } else {
            textNoteVietQr.visibility = View.GONE
            containerQr.visibility = View.GONE
        }

        textBankName.text = bank.bankName
        textBankName2.text = bank.bankName
        textAccountNumber.text = bank.bankAccountNumber
        textAccountHolder.text = bank.bankAccountName
        if (bank.content == "null") {
            containerNote.visibility = View.GONE
        } else {
            containerNote.visibility = View.VISIBLE
        }
        textNote.text = bank.content
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}

