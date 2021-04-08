package vn.payme.sdk.credit

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.common.util.Base64Utils
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Flash
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.kyc.PermisionCamera
import java.io.ByteArrayOutputStream


class CameraTakeProfileCreditActivity : AppCompatActivity() {
    companion object {
        public val EXTRA_DATA = "EXTRA_DATA"
    }

    private var cameraKitView: CameraView? = null
    private var buttonTakePicture: ImageView? = null
    private var buttonBackHeader: ImageView? = null
    private var buttonOnOffFlash: ImageView? = null
    private var buttonChooseGallery: ImageView? = null
    private val PICK_IMAGE = 1

    private var buttonBackHeaderErrorCamera: ImageView? = null
    private var enableSetting = false
    private var containerErrorCamera: ConstraintLayout? = null
    private var buttonOpenSetting: Button? = null
    private inner class Listener : CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            val bytearray = result.data
            val image: String = Base64Utils.encode(bytearray)
            var even: EventBus = EventBus.getDefault()
            var myEven: MyEven = MyEven(TypeCallBack.onTakeImageResult, image)
            even.post(myEven)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_take_profile_credit)
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setBackgroundDrawable(PayME.colorApp.backgroundColor);
        cameraKitView = findViewById(R.id.previewCamera)
        buttonTakePicture = findViewById(R.id.btn_takepicture)
        buttonChooseGallery = findViewById(R.id.buttonChooseGallery)
        buttonOnOffFlash = findViewById(R.id.buttonOnOffFlash)


        buttonBackHeader = findViewById(R.id.buttonBackHeader)


        containerErrorCamera = findViewById(R.id.containerErrorCamera)
        buttonOpenSetting = findViewById(R.id.buttonOpenSetting)
        buttonBackHeaderErrorCamera = findViewById(R.id.buttonBackHeaderErrorCamera)

        PermisionCamera().requestCamera(this, this)
        buttonChooseGallery?.setOnClickListener {
            val i = Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(i, PICK_IMAGE)
        }
        buttonOpenSetting!!.setOnClickListener {
            if (enableSetting) {
                PermisionCamera().openSetting(this)
            } else {
                PermisionCamera().requestCamera(this, this)
            }
        }
        buttonBackHeader!!.setOnClickListener {
            finish()
        }
        cameraKitView!!.setLifecycleOwner(this)
        cameraKitView!!.addCameraListener(Listener())

        buttonTakePicture?.setOnClickListener {
            cameraKitView!!.takePictureSnapshot()
        }
        buttonOnOffFlash?.setOnClickListener {
            if (cameraKitView!!.flash == Flash.TORCH) {
                cameraKitView!!.flash = Flash.OFF
                buttonOnOffFlash?.setImageResource(R.drawable.ic_noflash)
            } else {
                cameraKitView!!.flash = Flash.TORCH
                buttonOnOffFlash?.setImageResource(R.drawable.ic_iconflash)

            }


        }
    }

    fun BitMapToString(bitmap: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64Utils.encode(b)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
            val image: String? = BitMapToString(bitmap)
            if (image !== null) {
                var even: EventBus = EventBus.getDefault()
                var myEven: MyEven = MyEven(TypeCallBack.onTakeImageResult, image)
                even.post(myEven)
                finish()
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid && !cameraKitView!!.isOpened) {
            cameraKitView!!.open()
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
}