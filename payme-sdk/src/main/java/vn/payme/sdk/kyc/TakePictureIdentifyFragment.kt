package vn.payme.sdk.kyc

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.camerakit.CameraKitView
import com.camerakit.CameraKitView.ImageCallback
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.model.TypeIdentify
import vn.payme.sdk.payment.PopupSelectTypeIdentify
import java.io.ByteArrayOutputStream


class TakePictureIdentifyFragment : Fragment() {
    private var cameraKitView: CameraKitView? = null
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
    private var buttonSelectImage : LinearLayout? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View? = inflater?.inflate(R.layout.take_picture_image_identify, container, false)

        EventBus.getDefault().register(this)

        cameraKitView = view!!.findViewById(R.id.previewCamera)
        buttonTakePicture = view!!.findViewById(R.id.btn_takepicture)
        layoutConfirm = view!!.findViewById(R.id.confirm_screen)

        imagePreView = view!!.findViewById(R.id.previewImage)
        buttonBack = view!!.findViewById(R.id.buttonBack)
        buttonNext = view!!.findViewById(R.id.buttonNext)
        buttonBackHeader = view!!.findViewById(R.id.buttonBackHeader)
        buttonBackHeader2 = view!!.findViewById(R.id.buttonBackHeader2)
        textGuiTakePicture = view!!.findViewById(R.id.textGuiTakePicture)
        textTypeIdentify = view!!.findViewById(R.id.title_type_identify)
        buttonSelectTypeIdentify = view!!.findViewById(R.id.buttonSelectTypeIdentify)
        buttonSelectImage = view!!.findViewById(R.id.buttonSelectImage)
        buttonSelectImage?.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(requireActivity());

        }

        buttonBackHeader2!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
        }
        buttonBackHeader!!.setOnClickListener {
//            finish()
            activity?.finish()
        }
        buttonBack!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE

        }
        buttonNext!!.setOnClickListener {
            if (imageFront != null) {
                imageBackSide = saveImage
                val bundle: Bundle = Bundle()
                bundle.putByteArray("imageFront", imageFront)
                bundle.putByteArray("imageBackSide", imageBackSide)
                val takePictureAvataFragment = TakePictureAvataFragment()
                takePictureAvataFragment.arguments = bundle
                val fragment = activity?.supportFragmentManager?.beginTransaction()
                fragment?.replace(R.id.content_kyc, takePictureAvataFragment)
                fragment?.commit()

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

        buttonTakePicture?.setOnClickListener {
            cameraKitView?.captureImage(ImageCallback { cameraKitView, capturedImage ->
                val bitmapImage = cropBitmapCenter(
                    BitmapFactory.decodeByteArray(
                        capturedImage,
                        0,
                        capturedImage.size
                    ), cameraKitView.width, cameraKitView.height
                )
                imagePreView!!.setImageBitmap(
                    bitmapImage
                )

                layoutConfirm?.visibility = View.VISIBLE
                val stream = ByteArrayOutputStream()
                bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray: ByteArray = stream.toByteArray()
                saveImage = byteArray


            })


        }
        cameraKitView?.setErrorListener(CameraKitView.ErrorListener { cameraKitView, e ->
            Toast.makeText(this.context, e.toString(), Toast.LENGTH_SHORT).show()

        })
        return view
    }

    override fun onStart() {
        super.onStart()
        cameraKitView!!.onStart()
    }

    private fun cropBitmapCenter(bitmap: Bitmap, cropWidth: Int, cropHeight: Int): Bitmap? {
        var cropWidth = cropWidth
        var cropHeight = cropHeight
        val bitmapWidth = bitmap.width
        val bitmapheight = bitmap.height

        cropWidth = if (cropWidth > bitmapWidth) bitmapWidth else cropWidth
        cropHeight = if (cropHeight > bitmapheight) bitmapheight else cropHeight
        val newX = bitmapWidth / 2 - cropWidth / 2
        val newY = bitmapheight / 2 - cropHeight / 2
        return Bitmap.createBitmap(bitmap, newX, newY, cropWidth, cropHeight)
    }

    @Subscribe
    fun onChange(myEven: TypeIdentify) {
        textTypeIdentify?.text = myEven.title
        typeIdentify = myEven.type
    }


    override fun onResume() {
        super.onResume()
        cameraKitView!!.onResume()
    }

    override fun onPause() {
        cameraKitView!!.onPause()
        super.onPause()
    }

    override fun onStop() {
        cameraKitView!!.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        cameraKitView!!.onStop()
        EventBus.getDefault().unregister(this);
        super.onDestroy()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraKitView!!.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("data"+data.toString())

        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            println("result"+result.toString())
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