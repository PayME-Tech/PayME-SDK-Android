package vn.payme.sdk

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.CaptureManager
import kotlinx.android.synthetic.main.orientation_capture_activity.*
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.component.Button
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.kyc.PermisionCamera
import vn.payme.sdk.model.TypeCallBack


class AnyOrientationCaptureActivity : AppCompatActivity() {
    private val PICK_IMAGE = 1
    private var toggleTorch = true
    private lateinit var capture: CaptureManager
    private var btnPicker: LinearLayout? = null
    private var btnTorch: LinearLayout? = null
    private var buttonBack: ImageView? = null
    private var popup: PayMEQRCodePopup = PayMEQRCodePopup()

    private var buttonBackHeaderErrorCamera: ImageView? = null
    private var enableSetting = false
    private var containerErrorCamera: ConstraintLayout? = null
    private var buttonOpenSetting: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.orientation_capture_activity)

        mappingView()
        initScanner(savedInstanceState)
        eventPress()
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setBackgroundDrawable(PayME.colorApp.backgroundColor);
        PermisionCamera().requestCamera(this, this)


//        popup.show(this.supportFragmentManager, "ModalBottomSheet")
    }

    private fun eventPress() {
        btnPicker!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE)
        }
        buttonBack!!.setOnClickListener {
            finish()
        }

        btnTorch!!.setOnClickListener {
            Thread {
                try {
                    val inst = Instrumentation()
                    if (toggleTorch) {
                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP)
                    } else {
                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN)
                    }
                    toggleTorch = !toggleTorch
                } catch (e: InterruptedException) {
                }
            }.start()

        }
    }

    private fun mappingView() {
        btnPicker = findViewById(R.id.button_picker)
        btnTorch = findViewById(R.id.button_torch)
        buttonBack = findViewById(R.id.button_back)
        containerErrorCamera = findViewById(R.id.containerErrorCamera)
        buttonOpenSetting = findViewById(R.id.buttonOpenSetting)
        buttonBackHeaderErrorCamera = findViewById(R.id.buttonBackHeaderErrorCamera)

        buttonOpenSetting!!.setOnClickListener {
            if (enableSetting) {
                PermisionCamera().openSetting(this)
            } else {
                PermisionCamera().requestCamera(this, this)
            }
        }
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
        println("requestCode" + requestCode)
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
                    finish()
                    var even: EventBus = EventBus.getDefault()
                    var myEven: MyEven = MyEven(TypeCallBack.onScan, result.toString())
                    even.post(myEven)
                } catch (e: NotFoundException) {
                    popup.show(this.supportFragmentManager, "ModalBottomSheet")
                    Log.d("TAG", "Not found")
                }
            }
        }
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
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid) {
            containerErrorCamera?.visibility = View.GONE
            capture.onResume()
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

    override fun onResume() {
        super.onResume()
        if(PermisionCamera().isGrantedCamera(this)){
            capture.onResume()
        }
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return bcScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}