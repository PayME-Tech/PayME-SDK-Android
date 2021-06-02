package vn.payme.sdk.kyc

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import vn.payme.sdk.R
import vn.payme.sdk.store.Store

class CameraKycActivity : AppCompatActivity(R.layout.camera_kyc_activity) {
    companion object{
        var video :ByteArray? = null
        var imageFace :ByteArray? = null
        var imageFront :ByteArray? = null
        var imageBackSide :ByteArray? = null
        var typeIdentify :String? = null
        var updateOnlyIdentify = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        video = null
        imageFace = null
        imageFront= null
        imageBackSide = null
        typeIdentify = null
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setBackgroundDrawable(Store.config.colorApp.backgroundColor);
        if (savedInstanceState == null) {
                if (Store.config.kycIdentify) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<TakePictureIdentifyFragment>(R.id.content_kyc)
                    }
                } else if (Store.config.kycFace) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<TakePictureAvataFragment>(R.id.content_kyc)
                    }
                } else if (Store.config.kycVideo) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<TakeVideoKycFragment>(R.id.content_kyc)
                    }
                }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}