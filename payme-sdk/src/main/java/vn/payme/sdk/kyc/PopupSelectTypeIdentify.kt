package vn.payme.sdk.payment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView


import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.adapter.TypeIndentifyAdapter
import vn.payme.sdk.kyc.CameraKycPopup
import vn.payme.sdk.model.TypeIdentify

internal class PopupSelectTypeIdentify : BottomSheetDialogFragment() {

    private var methodSelected: Int = 0
    private lateinit var listView: ListView
    private lateinit var buttonClose: ImageView
    val listMethod: ArrayList<TypeIdentify> = ArrayList<TypeIdentify>()

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
            R.layout.payme_popup_select_type_indentify,
            container, false
        )
        listView = view.findViewById(R.id.recipe_list_type_identify)
        buttonClose = view.findViewById(R.id.buttonClose)
        val methodAdapter: TypeIndentifyAdapter =
            TypeIndentifyAdapter(PayME.context, this.listMethod!!)
        listView.adapter = methodAdapter

        this.listMethod.add(
            TypeIdentify(
                "Chứng minh nhân dân",
                "CMND",
                 CameraKycPopup.typeIdentify == "CMND"
            )
        )
        this.listMethod.add(
            TypeIdentify(
                "Căn cước công dân",
                "CCCD",
                CameraKycPopup.typeIdentify == "CCCD"
            )
        )
        this.listMethod.add(
            TypeIdentify(
                "Hộ chiếu",
                "PASSPORT",
                CameraKycPopup.typeIdentify == "PASSPORT"
            )
        )
        methodAdapter.notifyDataSetChanged()
        listView.setOnItemClickListener { adapterView, view, i, l ->
            this.listMethod[i].selected = true
            this.listMethod[methodSelected].selected = false
            this.methodSelected = i
            methodAdapter.notifyDataSetChanged()
            var typeIdentify = this.listMethod[i]
            if (typeIdentify.type == "PASSPORT") {
                fragmentManager?.let { PopupConfirmPassport().show(it, "ModalBottomSheet") }
                this.dialog?.dismiss()

            } else {
                var even: EventBus = EventBus.getDefault()
                even.post(typeIdentify)
                this.dialog?.dismiss()
            }

        }

        buttonClose.setOnClickListener {
            this.dialog?.dismiss()
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
        val contentView = View.inflate(context, R.layout.payme_payment_layout, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
    }

}