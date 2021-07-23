package vn.payme.sdk

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.component.Button
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.CheckActivityResult
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.evenbus.RequestPermissionsResult
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.kyc.*
import vn.payme.sdk.store.Store
import java.util.*


class ScanQR : DialogFragment()  {

    private val PICK_IMAGE = 1
    private var toggleTorch = false
    private var btnPicker: LinearLayout? = null
    private var btnTorch: LinearLayout? = null
    private var buttonBack: ImageView? = null
    private var buttonBackHeaderErrorCamera: ImageView? = null
  lateinit var    imageScan: ImageView
    private var enableSetting = false
    private var containerErrorCamera: ConstraintLayout? = null
    private var buttonOpenSetting: Button? = null
    var cameraAccept = false
    lateinit var barcodeView: BarcodeView
    lateinit var textFlash: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setStyle(STYLE_NO_FRAME, R.style.DialogStyle);

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun close(event: MyEven) {
        if (event.type == TypeCallBack.onExpired) {
            dismiss()
        }
    }

    @Subscribe
    fun eventActivityResult(event: CheckActivityResult) {
        val bitmap = event.data
        if (bitmap != null) {
            val width: Int = bitmap.width
            val height: Int = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            bitmap.recycle()
            val source = RGBLuminanceSource(width, height, pixels)
            val bBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            try {
                val result = reader.decode(bBitmap)
                dismiss()
                val payme = PayME()
                val payCode = arguments?.getString("payCode")
                payme.payQRCodeInSDK(PayME.fragmentManager, result.toString(), payCode!!)
            } catch (e: NotFoundException) {
                dismiss()
                var popup: SearchQrResultPopup = SearchQrResultPopup()
                popup.show(parentFragmentManager, "ModalBottomSheet")
            }
        }

    }

    @Subscribe
    fun eventRequestPermissionsResult(event: RequestPermissionsResult) {
        checkRequestPermissionsResult(event.requestCode, event.permissions, event.grantResults)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barcodeView = view.findViewById(R.id.scan_qr)
        val formats: Collection<BarcodeFormat> =
            Arrays.asList(BarcodeFormat.QR_CODE)
        val decoder = DefaultDecoderFactory(formats)
        barcodeView.setDecoderFactory(decoder)
        barcodeView.decodeSingle { result->
            dismiss()
            android.os.Handler().postDelayed(
                {
                    val payme = PayME()
                    val payCode = arguments?.getString("payCode")
                    payme.payQRCodeInSDK(
                        PayME.fragmentManager,
                        result.text.toString(),
                        payCode!!
                    )
                },
                500 // Timeout value
            )

        }

        dialog?.window?.setStatusBarColor(Color.TRANSPARENT);
        dialog?.window?.setBackgroundDrawable(Store.config.colorApp.backgroundColor);

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            visibleCamera()
        } else {
            PermissionCamera().requestCameraFragment(requireContext(), this)
        }


    }


    fun checkActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            val bitmap =
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImage)
            if (bitmap != null) {
                val width: Int = bitmap.width
                val height: Int = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                bitmap.recycle()
                val source = RGBLuminanceSource(width, height, pixels)
                val bBitmap = BinaryBitmap(HybridBinarizer(source))
                val reader = MultiFormatReader()
                try {
                    val result = reader.decode(bBitmap)
                    dismiss()
                    val payme = PayME()
                    val payCode = arguments?.getString("payCode")

                    android.os.Handler().postDelayed(
                        {
                            payme.payQRCodeInSDK(
                                PayME.fragmentManager,
                                result.toString(),
                                payCode!!
                            )
                        },
                        500 // Timeout value
                    )


                } catch (e: NotFoundException) {
                    dismiss()
                    var popup: SearchQrResultPopup = SearchQrResultPopup()
                    popup.show(parentFragmentManager, "ModalBottomSheet")
                }
            }
        }
    }


    private fun mappingView(v: View) {
        btnPicker = v.findViewById(R.id.button_picker)
        btnTorch = v.findViewById(R.id.button_torch)
        imageScan = v.findViewById(R.id.imageScan)
        buttonBack = v.findViewById(R.id.button_back)
        containerErrorCamera = v.findViewById(R.id.containerErrorCamera)
        buttonOpenSetting = v.findViewById(R.id.buttonOpenSetting)
        buttonBackHeaderErrorCamera = v.findViewById(R.id.buttonBackHeaderErrorCamera)
        textFlash = v.findViewById(R.id.txtFlash)
        btnPicker!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE)
        }
        ChangeColorImage().changeColor(requireContext(),imageScan!!,R.drawable.ic_scan_qr,1)

        buttonBack?.setOnClickListener {
            dismiss()
        }
        buttonBackHeaderErrorCamera?.setOnClickListener {
            dismiss()
        }


        buttonOpenSetting!!.setOnClickListener {
            if (enableSetting) {
                PermissionCamera().openSetting(requireActivity())
            } else {
                PermissionCamera().requestCamera(requireContext(), requireActivity())
            }
        }

        btnTorch!!.setOnClickListener {
                if (!toggleTorch) {
                    toggleTorch = true
                    barcodeView.setTorch(true)
                    textFlash.setText(getString(R.string.off_flash))
                } else {
                    toggleTorch = false
                    barcodeView.setTorch(false)
                    textFlash.setText(getString(R.string.on_flash))

                }
        }

    }
    private fun visibleCamera (){
        barcodeView.resume()
        containerErrorCamera?.visibility = View.GONE

    }

    fun checkRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid) {
            visibleCamera()
        } else {
            if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(
                    permissions[0]!!
                )
            ) {
                enableSetting = true
                containerErrorCamera?.visibility = View.VISIBLE
            } else {
                containerErrorCamera?.visibility = View.VISIBLE
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        PayME.onActivityResult(requestCode, resultCode, data)
        checkActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            visibleCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            barcodeView.pause()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(
            R.layout.payme_scan_qr,
            container, false
        )
        mappingView(v)
        return v
    }

}