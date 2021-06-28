package vn.payme.sdk.kyc

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.otaliastudios.cameraview.VideoResult
import org.greenrobot.eventbus.EventBus

import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.evenbus.ChangeFragmentKYC
import java.io.File


class VideoPreviewFragment : Fragment() {
    private var imagePreView: VideoView? = null
    private var containerVideo: ConstraintLayout? = null
    private var buttonBackHeader2: ImageView? = null
    private var buttonBack: Button? = null
    private var buttonNext: Button? = null
    private var buttonPlay: ImageView? = null
    private var loadingVideo: Boolean = false

    private var saveVideo: ByteArray? = null
    companion object{
        internal var videoResult: VideoResult? = null
    }
    fun playVideo() {
        if (!imagePreView!!.isPlaying) {
            imagePreView?.start()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.video_pewview_fragment, container, false)

        imagePreView = view.findViewById(R.id.previewImage)
        buttonBack = view.findViewById(R.id.buttonBack)
        buttonNext = view.findViewById(R.id.buttonNext)
        buttonBackHeader2 = view.findViewById(R.id.buttonBackHeader2)
        containerVideo = view.findViewById(R.id.containerPreviewVideo)
        buttonPlay = view.findViewById(R.id.buttonPlay)

        val controller = MediaController(context)
        controller.setAnchorView(imagePreView)
        controller.setMediaPlayer(imagePreView)
        imagePreView?.setMediaController(controller)
        imagePreView?.setVideoURI(Uri.fromFile(videoResult?.file))
        saveVideo = File(videoResult?.file?.absolutePath).inputStream().use { it.readBytes() }
        imagePreView?.setOnPreparedListener { mp ->
            val lp = containerVideo?.layoutParams
            val videoWidth = mp.videoWidth.toFloat()
            val videoHeight = mp.videoHeight.toFloat()
            val viewWidth = containerVideo?.width?.toFloat()
            if (viewWidth != null) {
                lp?.height = (viewWidth * (videoHeight / videoWidth)).toInt()
            }
            containerVideo?.layoutParams = lp
            imagePreView?.start()
            imagePreView?.pause()
        }
        imagePreView?.setOnCompletionListener {
            buttonPlay?.visibility = View.VISIBLE
        }

        buttonBackHeader2!!.setOnClickListener {
            EventBus.getDefault().post(ChangeFragmentKYC.CLOSE)
        }
        buttonPlay?.setOnClickListener {
            buttonPlay?.visibility = View.GONE
            playVideo()
        }

        buttonBack!!.setOnClickListener {
            EventBus.getDefault().post(ChangeFragmentKYC.KYC_VIDEO)
        }
        imagePreView!!.setOnClickListener { playVideo() }

        buttonNext!!.setOnClickListener {
            CameraKycPopup.video = saveVideo
            val newFragment = UploadKycFragment()
            val fragment = parentFragmentManager.beginTransaction()
            fragment?.addToBackStack(null)
            fragment?.add(R.id.content_kyc, newFragment)
            fragment?.commit()
        }



        return view
    }
}