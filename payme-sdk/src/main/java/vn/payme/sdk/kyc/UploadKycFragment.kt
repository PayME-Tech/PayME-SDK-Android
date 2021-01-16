package vn.payme.sdk.kyc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.UploadKycApi
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.TypeCallBack

class UploadKycFragment : Fragment() {
    private var layoutUpload: ConstraintLayout? = null
    private var loadingUploadKycApi = false
    suspend fun uploadKYC() {
        val uploadKycApi = UploadKycApi()
        uploadKycApi.upLoadKYC(CameraKycActivity.imageFront, CameraKycActivity.imageBackSide, CameraKycActivity.imageFace, CameraKycActivity.video,
            onSuccess = {
                loadingUploadKycApi = false
                activity?.finish()
                var even: EventBus = EventBus.getDefault()
                var myEven: MyEven = MyEven(TypeCallBack.onReload, "")
                even.post(myEven)
            },
            onError = { jsonObject, code, message ->
                loadingUploadKycApi = false
                parentFragmentManager.popBackStack()
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
        val view: View = inflater?.inflate(R.layout.upload_kyc_fragment, container, false)
        layoutUpload = view.findViewById(R.id.upLoadKyc)
        layoutUpload!!.background = PayME.colorApp.backgroundColor
        GlobalScope.launch(Dispatchers.Main) {
            if (!loadingUploadKycApi) {
                loadingUploadKycApi = true
                uploadKYC()

            }
        }

        return view


    }
}