package vn.payme.sdk.kyc

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.UploadKycApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.TypeCallBack
import vn.payme.sdk.payment.PopupTakeFace
import vn.payme.sdk.payment.PopupTakeIdentify
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

        buttonBackHeader2!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
        }
        buttonBackHeader!!.setOnClickListener {
            activity?.finish()
        }
        buttonBack!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE

        }
        cameraKitView!!.setLifecycleOwner(this)
        cameraKitView!!.addCameraListener(Listener())



//        cardCornerRadius

        buttonNext!!.setOnClickListener {
            val imageFront = arguments?.getByteArray("imageFront")
            val imageBackSide = arguments?.getByteArray("imageBackSide")
            val bundle: Bundle = Bundle()
            bundle.putByteArray("imageFront", imageFront)
            bundle.putByteArray("imageBackSide", imageBackSide)
            bundle.putByteArray("imageFace", saveImage)
            if (PayME.kycVideo) {
                val popupTakeVideo = PopupTakeVideo()
                popupTakeVideo.arguments = bundle
                popupTakeVideo.show(parentFragmentManager, "ModalBottomSheet")
//                val takePictureAvataFragment = TakeVideoKycFragment()
//                takePictureAvataFragment.arguments = bundle
//                val fragment = activity?.supportFragmentManager?.beginTransaction()
//                fragment?.replace(R.id.content_kyc, takePictureAvataFragment)
//                fragment?.commit()
            } else {
                val newFragment = UploadKycFragment()
                newFragment.arguments = bundle
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid && !cameraKitView!!.isOpened) {
            cameraKitView!!.open()
        }
    }


}