package vn.payme.sdk.payment

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.QRContentVietQR
import vn.payme.sdk.evenbus.VietQrTransferInfo
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.hepper.Clipboard
import vn.payme.sdk.store.Store
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class VietQRFragment : Fragment() {
    private lateinit var imageQR: ImageView
    private lateinit var textDownload: TextView
    private lateinit var textBankNumber: TextView
    private lateinit var textAccountName: TextView
    private lateinit var textBankName: TextView
    private lateinit var textContent: TextView
    private lateinit var buttonCopyBankNumber: ImageView
    private lateinit var buttonCopyContent: ImageView
    private lateinit var bankNumberContainer: LinearLayout
    private lateinit var contentContainer: LinearLayout
    var fragmentState = ""
    private val REQUEST_WRITE_EXTERNAL_STORAGE_CODE = 103
    var enableSettingWriteStorage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            inflater.inflate(R.layout.payme_payment_viet_qr_fragment, container, false)
        imageQR = view.findViewById(R.id.imageQR)
        bankNumberContainer = view.findViewById(R.id.bankNumberContainer)
        contentContainer = view.findViewById(R.id.contentContainer)
        textBankNumber = view.findViewById(R.id.txtBankNumber)
        textAccountName = view.findViewById(R.id.txtAccountName)
        textBankName = view.findViewById(R.id.txtBankName)
        textContent = view.findViewById(R.id.txtContent)
        buttonCopyBankNumber = view.findViewById(R.id.iconCopy)
        buttonCopyContent = view.findViewById(R.id.iconCopyContent)
        textDownload = view.findViewById(R.id.txt_download)
        textDownload.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        textDownload.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        textDownload.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (enableSettingWriteStorage) {
                    EventBus.getDefault()
                        .post(
                            ChangeFragmentPayment(
                                TYPE_FRAGMENT_PAYMENT.OPEN_SETTING,
                                "write_external_storage"
                            )
                        )
                } else {
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_WRITE_EXTERNAL_STORAGE_CODE
                    )
                }
            } else {
                imageQR.invalidate()
                val bitmap = imageQR.drawable.toBitmap()
                saveImage(bitmap, requireContext(), getString(R.string.app_name))
            }
        }

        setupUi()

        GlobalScope.launch {
            delay(5000)
            loopCallApi()
        }

        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid) {
            if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_CODE) {
                imageQR.invalidate()
                val bitmap = imageQR.drawable.toBitmap()
                saveImage(bitmap, requireContext(), getString(R.string.app_name))
            }
        } else {
            if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(
                    permissions[0]
                )
            ) {
                enableSettingWriteStorage = true
            }
        }
    }

    private fun saveImage(bitmap: Bitmap, context: Context, folderName: String) {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
                values.put(MediaStore.Images.Media.IS_PENDING, true)

                val uri: Uri? =
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                if (uri != null) {
                    saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                }
            } else {
                val directory = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .toString() + File.separator + folderName
                )

                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val fileName = System.currentTimeMillis().toString() + ".png"
                val file = File(directory, fileName)
                saveImageToStream(bitmap, FileOutputStream(file))
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        } catch (e: Exception) {
            Log.d("HIEU", "error save bitmap $e")
            Toast.makeText(
                requireContext(),
                getString(R.string.download_failed_viet_qr),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun contentValues(): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.download_succeeded_viet_qr),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("HIEU", "error save image $e")
                Toast.makeText(
                    requireContext(),
                    getString(R.string.download_failed_viet_qr),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
            imageQR.setImageBitmap(bitmap)
        }

        // transfer content
        val bankInfo = EventBus.getDefault().getStickyEvent(VietQrTransferInfo::class.java)
        if (bankInfo.bankNumber == null) {
            bankNumberContainer.visibility = View.GONE
        } else {
            textBankNumber.text = bankInfo.bankNumber
            textBankNumber.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
            buttonCopyBankNumber.setOnClickListener {
                Clipboard().setClipboard(requireContext(), bankInfo.bankNumber)
            }
        }

        if (bankInfo.fullName == null) {
            textAccountName.visibility = View.GONE
        } else {
            textAccountName.text = bankInfo.fullName
            textAccountName.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        }

        if (bankInfo.bankName == null) {
            textBankName.visibility = View.GONE
        } else {
            textBankName.text = bankInfo.bankName
        }

        if (bankInfo.bankName == null) {
            textBankName.visibility = View.GONE
        } else {
            textBankName.text = bankInfo.bankName
        }

        if (bankInfo.content == null) {
            contentContainer.visibility = View.GONE
        } else {
            val gradientDrawable = GradientDrawable()
            gradientDrawable.setColor(Color.parseColor(Store.config.colorApp.startColor))
            gradientDrawable.setStroke(4, Color.parseColor(Store.config.colorApp.startColor))
            gradientDrawable.cornerRadius = 12f
            gradientDrawable.alpha = 100

            contentContainer.background = gradientDrawable

            textContent.text = bankInfo.content
            textContent.setTextColor(Color.parseColor(Store.config.colorApp.startColor))

            buttonCopyContent.setOnClickListener {
                Clipboard().setClipboard(requireContext(), bankInfo.content)
            }
        }

        ChangeColorImage().changeColor(requireContext(), buttonCopyContent, R.drawable.ic_copi2, 1)
        ChangeColorImage().changeColor(
            requireContext(),
            buttonCopyBankNumber, R.drawable.ic_copi2, 1
        )
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

