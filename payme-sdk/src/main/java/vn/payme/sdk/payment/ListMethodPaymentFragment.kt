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
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.PaymentInfoEvent
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.model.BankInfo
import vn.payme.sdk.model.BankTransferInfo
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store

class ListMethodPaymentFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var loadingProcess: ProgressBar
    private lateinit var methodAdapter: MethodAdapter
    private  var loadingPopup =  SpinnerDialog()
    private fun showLoading() {
        loadingPopup.show(parentFragmentManager,null)
    }
    private fun disableLoading() {
        loadingPopup.dismiss()
    }
    fun getListBank (method: Method){
        val paymentApi = PaymentApi()
        paymentApi.getListBanks(onSuccess = { jsonObject ->
            if(!isVisible) return@getListBanks
            disableLoading()
            val Setting = jsonObject.optJSONObject("Setting")
            val banks = Setting.optJSONArray("banks")
            val listBanks = arrayListOf<BankInfo>()
            for (i in 0 until banks.length()) {
                val bank = banks.optJSONObject(i)
                val cardPrefix = bank.optString("cardPrefix")
                val depositable = bank.optBoolean("depositable")
                val cardNumberLength = bank.optInt("cardNumberLength")
                val shortName = bank.optString("shortName")
                val swiftCode = bank.optString("swiftCode")
                if (depositable) {
                    val bankInfo = BankInfo(
                        depositable,
                        cardPrefix,
                        cardNumberLength,
                        shortName,
                        swiftCode
                    )
                    listBanks.add(bankInfo)
                }

            }
            EventBus.getDefault().postSticky(listBanks)
            EventBus.getDefault().post(method)
        },
            onError = { jsonObject, code, message ->
                if(!isVisible) return@getListBanks
                PayME.showError(message)
            }
        )

    }
    private fun checkFee(method: Method) {
        val paymentApi = PaymentApi()
        showLoading()
        paymentApi.getFee(
            Store.paymentInfo.infoPayment!!.amount,
            method,
            onSuccess = { jsonObject ->
                if (!isVisible) return@getFee
                val Utility = jsonObject.getJSONObject("Utility")
                val GetFee = Utility.getJSONObject("GetFee")
                val succeeded = GetFee.getBoolean("succeeded")
                val message = GetFee.getString("message")
                if (succeeded) {
                    val feeObject = GetFee.getJSONObject("fee")
                    val fee = feeObject.getInt("fee")
                    val state = GetFee.getString("state")
                    if (state == "null") {
                        if(method.type == TYPE_PAYMENT.BANK_CARD){

                            EventBus.getDefault().postSticky(PaymentInfoEvent(null,fee))
                            getListBank(method)
                        }else if(method.type == TYPE_PAYMENT.BANK_TRANSFER){
                            EventBus.getDefault().postSticky(PaymentInfoEvent(null,fee))
                            val listBank = EventBus.getDefault().getStickyEvent(arrayListOf<BankTransferInfo>()::class.java)
                            if( listBank !=null && listBank.size>0){
                                disableLoading()
                                EventBus.getDefault().post(method)
                            }else{
                                getListBankTransfer(method)
                            }
                        }else{
                            disableLoading()
                            EventBus.getDefault().postSticky(PaymentInfoEvent(null,fee))
                            EventBus.getDefault().post(method)
                        }
                    } else {
                        disableLoading()
                        PayME.showError(message)
                    }

                } else {
                    disableLoading()
                    PayME.showError(message)
                }
            },
            onError = { jsonObject: JSONObject?, code: Int?, message: String ->
                if (!isVisible) return@getFee
                disableLoading()
                PayME.showError(message)
            })
    }

    private fun getListBankTransfer(method: Method) {
        val paymentApi = PaymentApi()
        Keyboard.closeKeyboard(requireContext())
        paymentApi.payment(
            method,
            "",
            null,
            "",
            "",
            false,
            onSuccess = { jsonObject ->
                if (!isVisible) return@payment
                disableLoading()
                val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                val Payment = OpenEWallet.optJSONObject("Payment")
                val Pay = Payment.optJSONObject("Pay")
                val succeeded = Pay.optBoolean("succeeded")
                val payment = Pay.optJSONObject("payment")
                val message = Pay.optString("message")
                if(succeeded){
                    val listBank = arrayListOf<BankTransferInfo>()
                    val bankList = payment.optJSONArray("bankList")
                    for (i in 0 until bankList.length()){
                        val bank = bankList.optJSONObject(i)
                        val bankAccountName = bank.optString("bankAccountName")
                        val bankAccountNumber = bank.optString("bankAccountNumber")
                        val bankBranch = bank.optString("bankBranch")
                        val bankCity = bank.optString("bankCity")
                        val bankName = bank.optString("bankName")
                        val content = bank.optString("content")
                        val swiftCode = bank.optString("swiftCode")
                        val bankTransferInfo = BankTransferInfo(bankAccountName,bankAccountNumber,bankBranch,bankCity,bankName,content,swiftCode)
                        listBank.add(bankTransferInfo)
                    }
                    EventBus.getDefault().postSticky(listBank[0])
                    EventBus.getDefault().postSticky(listBank)
                    EventBus.getDefault().post(method)
                }else{
                    PayME.showError(message)
                }
            },
            onError = { jsonObject, code, message ->
                if (!isVisible) return@payment
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
        val view: View = inflater?.inflate(R.layout.payment_list_method_payme_fragment, container, false)
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
                if(method.type == TYPE_PAYMENT.CREDIT_CARD ||
                    method.type == TYPE_PAYMENT.BANK_CARD ||
                    method?.type == TYPE_PAYMENT.LINKED ||
                    method?.type == TYPE_PAYMENT.BANK_TRANSFER
                ){
                    checkFee(method)
                    return@setOnItemClickListener
                }
                if(method?.type == TYPE_PAYMENT.WALLET){
                    if (
                        ( !Store.userInfo.accountActive || !Store.userInfo.accountKycSuccess ||Store.paymentInfo.infoPayment!!.amount > Store.userInfo.balance)
                    ){
                        val paymeSDK = PayME()
                        if (!Store.userInfo.accountActive) {
                            paymeSDK.openWallet(PayME.fragmentManager,PayME.onSuccess, PayME.onError)
                        } else if (!Store.userInfo.accountKycSuccess) {
                            paymeSDK.openKYC(PayME.fragmentManager,onSuccess = {},onError = { jsonObject: JSONObject?, i: Int?, message: String ->
                                PayME.showError(message)
                            })
                        } else if (Store.paymentInfo.infoPayment!!.amount > Store.userInfo.balance) {
                            paymeSDK.deposit(PayME.fragmentManager,0, false,PayME.onSuccess, PayME.onError)
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
