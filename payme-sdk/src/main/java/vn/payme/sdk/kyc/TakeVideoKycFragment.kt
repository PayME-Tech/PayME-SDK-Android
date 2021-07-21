package vn.payme.sdk.kyc

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.SimpleLottieValueCallback
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.VideoResult
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.R
import vn.payme.sdk.evenbus.ChangeFragmentKYC
import vn.payme.sdk.store.Store
import java.io.File


class TakeVideoKycFragment : Fragment() {
    private var cameraKitView: CameraView? = null
    private var buttonTakePicture: LottieAnimationView? = null
    private var buttonBackHeader: ImageView? = null
    private var cardViewCamera: CardView? = null
    private var loadingVideo: Boolean = false


    private inner class Listener : CameraListener() {
        override fun onVideoTaken(result: VideoResult) {
          super.onVideoTaken(result)
            VideoPreviewFragment.videoResult = result
            val newFragment = VideoPreviewFragment()
            val fragment = parentFragmentManager.beginTransaction()
            fragment?.replace(R.id.content_kyc, newFragment)
            fragment?.commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.payme_take_video_kyc, container, false)
        cameraKitView = view.findViewById(R.id.previewCamera)
        buttonTakePicture = view.findViewById(R.id.btn_takepicture)
        buttonBackHeader = view.findViewById(R.id.buttonBackHeader)
        cardViewCamera = view.findViewById(R.id.cardViewCamera)
        cameraKitView!!.open()

        buttonBackHeader!!.setOnClickListener {
            EventBus.getDefault().post(ChangeFragmentKYC.CLOSE)
        }
        buttonTakePicture?.addValueCallback<ColorFilter>(
            KeyPath("Camera", "**"),
            LottieProperty.COLOR_FILTER,
            SimpleLottieValueCallback<ColorFilter?> {
                PorterDuffColorFilter(
                    Color.parseColor(Store.config.colorApp.startColor),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        )
        cameraKitView!!.setLifecycleOwner(this)
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





}