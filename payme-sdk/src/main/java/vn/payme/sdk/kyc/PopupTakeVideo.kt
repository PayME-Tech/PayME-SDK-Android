package vn.payme.sdk.payment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.component.Button
import vn.payme.sdk.kyc.CameraKycActivity
import vn.payme.sdk.kyc.TakeVideoKycFragment

internal class PopupTakeVideo : BottomSheetDialogFragment() {

    private lateinit var buttonNext: Button
    private lateinit var buttonClose: ImageView

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
            R.layout.popup_take_video,
            container, false
        )
        buttonNext = view.findViewById(R.id.buttonNext)
        buttonClose = view.findViewById(R.id.buttonClose)
        buttonClose.setOnClickListener {
            dialog?.dismiss()
        }
        buttonNext.setOnClickListener {
            val openKycActivity = arguments?.getBoolean("openKycActivity")
            if (openKycActivity==true) {
                val intent = Intent(PayME.context, CameraKycActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                PayME.context?.startActivity(intent)
            } else {
                val newFragment = TakeVideoKycFragment()
                newFragment.arguments = arguments
                val fragment = activity?.supportFragmentManager?.beginTransaction()
                fragment?.replace(R.id.content_kyc, newFragment)
                fragment?.commit()
            }
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