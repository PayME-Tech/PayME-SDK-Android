package vn.payme.sdk.payment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.Nullable
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
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.ChangeTypePayment
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.hepper.Keyboard

internal class PaymePayment : DialogFragment() {
    companion object{
        fun onPaymentSuccess(jsonObject: JSONObject?,context: Context,fragmentManager:FragmentManager){
            PayME.onSuccess(jsonObject)
            if(PayME.isShowResultUI){
                val resultPaymentFragment = ResultPaymentFragment()
                val fragment = fragmentManager?.beginTransaction()
                fragment?.replace(R.id.frame_container, resultPaymentFragment)
                fragment?.commit()
            }else{
                closePopup(context)
            }

        }
        fun onPaymentError(message:String,context: Context,fragmentManager:FragmentManager){
            PayME.onError(null, ERROR_CODE.PAYMENT_ERROR,message,)
            if(PayME.isShowResultUI){
                val bundle: Bundle = Bundle()
                bundle.putString("message", message)
                val resultPaymentFragment = ResultPaymentFragment()
                resultPaymentFragment.arguments = bundle
                val fragment = fragmentManager?.beginTransaction()
                fragment?.replace(R.id.frame_container, resultPaymentFragment)
                fragment?.commit()
            }else{
                closePopup(context)
            }
        }
        fun backPopup(fragmentManager:FragmentManager){
            val fragment = fragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container, SelectMethodFragment())
            fragment?.commit()
        }
        fun  closePopup(context: Context){
            Keyboard.closeKeyboard(context)
            var even: EventBus = EventBus.getDefault()
            var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
            even.post(myEven)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheetDialogFragment: BottomSheetDialog = dialog as BottomSheetDialog
        val fragmentManager: FragmentManager
        val showResult = arguments?.getBoolean("showResult")
        val message = arguments?.getString("message")
        if (showResult == true) {
            val bundle: Bundle = Bundle()
            if (message!=null) {
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
            fragment.commit()

        } else {
            if(PayME.methodSelected!==null){
                if(PayME.methodSelected?.type!=TYPE_PAYMENT.BANK_CARD){
                    fragmentManager = childFragmentManager
                    val fragment = fragmentManager.beginTransaction()
                    fragment.add(R.id.frame_container, ConfirmPassFragment())
                    fragment.commit()
                }else{
                    fragmentManager = childFragmentManager
                    val fragment = fragmentManager.beginTransaction()
                    fragment.add(R.id.frame_container, SelectMethodFragment())
                    fragment.commit()
                }
            }else{
                fragmentManager = childFragmentManager
                val fragment = fragmentManager.beginTransaction()
                fragment.add(R.id.frame_container, SelectMethodFragment())
                fragment.commit()
            }

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

        if (typePayment.type == TYPE_PAYMENT.CONFIRM_OTP_BANK_NAPAS) {
            this.dialog?.dismiss()
            val bundle: Bundle = Bundle()
            bundle.putString("html", typePayment.value)
            bundle.putString("history", typePayment.data.toString())
            val intent = Intent(PayME.context, WebViewNapasActivity::class.java)
            intent.putExtras(bundle)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            PayME.context?.startActivity(intent)


        } else if (typePayment.type == TYPE_PAYMENT.CONFIRM_OTP_BANK) {
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
            if (message?.length!! > 0) {
                onPaymentError(message,requireContext(),childFragmentManager)
            }else{
                onPaymentSuccess(typePayment.data,requireContext(),childFragmentManager)
            }
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
        EventBus.getDefault().unregister(this);
        super.onDestroy()
    }
}