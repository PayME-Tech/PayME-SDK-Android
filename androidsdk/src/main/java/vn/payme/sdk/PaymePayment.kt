package vn.payme.sdk

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import androidx.annotation.Nullable
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.model.MyEven
import vn.payme.sdk.model.TypeCallBack
import vn.payme.sdk.payment.SelectMethodFragment
internal class PaymePayment : BottomSheetDialogFragment() {

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val bottomSheetDialogFragment : BottomSheetDialog = dialog as BottomSheetDialog
        val fragmentManager : FragmentManager
        fragmentManager = childFragmentManager
        val  fragment = fragmentManager.beginTransaction()
        fragment.add(R.id.frame_container,SelectMethodFragment())
        fragment.commit()
        bottomSheetDialogFragment.behavior.isDraggable = false
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(
                R.layout.payment_layout,
                container, false
        )
        EventBus.getDefault().register(this)
        return v

    }
    @Subscribe
    fun onText(myEven: MyEven){
        if(myEven.type=== TypeCallBack.onClose){
            this.dialog?.dismiss()
        }
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

    override fun onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy()
    }


}