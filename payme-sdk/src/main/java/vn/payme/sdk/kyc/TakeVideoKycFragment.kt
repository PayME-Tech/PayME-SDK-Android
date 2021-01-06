package vn.payme.sdk.kyc

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
import vn.payme.sdk.payment.PopupTakeFace
import vn.payme.sdk.payment.PopupTakeVideo
import java.io.File


class TakeVideoKycFragment : Fragment() {
    private var cameraKitView: CameraView? = null
    private var buttonTakePicture: LottieAnimationView? = null
    private var layoutConfirm: ConstraintLayout? = null
    private var imagePreView: VideoView? = null
    private var containerVideo: ConstraintLayout? = null
    private var buttonBackHeader: ImageView? = null
    private var buttonBackHeader2: ImageView? = null
    private var buttonBack: Button? = null
    private var buttonNext: Button? = null
    private var saveVideo: ByteArray? = null
    private var cardViewCamera: CardView? = null
    private var loadingVideo: Boolean = false
    private var buttonPlay: ImageView? = null
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
                val lp = containerVideo?.layoutParams
                val videoWidth = mp.videoWidth.toFloat()
                val videoHeight = mp.videoHeight.toFloat()
                val viewWidth = containerVideo?.width?.toFloat()
                if (viewWidth != null) {
                    lp?.height = (viewWidth * (videoHeight / videoWidth)).toInt()
                }

                containerVideo?.layoutParams = lp
                loadingVideo = false
                imagePreView?.start()
                imagePreView?.pause()
            }
            imagePreView?.setOnCompletionListener {
                buttonPlay?.visibility = View.VISIBLE
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
        containerVideo = view!!.findViewById(R.id.containerPreviewVideo)
        buttonPlay = view!!.findViewById(R.id.buttonPlay)

        buttonBackHeader2!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
            buttonTakePicture?.progress =0f

        }
        buttonPlay?.setOnClickListener {
            buttonPlay?.visibility = View.GONE
            playVideo()
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
            val imageFront = arguments?.getByteArray("imageFront")
            val imageBackSide = arguments?.getByteArray("imageBackSide")
            val imageFace = arguments?.getByteArray("imageFace")
            bundle.putByteArray("video", saveVideo)
            bundle.putByteArray("imageFace", imageFace)
            bundle.putByteArray("imageFront", imageFront)
            bundle.putByteArray("imageBackSide", imageBackSide)
            val newFragment = UploadKycFragment()
            newFragment.arguments = bundle
            val fragment = activity?.supportFragmentManager?.beginTransaction()
            fragment?.addToBackStack(null)
            fragment?.add(R.id.content_kyc, newFragment)
            fragment?.commit()
        }

        cameraKitView!!.addCameraListener(Listener())
        buttonTakePicture?.setOnClickListener {
            if(!loadingVideo){
                loadingVideo = true
                buttonTakePicture?.playAnimation()
                val screenShotDirPath = activity?.getApplication()!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path
                cameraKitView!!.takeVideoSnapshot(File(screenShotDirPath,"video.mp4"), 5000)
            }

        }

        return view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid && !cameraKitView!!.isOpened) {
            cameraKitView!!.open()
        }
    }


}