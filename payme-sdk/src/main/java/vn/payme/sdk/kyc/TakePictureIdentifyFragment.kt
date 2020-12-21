package vn.payme.sdk.kyc

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.camerakit.CameraKitView
import com.camerakit.CameraKitView.ImageCallback
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.UploadKycApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.TypeCallBack
import vn.payme.sdk.model.TypeIdentify
import vn.payme.sdk.payment.PopupSelectTypeIdentify
import java.io.ByteArrayOutputStream


class CameraKyc2Activity : Fragment() {
    private var cameraKitView: CameraKitView? = null
    private var buttonTakePicture: ImageView? = null
    private var layoutConfirm: ConstraintLayout? = null
    private var layoutUpload: ConstraintLayout? = null
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View? = inflater?.inflate(R.layout.camera_activity, container, false)

        EventBus.getDefault().register(this)

        cameraKitView = view!!.findViewById(R.id.previewCamera)
        buttonTakePicture = view!!.findViewById(R.id.btn_takepicture)
        layoutConfirm = view!!.findViewById(R.id.confirm_screen)
        layoutUpload = view!!.findViewById(R.id.upLoadKyc)
        layoutUpload!!.background = PayME.colorApp.backgroundColor

        imagePreView = view!!.findViewById(R.id.previewImage)
        buttonBack = view!!.findViewById(R.id.buttonBack)
        buttonNext = view!!.findViewById(R.id.buttonNext)
        buttonBackHeader = view!!.findViewById(R.id.buttonBackHeader)
        buttonBackHeader2 = view!!.findViewById(R.id.buttonBackHeader2)
        textGuiTakePicture = view!!.findViewById(R.id.textGuiTakePicture)
        textTypeIdentify = view!!.findViewById(R.id.title_type_identify)
        buttonSelectTypeIdentify = view!!.findViewById(R.id.buttonSelectTypeIdentify)

        buttonBackHeader2!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
        }
        buttonBackHeader!!.setOnClickListener {
            finish()
        }
        buttonBack!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE

        }
        buttonNext!!.setOnClickListener {
            if (imageFront != null) {
                layoutUpload!!.visibility = View.VISIBLE
                imageBackSide = saveImage
                val uploadKycApi = UploadKycApi()
                uploadKycApi.uploadImage(PayME.context,
                    imageFront!!,
                    imageBackSide!!,
                    typeIdentify,
                    onSuccess = {
                        finish()
                        var even: EventBus = EventBus.getDefault()
                        var myEven: MyEven = MyEven(TypeCallBack.onReload, "")
                        even.post(myEven)

                    },
                    onError = { jsonObject, code, message ->
                        layoutUpload!!.visibility = View.GONE
                        val toast: Toast =
                            Toast.makeText(PayME.context, message, Toast.LENGTH_SHORT)
                        toast.view?.setBackgroundColor(
                            ContextCompat.getColor(
                                PayME.context,
                                R.color.scarlet
                            )
                        )
                        toast.show()
                    })

            } else {
                textGuiTakePicture?.text = "Mặt sau"
                layoutConfirm!!.visibility = View.GONE
                imageFront = saveImage
            }
        }
        buttonSelectTypeIdentify?.setOnClickListener {
            val popupSelectTypeIdentify = PopupSelectTypeIdentify()
            popupSelectTypeIdentify.show(fragmentManager!!, "ModalBottomSheet")
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
        return  view
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


}