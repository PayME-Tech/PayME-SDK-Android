package vn.payme.sdk.kyc

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.model.TypeIdentify
import vn.payme.sdk.payment.*
import vn.payme.sdk.payment.PopupSelectTypeIdentify
import vn.payme.sdk.payment.PopupTakeFace
import vn.payme.sdk.payment.PopupTakeVideo
import java.io.ByteArrayOutputStream


class TakePictureIdentifyFragment : Fragment() {
    private var cameraKitView: CameraView? = null
    private var buttonTakePicture: ImageView? = null
    private var layoutConfirm: ConstraintLayout? = null
    private var imagePreView: ImageView? = null
    private var buttonBackHeader: ImageView? = null
    private var buttonBackHeader2: ImageView? = null
    private var buttonBack: Button? = null
    private var buttonNext: Button? = null
    private var imageFront: ByteArray? = null
    private var imageBackSide: ByteArray? = null
    private var saveImage: ByteArray? = null
    private var textGuiTakePicture: TextView? = null
    private var textTypeIdentify: TextView? = null
    private var buttonSelectTypeIdentify: ConstraintLayout? = null
    private var typeIdentify = "CMND"
    private var buttonSelectImage: LinearLayout? = null

    private var buttonBackHeaderErrorCamera: ImageView? = null
    private var enableSetting = false
    private var containerErrorCamera: ConstraintLayout? = null
    private var buttonOpenSetting: Button? = null


    private inner class Listener : CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            val bytearray = result.data
            val bmp = BitmapFactory.decodeByteArray(bytearray, 0, bytearray.size)
            imagePreView!!.setImageBitmap(
                bmp
            )
            layoutConfirm?.visibility = View.VISIBLE
            saveImage = result.data
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater?.inflate(R.layout.take_picture_image_identify, container, false)

        EventBus.getDefault().register(this)

        cameraKitView = view.findViewById(R.id.previewCamera)
        buttonTakePicture = view.findViewById(R.id.btn_takepicture)
        layoutConfirm = view.findViewById(R.id.confirm_screen)

        imagePreView = view.findViewById(R.id.previewImage)
        buttonBack = view.findViewById(R.id.buttonBack)
        buttonNext = view.findViewById(R.id.buttonNext)
        buttonBackHeader = view.findViewById(R.id.buttonBackHeader)
        buttonBackHeader2 = view.findViewById(R.id.buttonBackHeader2)
        textGuiTakePicture = view.findViewById(R.id.textGuiTakePicture)
        textTypeIdentify = view.findViewById(R.id.title_type_identify)
        buttonSelectTypeIdentify = view.findViewById(R.id.buttonSelectTypeIdentify)
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage)

        containerErrorCamera = view.findViewById(R.id.containerErrorCamera)
        buttonOpenSetting = view.findViewById(R.id.buttonOpenSetting)
        buttonBackHeaderErrorCamera = view.findViewById(R.id.buttonBackHeaderErrorCamera)
        PermisionCamera().requestCamera(requireContext(),requireActivity())
        buttonOpenSetting!!.setOnClickListener {
            if (enableSetting) {
                PermisionCamera().openSetting(requireActivity())
            } else {
                PermisionCamera().requestCamera(requireContext(), requireActivity())
            }
        }


        buttonSelectImage?.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(requireActivity());

        }

        buttonBackHeader2!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
        }
        buttonBackHeader!!.setOnClickListener {
            activity?.finish()
        }
        buttonBack!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE

        }

        buttonNext!!.setOnClickListener {
            if (imageFront != null) {
                imageBackSide = saveImage
                CameraKycActivity.imageBackSide = imageBackSide
                CameraKycActivity.imageFront = imageFront
                if (PayME.kycFace) {
                    val popupTakeFace = PopupTakeFace()
                    popupTakeFace.show(parentFragmentManager, "ModalBottomSheet")

                } else if (PayME.kycVideo) {

                    val popupTakeVideo = PopupTakeVideo()
                    popupTakeVideo.show(parentFragmentManager, "ModalBottomSheet")

                } else {
                    val newFragment = UploadKycFragment()
                    val fragment = activity?.supportFragmentManager?.beginTransaction()
                    fragment?.add(R.id.content_kyc, newFragment)
                    fragment?.addToBackStack(null)
                    fragment?.commit()
                }


            } else {
                textGuiTakePicture?.text = "Mặt sau"
                layoutConfirm!!.visibility = View.GONE
                imageFront = saveImage
            }
        }
        buttonSelectTypeIdentify?.setOnClickListener {
            val popupSelectTypeIdentify = PopupSelectTypeIdentify()
            popupSelectTypeIdentify.show(childFragmentManager, "ModalBottomSheet")
        }
        cameraKitView!!.setLifecycleOwner(this)
        cameraKitView!!.addCameraListener(Listener())


        buttonTakePicture?.setOnClickListener {
            cameraKitView!!.takePictureSnapshot()
        }

        return view
    }


    @Subscribe
    fun onChange(myEven: TypeIdentify) {
        textTypeIdentify?.text = myEven.title
        typeIdentify = myEven.type
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

//    override fun onResume() {
//        super.onResume()
//        PermisionCamera().requestCamera(requireContext(), requireActivity())
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("data" + data.toString())

        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            println("result" + result.toString())
            if (resultCode === RESULT_OK) {
                val resultUri: Uri = result.uri
                val bitmapImage = BitmapFactory.decodeFile(resultUri.path)
                imagePreView!!.setImageBitmap(
                    bitmapImage
                )

                layoutConfirm?.visibility = View.VISIBLE
                val stream = ByteArrayOutputStream()
                bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray: ByteArray = stream.toByteArray()
                saveImage = byteArray

            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }


    }


}