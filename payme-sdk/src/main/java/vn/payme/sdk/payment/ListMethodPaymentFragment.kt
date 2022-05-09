package vn.payme.sdk.payment

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment

import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.adapter.MethodAdapter
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.*
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store

class ListMethodPaymentFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var loadingProcess: ProgressBar
    private lateinit var methodAdapter: MethodAdapter
    private var loadingPopup = SpinnerDialog()
    private fun showLoading() {
        loadingPopup.show(parentFragmentManager, null)
    }

    private fun disableLoading() {
        loadingPopup.dismiss()
    }
    companion object{
        var isVisible = false
    }

    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        val mAdapter: ListAdapter = listView.adapter
        var totalHeight = 0
        for (i in 0 until mAdapter.getCount()) {
            val mView: View = mAdapter.getView(i, null, listView)
            mView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            totalHeight += mView.measuredHeight
        }
        val params = listView.layoutParams
        params.height = (totalHeight
                + listView.dividerHeight * (mAdapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            inflater?.inflate(R.layout.payme_payment_list_method_payme_fragment, container, false)
        Keyboard.closeKeyboard(requireContext())
        listView = view.findViewById(R.id.recipe_list_view)
        loadingProcess = view.findViewById(R.id.loadingListMethodPayment)
        ListMethodPaymentFragment.isVisible = true

        loadingProcess.getIndeterminateDrawable().mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )
        methodAdapter = MethodAdapter(PayME.context, Store.paymentInfo.listMethod)
        this.listView.adapter = methodAdapter
        setListViewHeightBasedOnChildren(listView)

        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_CARD) {
            PayME.fragmentManager = childFragmentManager
            val fragment = fragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, EnterAtmCardFragment(), "inputFragment")
            fragment?.commit()
        }

        this.listView.setOnItemClickListener { _, view, i, _ ->
            if (!loadingPopup.isVisible) {
                val method = Store.paymentInfo.listMethod[i]
                if (method?.type == TYPE_PAYMENT.WALLET ) {
                    val total = Store.paymentInfo.infoPayment!!.amount

                    val feeInfo = EventBus.getDefault().getStickyEvent(FeeInfo::class.java)
                    if (
                        (!Store.userInfo.accountActive || !Store.userInfo.accountKycSuccess ||total > Store.userInfo.balance )
                    ) {
                        val paymeSDK = PayME()
                        if (!Store.userInfo.accountActive) {
                            paymeSDK.openWallet(
                                PayME.fragmentManager,
                                PayME.onSuccess,
                                PayME.onError
                            )
                        } else if (!Store.userInfo.accountKycSuccess) {
                            paymeSDK.openKYC(
                                PayME.fragmentManager,
                                onSuccess = {},
                                onError = { _: JSONObject?, _: Int, message: String? ->
                                    PayME.showError(message)
                                })
                        } else if (total > Store.userInfo.balance) {
                            paymeSDK.deposit(
                                PayME.fragmentManager,
                                0,
                                false,
                                PayME.onSuccess,
                                PayME.onError
                            )
                        }
                        EventBus.getDefault()
                            .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT, null))

                    }else{
                        if( method.type != Store.paymentInfo.methodSelected!!.type){
                            getFee(method)
                        }
                    }
                }else{
                    if(method.methodId != Store.paymentInfo.methodSelected!!.methodId || method.type != Store.paymentInfo.methodSelected!!.type){
                        getFee(method)
                    }
                }
            }
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        ListMethodPaymentFragment.isVisible = false

    }
    private fun getFee(method:Method){
        val payFunction = PayFunction()
        showLoading()
        payFunction.checkFee(Store.paymentInfo.infoPayment!!,method,onSuccess = {
            if (!isVisible) return@checkFee
            disableLoading()
            Store.paymentInfo.methodSelected  = method
            EventBus.getDefault().post(method)
            methodAdapter.notifyDataSetChanged()

        },onError = { _, _, s ->
            if (!isVisible) return@checkFee
            disableLoading()
            PayME.showError(s)

        })
    }



}
