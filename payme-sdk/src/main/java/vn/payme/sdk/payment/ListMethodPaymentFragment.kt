package vn.payme.sdk.payment

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.fragment.app.Fragment

import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.adapter.MethodAdapter
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.ChangeTypePayment
import vn.payme.sdk.model.DataMethod
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.model.Info
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store
import java.text.DecimalFormat

class ListMethodPaymentFragment : Fragment() {
    private var loading: Boolean = false
    private lateinit var listView: ListView
    private lateinit var loadingProcess: ProgressBar
    private lateinit var methodAdapter: MethodAdapter
    val listMethod: ArrayList<Method> = ArrayList<Method>()
    private fun showLoading() {
        loadingProcess.visibility = View.VISIBLE
    }
    private fun disableLoading() {
        loadingProcess.visibility = View.GONE
    }
    private fun checkFee(method: Method) {
        val paymentApi = PaymentApi()
        showLoading()
        paymentApi.getFee(
            Store.paymentInfo.infoPayment!!.amount,
            method,
            onSuccess = { jsonObject ->
                disableLoading()
                val Utility = jsonObject.getJSONObject("Utility")
                val GetFee = Utility.getJSONObject("GetFee")
                val succeeded = GetFee.getBoolean("succeeded")
                val message = GetFee.getString("message")
                if (succeeded) {
                    val feeObject = GetFee.getJSONObject("fee")
                    val fee = feeObject.getInt("fee")
                    val state = GetFee.getString("state")
                    if (state == "null") {
                        EventBus.getDefault().postSticky(PaymentInfoEvent(null,fee))
                        EventBus.getDefault().post(method)
                    } else {
                        PayME.showError(message)
                    }


                } else {
                    PayME.showError(message)
                }
            },
            onError = { jsonObject: JSONObject?, code: Int?, message: String ->
                disableLoading()
                PayME.showError(message)
            })
    }





    fun setListViewHeightBasedOnChildren(listView: ListView) {
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
                + listView.dividerHeight * (mAdapter.getCount() - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater?.inflate(R.layout.list_method_payme_fragment, container, false)
        Keyboard.closeKeyboard(requireContext())
        listView = view.findViewById(R.id.recipe_list_view)
        loadingProcess = view.findViewById(R.id.loadingListMethodPayment)
        loadingProcess.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )
        methodAdapter = MethodAdapter(PayME.context, Store.paymentInfo.listMethod)
        this.listView.adapter = methodAdapter
        setListViewHeightBasedOnChildren(listView)

        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_CARD) {
            val fragment = fragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, EnterAtmCardFragment())
            fragment?.commit()
        }

        this.listView.setOnItemClickListener { adapterView, view, i, l ->
            if (loadingProcess.visibility != View.VISIBLE) {
                val method = Store.paymentInfo.listMethod[i]
                Store.paymentInfo.methodSelected = method
                if(method.type!=TYPE_PAYMENT.WALLET && !Store.config.openPayAndKyc){
                    PayME.showError("Chức năng chỉ có thể thao tác môi trường production")
                    return@setOnItemClickListener
                }
                if(method.type == TYPE_PAYMENT.BANK_CARD || method?.type == TYPE_PAYMENT.LINKED){
                    checkFee(method)
                    return@setOnItemClickListener
                }
                if(method?.type == TYPE_PAYMENT.WALLET){
                    if (
                        ( !Store.userInfo.accountActive || !Store.userInfo.accountKycSuccess ||Store.paymentInfo.infoPayment!!.amount > Store.userInfo.balance)
                    ){
                        val paymeSDK = PayME()
                        if (!Store.userInfo.accountActive) {
                            paymeSDK.openWallet(PayME.onSuccess, PayME.onError)
                        } else if (!Store.userInfo.accountKycSuccess) {
                            paymeSDK.openKYC(PayME.fragmentManager,onSuccess = {},onError = { jsonObject: JSONObject?, i: Int?, message: String ->
                                PayME.showError(message)
                            })
                        } else if (Store.paymentInfo.infoPayment!!.amount > Store.userInfo.balance) {
                            paymeSDK.deposit(0, false,PayME.onSuccess, PayME.onError)
                        }
                        EventBus.getDefault().post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT,null))

                    }else{
                        checkFee(method)
                    }
                    return@setOnItemClickListener

                }

                PayME.showError("Phương thức chưa được hỗ trợ")





            }
        }
        return view
    }


}
