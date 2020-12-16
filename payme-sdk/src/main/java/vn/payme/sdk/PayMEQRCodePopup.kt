package vn.payme.sdk

import android.app.Dialog
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.integration.android.IntentIntegrator
import vn.payme.sdk.component.Button

class PayMEQRCodePopup : BottomSheetDialogFragment() {
    private var btnSubmit: Button? = null

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
            R.layout.modal_qr_layout,
            container, false
        )
        btnSubmit = view.findViewById(R.id.buttonSubmit)
        btnSubmit?.setOnClickListener {
            IntentIntegrator(activity).apply {
                setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                captureActivity = AnyOrientationCaptureActivity::class.java
                setPrompt("")
                setCameraId(0)
                setRequestCode(5)
                setBeepEnabled(true)
                setOrientationLocked(false)
                initiateScan()
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
        val contentView = View.inflate(context, R.layout.modal_qr_layout, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
    }
}