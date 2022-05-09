package vn.payme.sdk.payment

import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.R
import vn.payme.sdk.adapter.SupportedBanksVietQRAdapter
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.ListBankVietQR
import vn.payme.sdk.evenbus.QRContentVietQR

class VietQRFragment : Fragment() {
    private lateinit var imageQR: ImageView
    private lateinit var recyclerView: RecyclerView
    var fragmentState = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            inflater.inflate(R.layout.payme_payment_viet_qr_fragment, container, false)
        imageQR = view.findViewById(R.id.imageQR)
        recyclerView = view.findViewById(R.id.viet_qr_bank_recycler_view)
        setupUi()
        GlobalScope.launch {
            delay(5000)
            loopCallApi()
        }
        return view
    }

    private fun setupUi() {
        // qr
        val qrContentVietQR = EventBus.getDefault().getStickyEvent(QRContentVietQR::class.java)
        val qrContent = qrContentVietQR.qrContent
        if (qrContent != "null") {
            val writer = QRCodeWriter()
            val hintMap: MutableMap<EncodeHintType, Any> = HashMap()
            hintMap[EncodeHintType.MARGIN] = 0
            val bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512, hintMap)
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
        }

        // list bank
        val listBankInfo = EventBus.getDefault().getStickyEvent(ListBankVietQR::class.java)
        val supportedBanksVietQRAdapterAdapter = SupportedBanksVietQRAdapter()
        supportedBanksVietQRAdapterAdapter.submitList(listBankInfo.listBankVietQRInfo)
        recyclerView.adapter = supportedBanksVietQRAdapterAdapter
        val layoutManager = FlexboxLayoutManager(requireContext(), FlexDirection.ROW, FlexWrap.WRAP)
        layoutManager.justifyContent = JustifyContent.CENTER
        layoutManager.alignItems = AlignItems.STRETCH
        recyclerView.layoutManager = layoutManager
    }

    private fun mergeBitmaps(logo: Bitmap?, qrcode: Bitmap): Bitmap? {
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

    override fun onStop() {
        fragmentState = "onStop"
        super.onStop()
    }

    override fun onResume() {
        if (fragmentState == "onStop") {
            loopCallApi()
        }
        fragmentState = "onResume"
        super.onResume()
    }

    private fun loopCallApi() {
        val paymentApi = PaymentApi()
        paymentApi.checkTransaction(onSuccess = { jsonObject ->
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
                    if (fragmentState != "onStop") {
                        GlobalScope.launch {
                            delay(5000)
                            loopCallApi()
                        }
                    }
                }
            }
        }, onError = { jsonObject, code, s ->
        })
    }

}

