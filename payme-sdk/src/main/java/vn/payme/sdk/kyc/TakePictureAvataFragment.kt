package vn.payme.sdk.kyc

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout

import androidx.fragment.app.Fragment
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.payment.PopupTakeVideo


class TakePictureAvataFragment : Fragment() {
    private var cameraKitView: CameraView? = null
    private var buttonTakePicture: ImageView? = null
    private var layoutConfirm: ConstraintLayout? = null
    private var imagePreView: ImageView? = null
    private var buttonBackHeader: ImageView? = null
    private var buttonBackHeader2: ImageView? = null
    private var buttonBack: Button? = null
    private var buttonNext: Button? = null
    private var saveImage: ByteArray? = null
    private var cardViewCamera: CardView? = null

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

            saveImage = result.data

            layoutConfirm?.visibility = View.VISIBLE

        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View? = inflater?.inflate(R.layout.take_picture_image_avata, container, false)
        cameraKitView = view!!.findViewById(R.id.previewCamera)
        buttonTakePicture = view!!.findViewById(R.id.btn_takepicture)
        layoutConfirm = view!!.findViewById(R.id.confirm_screen)
        imagePreView = view!!.findViewById(R.id.previewImage)
        buttonBack = view!!.findViewById(R.id.buttonBack)
        buttonNext = view!!.findViewById(R.id.buttonNext)
        buttonBackHeader = view!!.findViewById(R.id.buttonBackHeader)

        buttonBackHeader2 = view!!.findViewById(R.id.buttonBackHeader2)
        cardViewCamera = view!!.findViewById(R.id.cardViewCamera)

        //ErrorCamera
        containerErrorCamera = view!!.findViewById(R.id.containerErrorCamera)
        buttonOpenSetting = view!!.findViewById(R.id.buttonOpenSetting)
        buttonBackHeaderErrorCamera = view!!.findViewById(R.id.buttonBackHeaderErrorCamera)

        PermisionCamera().requestCamera(requireContext(),requireActivity())

        buttonOpenSetting!!.setOnClickListener {
            if (enableSetting) {
                PermisionCamera().openSetting(requireActivity())
            } else {
                PermisionCamera().requestCamera(requireContext(),requireActivity())
            }
        }

        buttonBackHeader2!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
        }
        buttonBackHeader!!.setOnClickListener {
            activity?.finish()
        }
        buttonBackHeaderErrorCamera!!.setOnClickListener {
            activity?.finish()
        }
        buttonBack!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE

        }
        cameraKitView!!.setLifecycleOwner(this)
        cameraKitView!!.addCameraListener(Listener())


//        cardCornerRadius

        buttonNext!!.setOnClickListener {


            CameraKycActivity.imageFace = saveImage
            if (PayME.kycVideo) {
                val popupTakeVideo = PopupTakeVideo()
                popupTakeVideo.show(parentFragmentManager, "ModalBottomSheet")

            } else {
                val newFragment = UploadKycFragment()
                val fragment = activity?.supportFragmentManager?.beginTransaction()
                fragment?.addToBackStack(null)
                fragment?.add(R.id.content_kyc, newFragment)
                fragment?.commit()
            }


        }

        buttonTakePicture?.setOnClickListener {
            cameraKitView!!.takePictureSnapshot()
        }

        return view
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
//        PermisionCamera().requestCamera(requireContext(),requireActivity())
//    }


}