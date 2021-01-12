package vn.payme.sdk.payment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.evenbus.ChangeTypePayment
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.TypeCallBack

internal class PaymePayment : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheetDialogFragment: BottomSheetDialog = dialog as BottomSheetDialog
        val fragmentManager: FragmentManager
        fragmentManager = childFragmentManager
        val fragment = fragmentManager.beginTransaction()
        fragment.add(R.id.frame_container, SelectMethodFragment())
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
    fun onClose(myEven: MyEven) {
        if (myEven.type === TypeCallBack.onClose) {
            this.dialog?.dismiss()
        }
    }

    @Subscribe
    fun onChangeFragment(typePayment: ChangeTypePayment) {
        println("CHNAG IOTPP" + typePayment.type)
        if (typePayment.type == TYPE_PAYMENT.CONFIRM_OTP_BANK_NAPAS) {
            println("CHNAG IOTPP")
            val confirmOtpFragment = ConfirmOtpNapasFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("html", typePayment.value)
            val fragment = childFragmentManager?.beginTransaction()
            confirmOtpFragment.arguments = bundle
            fragment?.replace(
                R.id.frame_container,
                confirmOtpFragment
            )
            fragment?.commit()

        } else if (typePayment.type == TYPE_PAYMENT.CONFIRM_OTP_BANK) {
            println("CHNAG IOTPP")
            val confirmOtpFragment: ConfirmOtpFragment =
                ConfirmOtpFragment()
            val bundle: Bundle = Bundle()
            bundle.putString("transaction", typePayment.value)
            val fragment = childFragmentManager?.beginTransaction()
            confirmOtpFragment.arguments = bundle
            fragment?.replace(
                R.id.frame_container,
                confirmOtpFragment
            )
            fragment?.commit()

        } else if (typePayment.type == TYPE_PAYMENT.PAYMENT_RESULT) {
            val message = typePayment.value
            val bundle: Bundle = Bundle()
            if (message?.length!! > 0) {
                bundle.putString("message", message)
            }
            val resultPaymentFragment: ResultPaymentFragment =
                ResultPaymentFragment()
            resultPaymentFragment.arguments = bundle
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(
                R.id.frame_container,
                resultPaymentFragment
            )
            fragment?.commit()
        } else if (typePayment.type == TYPE_PAYMENT.WALLET) {
            val confirmPassFragment: ConfirmPassFragment = ConfirmPassFragment()
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container, confirmPassFragment)
            fragment?.commit()
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

        PayME.onClose()
        EventBus.getDefault().unregister(this);
        super.onDestroy()
    }
}