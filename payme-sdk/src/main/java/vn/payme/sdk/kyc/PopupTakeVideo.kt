package vn.payme.sdk.payment

import android.app.Dialog
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.SimpleLottieValueCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.evenbus.ChangeFragmentKYC
import vn.payme.sdk.kyc.CameraKycPopup
import vn.payme.sdk.store.Store

internal class PopupTakeVideo : BottomSheetDialogFragment() {

    private lateinit var buttonNext: Button
    private lateinit var buttonClose: ImageView
    private lateinit var lottie: LottieAnimationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(
            R.layout.payme_popup_take_video,
            container, false
        )
        buttonNext = view.findViewById(R.id.buttonNext)
        buttonClose = view.findViewById(R.id.buttonClose)
        lottie = view.findViewById(R.id.animation_view)

        loadAnimation()
        buttonClose.setOnClickListener {
            dialog?.dismiss()
        }
        buttonNext.setOnClickListener {
            val openKycActivity = arguments?.getBoolean("openKycActivity")
            if (openKycActivity==true) {
                val cameraKycActivity = CameraKycPopup()
                cameraKycActivity.show(PayME.fragmentManager,null)
            } else {
                EventBus.getDefault().post(ChangeFragmentKYC.KYC_VIDEO)
            }
            dialog?.dismiss()

        }


        return view
    }
    fun loadAnimation (){
        lottie.addValueCallback<ColorFilter>(
            KeyPath("Focus", "**"),
            LottieProperty.COLOR_FILTER,
            SimpleLottieValueCallback<ColorFilter?> {
                PorterDuffColorFilter(
                    Color.parseColor(Store.config.colorApp.startColor),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        )
        lottie.addValueCallback<ColorFilter>(
            KeyPath("CMNN_bg","Group 6", "**"),
            LottieProperty.COLOR_FILTER,
            SimpleLottieValueCallback<ColorFilter?> {
                PorterDuffColorFilter(
                    Color.parseColor(Store.config.colorApp.startColor),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        )
        lottie.playAnimation()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        return dialog
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, R.layout.payme_payment_layout, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

}