package vn.payme.sdk.kyc

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.VideoResult
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import java.io.File


class TakeVideoKycFragment : Fragment() {
    private var cameraKitView: CameraView? = null
    private var buttonTakePicture: LottieAnimationView? = null
    private var buttonBackHeader: ImageView? = null
    private var cardViewCamera: CardView? = null
    private var loadingVideo: Boolean = false
    private var buttonBackHeaderErrorCamera: ImageView? = null
    private var enableSetting = false
    private var containerErrorCamera: ConstraintLayout? = null
    private var buttonOpenSetting: Button? = null

    private inner class Listener : CameraListener() {
        override fun onVideoTaken(result: VideoResult) {
          super.onVideoTaken(result)
            VideoPreviewFragment.videoResult = result
            val newFragment = VideoPreviewFragment()
            val fragment = activity?.supportFragmentManager?.beginTransaction()
            fragment?.replace(R.id.content_kyc, newFragment)
            fragment?.commit()
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
        buttonBackHeader = view!!.findViewById(R.id.buttonBackHeader)
        cardViewCamera = view!!.findViewById(R.id.cardViewCamera)

        containerErrorCamera = view!!.findViewById(R.id.containerErrorCamera)
        buttonOpenSetting = view!!.findViewById(R.id.buttonOpenSetting)
        buttonBackHeaderErrorCamera = view!!.findViewById(R.id.buttonBackHeaderErrorCamera)
        PermisionCamera().requestCamera(requireContext(),requireActivity())
        buttonOpenSetting!!.setOnClickListener {
            if (enableSetting) {
                PermisionCamera().openSetting(requireActivity())
            } else {
                PermisionCamera().requestCamera(requireContext(),requireActivity())
            }
        }
        buttonBackHeader!!.setOnClickListener {
            activity?.finish()
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid && !cameraKitView!!.isOpened) {
            cameraKitView!!.open()
            containerErrorCamera?.visibility = View.GONE
        } else {
            if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(
                    permissions[0]!!
                )
            ) {
                enableSetting = true
                containerErrorCamera?.visibility = View.VISIBLE
            } else {
                containerErrorCamera?.visibility = View.VISIBLE
            }
        }
    }
//    override fun onResume() {
//        super.onResume()
//        PermisionCamera().requestCamera(requireContext(),requireActivity())
//    }


}