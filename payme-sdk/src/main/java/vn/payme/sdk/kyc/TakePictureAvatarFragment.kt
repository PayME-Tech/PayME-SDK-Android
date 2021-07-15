package vn.payme.sdk.kyc

import android.graphics.BitmapFactory
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
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.evenbus.ChangeFragmentKYC
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.payment.PopupTakeVideo
import vn.payme.sdk.store.Store


class TakePictureAvatarFragment : Fragment() {
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
        val view: View = inflater?.inflate(R.layout.take_picture_image_avata, container, false)
        cameraKitView = view.findViewById(R.id.previewCamera)
        buttonTakePicture = view.findViewById(R.id.btn_takepicture)
        layoutConfirm = view.findViewById(R.id.confirm_screen)
        imagePreView = view.findViewById(R.id.previewImage)
        buttonBack = view.findViewById(R.id.buttonBack)
        buttonNext = view.findViewById(R.id.buttonNext)
        buttonBackHeader = view.findViewById(R.id.buttonBackHeader)
        buttonBack?.setButtonTypeBorder()

        buttonBackHeader2 = view.findViewById(R.id.buttonBackHeader2)
        cardViewCamera = view.findViewById(R.id.cardViewCamera)
        ChangeColorImage().changeColor(requireContext(),buttonTakePicture!!,R.drawable.ic_buttontakepic,1)
        buttonBackHeader2!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
        }
        buttonBackHeader!!.setOnClickListener {
            EventBus.getDefault().post(ChangeFragmentKYC.CLOSE)
        }
        buttonBack!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
        }
        cameraKitView!!.setLifecycleOwner(this)
        cameraKitView!!.addCameraListener(Listener())
        cameraKitView!!.open()

        buttonNext!!.setOnClickListener {
            CameraKycPopup.imageFace = saveImage
            if (Store.config.kycVideo) {
                val popupTakeVideo = PopupTakeVideo()
                popupTakeVideo.show(parentFragmentManager, "ModalBottomSheet")
            } else {
                val newFragment = UploadKycFragment()
                val fragment = parentFragmentManager.beginTransaction()
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


    override fun onDestroy() {
        super.onDestroy()
    }

}