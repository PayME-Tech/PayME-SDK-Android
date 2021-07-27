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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.common.util.Base64Utils
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Flash
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.CheckActivityResult
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.evenbus.RequestPermissionsResult
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.kyc.*
import vn.payme.sdk.store.Store
import java.io.ByteArrayOutputStream


class CameraTakeProfileCreditActivity : DialogFragment() {
    companion object {
        public val EXTRA_DATA = "EXTRA_DATA"
    }

    private var cameraKitView: CameraView? = null
    private var buttonTakePicture: ImageView? = null
    private var buttonBackHeader: ImageView? = null
    private var buttonOnOffFlash: ImageView? = null
    lateinit var imageErrorCamera: ImageView
    private var buttonChooseGallery: ImageView? = null
    private val PICK_IMAGE = 1
    private var buttonBackHeaderErrorCamera: ImageView? = null
    private var enableSetting = false
    private var containerErrorCamera: ConstraintLayout? = null
    private var buttonOpenSetting: Button? = null
    private var isAcceptCamera = false


    private inner class Listener : CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            val bytearray = result.data
            val image: String = Base64Utils.encode(bytearray)
            var even: EventBus = EventBus.getDefault()
            var myEven: MyEven = MyEven(TypeCallBack.onTakeImageResult, image)
            even.post(myEven)
            dismiss()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setStyle(STYLE_NO_FRAME,R.style.DialogStyle);
    }
    @Subscribe
    fun close(event : MyEven){
        if(event.type == TypeCallBack.onExpired){
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(
            R.layout.payme_activity_camera_take_profile_credit,
            container, false
        )

        dialog?.window?.setStatusBarColor(Color.TRANSPARENT);
        dialog?.window?.setBackgroundDrawable(Store.config.colorApp.backgroundColor);
        cameraKitView = v.findViewById(R.id.previewCamera)
        buttonTakePicture = v.findViewById(R.id.btn_takepicture)
        buttonChooseGallery = v.findViewById(R.id.buttonChooseGallery)
        buttonOnOffFlash = v.findViewById(R.id.buttonOnOffFlash)


        buttonBackHeader = v.findViewById(R.id.buttonBackHeader)


        containerErrorCamera = v.findViewById(R.id.containerErrorCamera)
        imageErrorCamera = v.findViewById(R.id.imageErrorCamera)
        buttonOpenSetting = v.findViewById(R.id.buttonOpenSetting)
        buttonBackHeaderErrorCamera = v.findViewById(R.id.buttonBackHeaderErrorCamera)
        ChangeColorImage().changeColor(requireContext(), imageErrorCamera,R.drawable.icon_error_camera,3)

        buttonChooseGallery?.setOnClickListener {
            val i = Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(i, PICK_IMAGE)
        }
        buttonOpenSetting!!.setOnClickListener {
            if (enableSetting) {
                PermissionCamera().openSetting(requireActivity())
            } else {
                PermissionCamera().requestCameraFragment(requireContext(), this)
            }
        }
        buttonBackHeaderErrorCamera!!.setOnClickListener {
            dismiss()
        }
        buttonBackHeader!!.setOnClickListener {
            dismiss()
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
        return  v
    }


    fun BitMapToString(bitmap: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64Utils.encode(b)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED){
            containerErrorCamera?.visibility = View.GONE
            cameraKitView!!.open()
            isAcceptCamera = true

        }else{
            PermissionCamera().requestCameraFragment(requireContext(),this)
        }
    }

    override fun onResume() {
        super.onResume()
        if(!isAcceptCamera){
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){
                containerErrorCamera?.visibility = View.GONE
                cameraKitView!!.open()
            }
        }

    }
    @Subscribe
    fun eventActivityResult(event: CheckActivityResult){
        val image: String? = BitMapToString(event.data)
        if (image !== null) {
            var even: EventBus = EventBus.getDefault()
            var myEven: MyEven = MyEven(TypeCallBack.onTakeImageResult, image)
            even.post(myEven)
            dismiss()
        }
    }
    @Subscribe
    fun eventRequestPermissionsResult(event: RequestPermissionsResult){
        checkRequestPermissionsResult(event.requestCode,event.permissions,event.grantResults)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
   fun checkActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImage)
            val image: String? = BitMapToString(bitmap)
            if (image !== null) {
                var even: EventBus = EventBus.getDefault()
                var myEven: MyEven = MyEven(TypeCallBack.onTakeImageResult, image)
                even.post(myEven)
                dismiss()
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
            isAcceptCamera = true
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