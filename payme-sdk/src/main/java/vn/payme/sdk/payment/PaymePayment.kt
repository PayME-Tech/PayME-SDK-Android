package vn.payme.sdk.payment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.store.Store

internal class PaymePayment : DialogFragment() {

    lateinit var buttonClose: ConstraintLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.isCancelable = false
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheetDialogFragment: BottomSheetDialog = dialog as BottomSheetDialog
        val fragmentManager: FragmentManager
        val showResult = arguments?.getBoolean("showResult")
        val message = arguments?.getString("message")
        val state = arguments?.getString("state")
        Keyboard.closeKeyboard(requireContext())
        if (showResult == true) {
            val bundle: Bundle = Bundle()
            if (message != null) {
                bundle.putString("message", message)
                bundle.putString("state", state)
            }
            buttonClose.visibility = View.GONE
            val resultPaymentFragment: ResultPaymentFragment =
                ResultPaymentFragment()
            resultPaymentFragment.arguments = bundle
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(
                R.id.frame_container,
                resultPaymentFragment
            )
            fragment.commit()
        } else {
            fragmentManager = childFragmentManager
            val fragment = fragmentManager.beginTransaction()
            fragment.add(R.id.frame_container, SelectMethodFragment())
            fragment.commit()
        }
        bottomSheetDialogFragment.behavior.isDraggable = false
        dialog?.setCanceledOnTouchOutside(false)


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
        buttonClose = v.findViewById(R.id.buttonClose)
        buttonClose.setOnClickListener {
            this.dialog?.dismiss()
            PayME.onError(null, ERROR_CODE.USER_CANCELLED, "")
        }
        EventBus.getDefault().register(this)
        return v

    }

    @Subscribe
    fun onChangeFragment(event: ChangeFragmentPayment) {
        if (event.typeFragment == TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT_NOT_CALL_BACK) {
            this.dialog?.dismiss()
        } else if (event.typeFragment == TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT) {
            this.dialog?.dismiss()
            if (!Store.config.disableCallBackResult) {
                PayME.onError(null, ERROR_CODE.USER_CANCELLED, "")
            }
        } else if (event.typeFragment == TYPE_FRAGMENT_PAYMENT.RESULT) {
            buttonClose.visibility = View.GONE
            val message = event.value
            if (message != null) {
                if (!Store.config.disableCallBackResult) {
                    PayME.onError(null, ERROR_CODE.PAYMENT_ERROR, message!!)
                }
            } else {
                val data =
                    JSONObject("""{payment:{transaction:${Store.paymentInfo.transaction}}}""")
                if (!Store.config.disableCallBackResult) {
                    PayME.onSuccess(data)
                }
            }
            if (Store.paymentInfo.isShowResultUI) {
                val resultPaymentFragment = ResultPaymentFragment()
                val bundle = Bundle()
                bundle.putString("message", message)
                resultPaymentFragment.arguments = bundle
                val fragment = childFragmentManager?.beginTransaction()
                fragment?.replace(
                    R.id.frame_container,
                    resultPaymentFragment
                )
                fragment?.commit()
            } else {
                this.dialog?.dismiss()
            }


        } else if (event.typeFragment == TYPE_FRAGMENT_PAYMENT.CONFIRM_OTP) {
            val confirmFragment = ConfirmOtpFragment()
            val bundle = Bundle()
            bundle.putString("securityCode", event.value)
            confirmFragment.arguments
            val fragment = childFragmentManager?.beginTransaction()
            fragment?.replace(
                R.id.frame_container,
                confirmFragment
            )
            fragment?.commit()
        } else if (event.typeFragment == TYPE_FRAGMENT_PAYMENT.CONFIRM_NAPAS) {
            this.dialog?.dismiss()
            val bundle: Bundle = Bundle()
            bundle.putString("html", event.value)
            val web = WebViewNapasActivity()
            web.arguments = bundle
            fragmentManager?.let { web.show(it,null) }

        } else if (event.typeFragment == TYPE_FRAGMENT_PAYMENT.CONFIRM_PASS) {
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
        EventBus.getDefault().unregister(this);
        super.onDestroy()
    }

}