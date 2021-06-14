package vn.payme.sdk.kyc

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.ChangeFragmentKYC
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.store.Store

class CameraKycActivity : DialogFragment() {
    companion object{
        var video :ByteArray? = null
        var imageFace :ByteArray? = null
        var imageFront :ByteArray? = null
        var imageBackSide :ByteArray? = null
        var typeIdentify :String? = null
        var updateOnlyIdentify = false
        private var buttonBackHeaderErrorCamera: ImageView? = null
        private var enableSetting = false
        private var containerErrorCamera: ConstraintLayout? = null
        private var buttonOpenSetting: Button? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
        isCancelable = false
        setStyle(STYLE_NO_FRAME,R.style.DialogStyle);
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(
            R.layout.camera_kyc_activity,
            container, false
        )
        containerErrorCamera = v.findViewById(R.id.containerErrorCamera)
        buttonOpenSetting = v.findViewById(R.id.buttonOpenSetting)
        buttonBackHeaderErrorCamera = v.findViewById(R.id.buttonBackHeaderErrorCamera)
        containerErrorCamera?.visibility = View.VISIBLE
        buttonOpenSetting!!.setOnClickListener {
            if (enableSetting) {
                PermisionCamera().openSetting(requireActivity())
            } else {
                PermisionCamera().requestCameraFragment(requireContext(), this)
            }
        }
        buttonBackHeaderErrorCamera!!.setOnClickListener {
            dismiss()
        }


        video = null
        imageFace = null
        imageFront= null
        imageBackSide = null
        typeIdentify = null
        dialog?.window?.setStatusBarColor(Color.TRANSPARENT);
        dialog?.window?.setBackgroundDrawable(Store.config.colorApp.backgroundColor);

        return  v

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED){
            containerErrorCamera?.visibility = View.GONE
            if (Store.config.kycIdentify) {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<TakePictureIdentifyFragment>(R.id.content_kyc)
                }
            } else if (Store.config.kycFace) {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<TakePictureAvatarFragment>(R.id.content_kyc)
                }
            } else if (Store.config.kycVideo) {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<TakeVideoKycFragment>(R.id.content_kyc)
                }
            }
        }else{
            PermisionCamera().requestCameraFragment(requireContext(),this)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid) {
            if (Store.config.kycIdentify) {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<TakePictureIdentifyFragment>(R.id.content_kyc)
                }
            } else if (Store.config.kycFace) {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<TakePictureAvatarFragment>(R.id.content_kyc)
                }
            } else if (Store.config.kycVideo) {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<TakeVideoKycFragment>(R.id.content_kyc)
                }
            }
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


    @Subscribe
    fun close(event : MyEven){
        if(event.type == TypeCallBack.onExpired){
            dismiss()
        }
    }
    @Subscribe
    fun changeFragment(event : ChangeFragmentKYC){
        if (event ==ChangeFragmentKYC.CLOSE){
            dismiss()
        }
        if(event==ChangeFragmentKYC.KYC_FACE){
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(
                R.id.content_kyc,
                TakePictureAvatarFragment()
            )
            fragment?.commit()
        }
        if(event==ChangeFragmentKYC.KYC_VIDEO){
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(
                R.id.content_kyc,
                TakeVideoKycFragment()
            )
            fragment?.commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}