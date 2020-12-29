package vn.payme.sdk.kyc

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.camerakit.CameraKitView
import com.camerakit.CameraKitView.ImageCallback
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.VideoResult
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.UploadKycApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.TypeCallBack
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files


class TakeVideoKycFragment : Fragment() {
    private var cameraKitView: CameraView? = null
    private var buttonTakePicture: ImageView? = null
    private var layoutConfirm: ConstraintLayout? = null
    private var layoutUpload: ConstraintLayout? = null
    private var imagePreView: VideoView? = null
    private var buttonBackHeader: ImageView? = null
    private var buttonBackHeader2: ImageView? = null
    private var buttonBack: Button? = null
    private var buttonNext: Button? = null
    private var saveImage: ByteArray? = null
    private var cardViewCamera: CardView? = null
    fun playVideo() {
        if (!imagePreView!!.isPlaying) {
            imagePreView?.start()
        }
    }

    private inner class Listener : CameraListener() {
        override fun onVideoTaken(result: VideoResult) {
          super.onVideoTaken(result)
            layoutConfirm?.visibility = View.VISIBLE
            val controller = MediaController(PayME.context)
            controller.setAnchorView(imagePreView)
            controller.setMediaPlayer(imagePreView)
            imagePreView?.setMediaController(controller)
            imagePreView?.setVideoURI(Uri.fromFile(result.file))
            saveImage = File(result.file.absolutePath).inputStream().use { it.readBytes() }
            imagePreView?.setOnPreparedListener { mp ->
                val lp = imagePreView?.layoutParams
                val videoWidth = mp.videoWidth.toFloat()
                val videoHeight = mp.videoHeight.toFloat()
                val viewWidth = imagePreView?.width?.toFloat()
                if (viewWidth != null) {
                    lp?.height = (viewWidth * (videoHeight / videoWidth)).toInt()
                }
                imagePreView?.layoutParams = lp
                playVideo()
                if (result.isSnapshot) {
                    // Log the real size for debugging reason.
                    Log.e("VideoPreview", "The video full size is " + videoWidth + "x" + videoHeight)
                }
            }
        }

        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
        }
    }

    suspend fun uploadKYC() {
        this.layoutUpload!!.visibility = View.VISIBLE
        val uploadKycApi = UploadKycApi()
        val imageFront = arguments?.getByteArray("imageFront")
        val imageBackSide = arguments?.getByteArray("imageBackSide")
        val imageFade = arguments?.getByteArray("imageFade")
        uploadKycApi.upLoadKYC(imageFront, imageBackSide, imageFade, saveImage,
            onSuccess = {
                activity?.finish()
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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater?.inflate(R.layout.take_video_kyc, container, false)
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
        imagePreView!!.setOnClickListener { playVideo() }



//        cardCornerRadius

        buttonNext!!.setOnClickListener {
            layoutUpload!!.visibility = View.VISIBLE
            GlobalScope.launch {
                uploadKYC()

            }


        }

        cameraKitView!!.addCameraListener(Listener())
        buttonTakePicture?.setOnClickListener {
            val screenShotDirPath = activity?.getApplication()!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path

            cameraKitView!!.takeVideo(File(screenShotDirPath,"video.mp4"), 5000)


//            cameraKitView?.captureImage(ImageCallback { cameraKitView, capturedImage ->
//                val bitmapImage = cropBitmapCenter(
//                    BitmapFactory.decodeByteArray(
//                        capturedImage,
//                        0,
//                        capturedImage.size
//                    ), cameraKitView.width, cameraKitView.height
//                )
//                imagePreView!!.setImageBitmap(
//                    bitmapImage
//                )
//
//                layoutConfirm?.visibility = View.VISIBLE
//                val stream = ByteArrayOutputStream()
//                bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//                val byteArray: ByteArray = stream.toByteArray()
//                saveImage = byteArray
//            })


        }

        return view
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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid && !cameraKitView!!.isOpened) {
            cameraKitView!!.open()
        }
    }


}