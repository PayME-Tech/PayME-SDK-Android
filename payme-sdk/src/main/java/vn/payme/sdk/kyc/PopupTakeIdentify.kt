package vn.payme.sdk.payment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.SimpleLottieValueCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.kyc.CameraKycActivity
import vn.payme.sdk.store.Store

internal class PopupTakeIdentify : BottomSheetDialogFragment() {

    private lateinit var buttonNext: Button
    private lateinit var buttonClose: ImageView
    private lateinit var textNote: TextView
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
            R.layout.popup_take_identify,
            container, false
        )
        buttonNext = view.findViewById(R.id.buttonNext)
        buttonClose = view.findViewById(R.id.buttonClose)
        textNote = view.findViewById(R.id.txtNote)
        lottie = view.findViewById(R.id.animation_view)

        textNote.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
        textNote.background = Store.config.colorApp.backgroundColorRadiusAlpha
        loadAnimation()
        buttonClose.setOnClickListener {
            dialog?.dismiss()
        }
        buttonNext.setOnClickListener {
            val cameraKycActivity = CameraKycActivity()
            cameraKycActivity.show(PayME.fragmentManager,null)
            dismiss()
        }



        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        return dialog
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, R.layout.payment_layout, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
    }
    fun loadAnimation (){
//        lottie.addValueCallback<ColorFilter>(
//            KeyPath("Export_ChupGTTT", "**"),
//            LottieProperty.COLOR_FILTER,
//            SimpleLottieValueCallback<ColorFilter?> {
//                PorterDuffColorFilter(
//                    Color.parseColor(Store.config.colorApp.startColor),
//                    PorterDuff.Mode.SRC_ATOP
//                )
//            }
//        )

//        addContent("1")
//        addContent("2")
//        addContent("14")
        addColorContent("CMNN Outlines","Group 1")
        addColorContent("CMNN Outlines","Group 2")
        addColorContent("CMNN Outlines","Group 14")
        addColor("Chup_xanh")
        addColor("Focus_xanh")
        addColor("Bo_xanh")
        addColor("CMNN_xanh")
        addColor("CMNN_2_xanh")

        lottie.playAnimation()


    }
    fun  addColor(value:String){
        lottie.addValueCallback<ColorFilter>(
            KeyPath(value,"**"),
            LottieProperty.COLOR_FILTER,
            SimpleLottieValueCallback<ColorFilter?> {
                PorterDuffColorFilter(
                    Color.parseColor(Store.config.colorApp.startColor),
                    PorterDuff.Mode.SRC_IN
                )
            }
        )
    }
    fun  addColorContent(value:String,content:String){
        lottie.addValueCallback<ColorFilter>(
            KeyPath(value,content,"**"),
            LottieProperty.COLOR_FILTER,
            SimpleLottieValueCallback<ColorFilter?> {
                PorterDuffColorFilter(
                    Color.parseColor(Store.config.colorApp.startColor),
                    PorterDuff.Mode.SRC_IN
                )
            }
        )
    }

}