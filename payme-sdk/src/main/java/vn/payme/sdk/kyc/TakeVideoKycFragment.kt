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
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.VideoResult
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import java.io.File


class TakeVideoKycFragment : Fragment() {
    private var cameraKitView: CameraView? = null
    private var buttonTakePicture: LottieAnimationView? = null
    private var layoutConfirm: ConstraintLayout? = null
    private var imagePreView: VideoView? = null
    private var buttonBackHeader: ImageView? = null
    private var buttonBackHeader2: ImageView? = null
    private var buttonBack: Button? = null
    private var buttonNext: Button? = null
    private var saveVideo: ByteArray? = null
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
            saveVideo = File(result.file.absolutePath).inputStream().use { it.readBytes() }
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


        imagePreView = view!!.findViewById(R.id.previewImage)
        buttonBack = view!!.findViewById(R.id.buttonBack)
        buttonNext = view!!.findViewById(R.id.buttonNext)
        buttonBackHeader = view!!.findViewById(R.id.buttonBackHeader)
        buttonBackHeader2 = view!!.findViewById(R.id.buttonBackHeader2)
        cardViewCamera = view!!.findViewById(R.id.cardViewCamera)

        buttonBackHeader2!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
            buttonTakePicture?.progress =0f

        }
        buttonBackHeader!!.setOnClickListener {
            activity?.finish()
        }
        buttonBack!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
            buttonTakePicture?.progress =0f
        }
        cameraKitView!!.setLifecycleOwner(this)
        imagePreView!!.setOnClickListener { playVideo() }

        buttonNext!!.setOnClickListener {
            val bundle: Bundle = Bundle()
            bundle.putByteArray("imageFront", saveVideo)
            val newFragment = UploadKycFragment()
            newFragment.arguments = bundle
            val fragment = activity?.supportFragmentManager?.beginTransaction()
            fragment?.addToBackStack(null)
            fragment?.add(R.id.content_kyc, newFragment)
            fragment?.commit()
        }

        cameraKitView!!.addCameraListener(Listener())
        buttonTakePicture?.setOnClickListener {
            buttonTakePicture?.playAnimation()
            val screenShotDirPath = activity?.getApplication()!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path
            cameraKitView!!.takeVideoSnapshot(File(screenShotDirPath,"video.mp4"), 5000)
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