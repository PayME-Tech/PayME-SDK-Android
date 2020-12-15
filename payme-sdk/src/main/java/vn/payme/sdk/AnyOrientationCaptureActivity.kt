package vn.payme.sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureManager
import kotlinx.android.synthetic.main.orientation_capture_activity.*
import vn.payme.sdk.PayME.Companion.context


class AnyOrientationCaptureActivity : AppCompatActivity() {
    private val PICK_IMAGE = 1
    private var toggleTorch = true
    private lateinit var capture: CaptureManager
    private var btnPicker: LinearLayout? = null
    private var btnTorch: LinearLayout? = null
    private var popup: PayMEQRCodePopup = PayMEQRCodePopup()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.orientation_capture_activity)

        mappingView()
        initScanner(savedInstanceState)
        eventPress()

//        popup.show(this.supportFragmentManager, "ModalBottomSheet")
    }

    private fun eventPress() {
        btnPicker!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE)
        }
        btnTorch!!.setOnClickListener {
            try {
                val cameraManager: CameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraId = cameraManager.cameraIdList[0]
                cameraManager.setTorchMode(cameraId, toggleTorch)
                toggleTorch = !toggleTorch
            } catch (e: java.lang.Exception) {
            }
        }
    }

    private fun mappingView() {
        btnPicker = findViewById(R.id.button_picker)
        btnTorch = findViewById(R.id.button_torch)
    }

    private fun initScanner(savedInstanceState: Bundle?) {
        capture = CaptureManager(this, bcScanner)
        capture.apply {
            initializeFromIntent(intent, savedInstanceState)
            decode()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
            Log.d("RRR", bitmap.toString())
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
                    Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show()
                } catch (e: NotFoundException) {
                    Log.d("TAG", "Not found")
                }
            }
        }

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result?.let {
            Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        capture.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return bcScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}