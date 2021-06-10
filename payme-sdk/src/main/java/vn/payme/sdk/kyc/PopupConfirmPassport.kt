package vn.payme.sdk.payment

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.devs.vectorchildfinder.VectorChildFinder
import com.devs.vectorchildfinder.VectorDrawableCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.model.TypeIdentify
import vn.payme.sdk.store.Store

internal class PopupConfirmPassport : BottomSheetDialogFragment() {

    private lateinit var buttonNext: Button
    private lateinit var buttonClose: Button
    private lateinit var imagePassport: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(
            R.layout.pupup_comfirm_passport,
            container, false
        )
        buttonNext = view.findViewById(R.id.buttonNext)
        buttonClose = view.findViewById(R.id.buttonBack)
        imagePassport = view.findViewById(R.id.imagePassport)

        ChangeColorImage().changeColor(requireContext(),imagePassport,R.drawable.ic_passpost,3)
        buttonClose.setOnClickListener {
            dialog?.dismiss()
            fragmentManager?.let { PopupSelectTypeIdentify().show(it, "ModalBottomSheet") }

        }
        buttonNext.setOnClickListener {
         val typeIdentify=   TypeIdentify(
                "Hộ chiếu",
                "PASSPORT",
                false
            )
            var even: EventBus = EventBus.getDefault()
            even.post(typeIdentify)
            dialog?.dismiss()


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

}