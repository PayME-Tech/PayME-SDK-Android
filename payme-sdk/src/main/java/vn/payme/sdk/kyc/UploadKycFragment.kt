package vn.payme.sdk.kyc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.PayMEOpenSDKPopup
import vn.payme.sdk.R
import vn.payme.sdk.api.UploadKycApi
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.ChangeFragmentKYC
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.store.Store


class UploadKycFragment : Fragment() {
    private var layoutUpload: ConstraintLayout? = null
    private var loadingUploadKycApi = false
    suspend fun uploadKYC() {
        val uploadKycApi = UploadKycApi()
        uploadKycApi.upLoadKYC(CameraKycPopup.imageFront, CameraKycPopup.imageBackSide, CameraKycPopup.imageFace, CameraKycPopup.video,CameraKycPopup.typeIdentify,
            onSuccess = {jsonObject->
                loadingUploadKycApi = false
                val Account = jsonObject.optJSONObject("Account")
                val KYC = Account.optJSONObject("KYC")
                val message = KYC.optString("message")
                val succeeded = KYC.optBoolean("succeeded")
                if(succeeded){
                    CameraKycPopup.imageBackSide = null
                    CameraKycPopup.imageFace = null
                    CameraKycPopup.imageFront = null
                    CameraKycPopup.video = null
                    if(PayMEOpenSDKPopup.isVisible){
                        if(CameraKycPopup.updateOnlyIdentify){
                            var even: EventBus = EventBus.getDefault()
                            var myEven: MyEven = MyEven(TypeCallBack.onUpdateIdentify, "")
                            even.post(myEven)
                            EventBus.getDefault().post(ChangeFragmentKYC.CLOSE)

                        }else{
                            var even: EventBus = EventBus.getDefault()
                            var myEven: MyEven = MyEven(TypeCallBack.onReload, "")
                            even.post(myEven)
                            EventBus.getDefault().post(ChangeFragmentKYC.CLOSE)
                        }

                    }else{
                        val payme = PayME()
                        payme.openWallet(PayME.fragmentManager,onSuccess = {
                                                                           
                        },onError = {jsonObject, i, s ->  
                            
                        })
                        EventBus.getDefault().post(ChangeFragmentKYC.CLOSE)

                    }

                }else{
                    parentFragmentManager.popBackStack()
                    PayME.showError(message)
                }
            },
            onError = { jsonObject, code, message ->
                loadingUploadKycApi = false
                parentFragmentManager.popBackStack()
                PayME.showError(message)

            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.payme_upload_kyc_fragment, container, false)
        layoutUpload = view.findViewById(R.id.upLoadKyc)
        layoutUpload!!.background = Store.config.colorApp.backgroundColor
        GlobalScope.launch(Dispatchers.Main) {
            if (!loadingUploadKycApi) {
                loadingUploadKycApi = true
                uploadKYC()
            }
        }

        return view


    }
}