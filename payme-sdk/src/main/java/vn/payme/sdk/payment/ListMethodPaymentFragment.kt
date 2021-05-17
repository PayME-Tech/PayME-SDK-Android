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

    private fun getBalance() {
        val paymentApi = PaymentApi()
        paymentApi.getBalance(onSuccess = { jsonObject ->
            val walletBalance = jsonObject.optJSONObject("Wallet")
            val balance = walletBalance.optInt("balance")
            Store.userInfo.balance = balance
            getListMethod()

        }, onError = { jsonObject, code, message ->
            if (code == ERROR_CODE.EXPIRED) {
                PayME.onExpired()
                PayME.onError(jsonObject, code, message)

            } else {
                disableLoading()
                loading = false
                PayME.showError(message)
            }
        })
    }


    private fun getListMethod() {
        val paymentApi = PaymentApi()
        this.showLoading()


        paymentApi.getTransferMethods(onSuccess = { jsonObject ->
            disableLoading()

            val Utility = jsonObject.optJSONObject("Utility")
            val GetPaymentMethod = Utility.optJSONObject("GetPaymentMethod")
            val message = GetPaymentMethod.optString("message")
            val succeeded = GetPaymentMethod.optBoolean("succeeded")
            val methods = GetPaymentMethod.optJSONArray("methods")
            if (succeeded) {
                for (i in 0 until methods.length()) {
                    val jsonObject = methods.getJSONObject(i)
                    var data = jsonObject.optJSONObject("data")
                    var dataMethod = DataMethod(null, "")
                    if (data != null) {
                        val linkedId = data.optString("linkedId")
                        val swiftCode = data.optString("swiftCode")
                        dataMethod = DataMethod(linkedId, swiftCode)

                    }

                    var fee = jsonObject.optInt("fee")
                    var label = jsonObject.optString("label")
                    var methodId = jsonObject.optInt("methodId")
                    var minFee = jsonObject.optInt("minFee")
                    var title = jsonObject.optString("title")
                    var type = jsonObject.optString("type")


                    this.listMethod.add(
                        Method(
                            dataMethod,
                            fee,
                            label,
                            methodId,
                            minFee,
                            title,
                            type,
                        )
                    )
                }
                methodAdapter.notifyDataSetChanged()
                setListViewHeightBasedOnChildren(listView)

            } else {
                PayME.showError(message)
            }

        },
            onError = { jsonObject, code, message ->
                disableLoading()

                if (code == ERROR_CODE.EXPIRED) {
                    PayME.onExpired()
                    PayME.onError(jsonObject, code, message)

                } else {
                    PayME.showError(message)
                }


            }
        )
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
        context?.let { Keyboard.closeKeyboard(it) }
        listView = view.findViewById(R.id.recipe_list_view)
        loadingProcess = view.findViewById(R.id.loadingListMethodPayment)
        loadingProcess.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )
        methodAdapter = MethodAdapter(PayME.context, this.listMethod!!)
        this.listView.adapter = methodAdapter
        if (Store.userInfo.accountActive && Store.userInfo.accountKycSuccess) {
            getBalance()
        } else {
            getListMethod()
        }

        if (Store.paymentInfo.methodSelected?.type == TYPE_PAYMENT.BANK_CARD) {
            val fragment = fragmentManager?.beginTransaction()
            fragment?.replace(R.id.frame_container_select_method, EnterAtmCardFragment())
            fragment?.commit()
        }


        this.listView.setOnItemClickListener { adapterView, view, i, l ->
            if (loadingProcess.visibility != View.VISIBLE) {
                val method = this.listMethod[i]
                Store.paymentInfo.methodSelected = method!!
                if(method?.type != TYPE_PAYMENT.WALLET){
                    if(!Store.config.openPayAndKyc){
                        PayME.showError("Chức năng chỉ có thể thao tác môi trường production")
                    }else if(method.type == TYPE_PAYMENT.BANK_CARD){
                        val fragment = fragmentManager?.beginTransaction()
                        val enterAtmCardFragment = EnterAtmCardFragment()
                        val bundle = Bundle()
                        bundle.putBoolean("showChangeMethod", true)
                        enterAtmCardFragment.arguments = bundle
                        fragment?.replace(R.id.frame_container_select_method, enterAtmCardFragment)
                        fragment?.commit()
                    }else{
                        checkFree()
                    }
                }else{
                    checkFree()
                }
            }
        }
        return view
    }

    fun checkFree() {
        var listInfoTop = arrayListOf<Info>()
        var listInfoBottom = arrayListOf<Info>()
        val decimal = DecimalFormat("#,###")
        val method = Store.paymentInfo.methodSelected
        listInfoTop.add(Info("Dịch vụ", "Tên Merchant", null, null, false))
        listInfoTop.add(
            Info(
                "Số tiền thanh toán",
                "${decimal.format(Store.paymentInfo.infoPayment?.amount)} đ",
                null,
                Color.parseColor(Store.config.colorApp.startColor),
                false
            )
        )
        listInfoTop.add(Info("Nội dung", Store.paymentInfo.infoPayment?.note, null, null, true))

        if (method?.type == TYPE_PAYMENT.LINKED) {
            listInfoBottom.add(Info("Phương thức", "Tài khoản liên kết", null, null, false))
            listInfoBottom.add(
                Info(
                    "Số tài khoản",
                    Store.paymentInfo.methodSelected?.title + Store.paymentInfo.methodSelected?.label,
                    null,
                    null,
                    false
                )
            )
        } else {
            listInfoBottom.add(
                Info(
                    "Phương thức",
                    Store.paymentInfo.methodSelected?.title,
                    null,
                    null,
                    false
                )
            )
        }
        val paymentApi = PaymentApi()
        loadingProcess.visibility = View.VISIBLE
        paymentApi.getFee(Store.paymentInfo.amount, onSuccess = { jsonObject ->
            val Utility = jsonObject.getJSONObject("Utility")
            val GetFee = Utility.getJSONObject("GetFee")
            val succeeded = GetFee.getBoolean("succeeded")
            val message = GetFee.getString("message")
            val decimal = DecimalFormat("#,###")
            if (succeeded) {
                val feeObject = GetFee.getJSONObject("fee")
                val fee = feeObject.getInt("fee")
                listInfoBottom.add(Info("Phí", "${decimal.format(fee)} đ", null, null, false))
                listInfoBottom.add(
                    Info(
                        "Tổng thanh toán", "${
                            decimal.format(
                                Store.paymentInfo.infoPayment?.amount?.plus(
                                    fee
                                )
                            )
                        } đ", null, resources.getColor(R.color.red), true
                    )
                )
                EventBus.getDefault()
                    .postSticky(PaymentInfoEvent(listInfoTop, listInfoBottom, null))
                EventBus.getDefault().post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CONFIRM_PAYMENT,null))

            } else {
                PayME.showError(message)
            }
        }, onError = { jsonObject: JSONObject?, code: Int?, message: String ->
            loadingProcess.visibility = View.GONE
            PayME.showError(message)
        })
    }
}
