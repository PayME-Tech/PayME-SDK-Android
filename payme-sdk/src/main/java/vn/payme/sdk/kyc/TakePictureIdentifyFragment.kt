package vn.payme.sdk.kyc


import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.evenbus.ChangeFragmentKYC
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.model.TypeIdentify
import vn.payme.sdk.payment.PopupSelectTypeIdentify
import vn.payme.sdk.payment.PopupTakeFace
import vn.payme.sdk.payment.PopupTakeVideo
import vn.payme.sdk.store.Store


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
    private lateinit var  buttonDropdown: ImageView




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

        val view: View = inflater?.inflate(R.layout.payme_take_picture_image_identify, container, false)

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
        buttonDropdown = view.findViewById(R.id.buttonDropdown)
        cameraKitView!!.open()
        ChangeColorImage().changeColor(requireContext(),buttonTakePicture!!,R.drawable.ic_buttontakepic,1)
        if(CameraKycPopup.updateOnlyIdentify){
            buttonDropdown.visibility  = View.GONE
        }
        buttonBack?.setButtonTypeBorder()



        if(CameraKycPopup.updateOnlyIdentify){
            CameraKycPopup.typeIdentify = "CCCD"
            textTypeIdentify?.text = "Căn cước công dân"

        }else{
            CameraKycPopup.typeIdentify = "CMND"
        }



        buttonBackHeader2!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
        }
        buttonBackHeader!!.setOnClickListener {
            EventBus.getDefault().post(ChangeFragmentKYC.CLOSE)
        }
        buttonBack!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE

        }

        buttonNext!!.setOnClickListener {
            if (imageFront != null) {
                imageBackSide = saveImage
                CameraKycPopup.imageBackSide = imageBackSide
                CameraKycPopup.imageFront = imageFront
                if (Store.config.kycFace) {
                    val popupTakeFace = PopupTakeFace()
                    popupTakeFace.show(parentFragmentManager, "ModalBottomSheet")

                } else if (Store.config.kycVideo) {

                    val popupTakeVideo = PopupTakeVideo()
                    popupTakeVideo.show(parentFragmentManager, "ModalBottomSheet")


                } else {
                    val newFragment = UploadKycFragment()
                    val fragment = parentFragmentManager.beginTransaction()
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
            if(imageFront==null && !CameraKycPopup.updateOnlyIdentify){
                val popupSelectTypeIdentify = PopupSelectTypeIdentify()
                popupSelectTypeIdentify.show(childFragmentManager, "ModalBottomSheet")
            }

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
        CameraKycPopup.typeIdentify = myEven.type
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }




}