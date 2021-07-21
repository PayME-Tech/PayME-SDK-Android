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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.component.Button
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.CheckActivityResult
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.evenbus.RequestPermissionsResult
import vn.payme.sdk.kyc.*
import vn.payme.sdk.store.Store

class ScanQR : DialogFragment() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private val PICK_IMAGE = 1
    private var toggleTorch = false
    private var btnPicker: LinearLayout? = null
    private var btnTorch: LinearLayout? = null
    private var buttonBack: ImageView? = null
    private var buttonBackHeaderErrorCamera: ImageView? = null
    private var enableSetting = false
    private var containerErrorCamera: ConstraintLayout? = null
    private var buttonOpenSetting: Button? = null
    var cameraAccept = false

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
//        PayME.showError("event.resultCode"+event.resultCode)
//        if(PayME.activityResult !=null){
//            checkActivityResult(PayME.activityResult!!.requestCode,PayME.activityResult!!.resultCode,PayME.activityResult!!.data)
//            PayME.activityResult = null
//        }
    }

    @Subscribe
    fun eventRequestPermissionsResult(event: RequestPermissionsResult) {
        checkRequestPermissionsResult(event.requestCode, event.permissions, event.grantResults)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scannerView = view.findViewById<CodeScannerView>(R.id.scan_qr)
        scannerView.setOnClickListener {
            if (cameraAccept) {
                codeScanner.startPreview()
            }
        }
        dialog?.window?.setStatusBarColor(Color.TRANSPARENT);
        dialog?.window?.setBackgroundDrawable(Store.config.colorApp.backgroundColor);

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val activity = requireActivity()
            codeScanner = CodeScanner(activity, scannerView)
            codeScanner.decodeCallback = DecodeCallback {
                activity.runOnUiThread {
                    dismiss()
                    val payme = PayME()
                    val payCode = arguments?.getString("payCode")
                    payme.payQRCodeInSDK(PayME.fragmentManager, it.text.toString(), payCode!!)
                }
            }
            cameraAccept = true
            containerErrorCamera?.visibility = View.GONE

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
        buttonBack = v.findViewById(R.id.button_back)
        containerErrorCamera = v.findViewById(R.id.containerErrorCamera)
        buttonOpenSetting = v.findViewById(R.id.buttonOpenSetting)
        buttonBackHeaderErrorCamera = v.findViewById(R.id.buttonBackHeaderErrorCamera)
        btnPicker!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE)
        }
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
            if (cameraAccept) {
                if (!toggleTorch) {
                    codeScanner.isFlashEnabled = true
                    toggleTorch = true
                } else {
                    codeScanner.isFlashEnabled = false
                    toggleTorch = false
                }
            }
        }

    }

    fun checkRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid) {
            val activity = requireActivity()
            cameraAccept = true
            codeScanner = CodeScanner(activity, scannerView)
            codeScanner.decodeCallback = DecodeCallback {
                activity.runOnUiThread {
                    dismiss()
                    var even: EventBus = EventBus.getDefault()
                    var myEven: MyEven = MyEven(TypeCallBack.onScan, it.text.toString())
                    even.post(myEven)
                }
            }
            codeScanner.startPreview()
            containerErrorCamera?.visibility = View.GONE
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
        if (cameraAccept) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        if (cameraAccept) {
            codeScanner.releaseResources()
        }
        super.onPause()
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