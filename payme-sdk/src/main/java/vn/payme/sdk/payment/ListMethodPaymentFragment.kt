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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.adapter.MethodAdapter
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.evenbus.ChangeTypePayment
import vn.payme.sdk.model.DataMethod
import vn.payme.sdk.model.Method
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
            PayME.balance = balance
            getListMethod()

        }, onError = { jsonObject, code, message ->
            disableLoading()

            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            println("onError=" + message)
        })
    }

    private fun paymentLinkedBank(method: Method) {
        val paymentApi = PaymentApi()
        showLoading()
        var even: EventBus = EventBus.getDefault()

        var myEven: ChangeTypePayment = ChangeTypePayment(TYPE_PAYMENT.PAYMENT_RESULT, "")
        println("method111111" + method.data?.linkedId)

        paymentApi.payment(method, null, null, null, null, null, null,
            onSuccess = { jsonObject ->
                disableLoading()
                val OpenEWallet = jsonObject.optJSONObject("OpenEWallet")
                val Payment = OpenEWallet.optJSONObject("Payment")
                val Pay = Payment.optJSONObject("Pay")
                val succeeded = Pay.optBoolean("succeeded")
                val payment = Pay.optJSONObject("payment")
                val message = Pay.optString("message")
                if (succeeded) {
                    println("THANH CONG")

                    even.post(myEven)
                } else {
                    println("payment" + payment)
                    if (payment != null) {
                        val statePaymentLinkedResponsed =
                            payment.optString("statePaymentLinkedResponsed")
                        println("statePaymentLinkedResponsed" + statePaymentLinkedResponsed)
                        if (statePaymentLinkedResponsed == "REQUIRED_VERIFY") {
                            val html = payment.optString("html")
                            var changeFragmentOtp: ChangeTypePayment =
                                ChangeTypePayment(TYPE_PAYMENT.CONFIRM_OTP_BANK_NAPAS, html)
                            even.post(changeFragmentOtp)
                        } else if (statePaymentLinkedResponsed == "REQUIRED_OTP") {
                                println("REQUIRED_OTP" + "1111111")
                                val transaction = payment.optString("transaction")
                                var changeFragmentOtp: ChangeTypePayment =
                                    ChangeTypePayment(TYPE_PAYMENT.CONFIRM_OTP_BANK, transaction)
                                even.post(changeFragmentOtp)
                            } else {
                                myEven.value = message
                                even.post(myEven)
                            }
                    } else {
                        println("THAT BAI ")
                        myEven.value = message
                        even.post(myEven)
                    }

                }
                loading = false

            },
            onError = { jsonObject, i, s ->
                disableLoading()
                loading = false
                PayME.showError(s)

            }
        )
    }

    private fun getListMethod() {
        val paymentApi = PaymentApi()
        this.showLoading()


        paymentApi.getTransferMethods(onSuccess = { jsonObject ->
            println("onSuccessFFFFFFFFFFFF" + jsonObject)
            disableLoading()

            val Utility = jsonObject.optJSONObject("Utility")
            val GetPaymentMethod = Utility.optJSONObject("GetPaymentMethod")
            val message = GetPaymentMethod.optString("message")
            val succeeded = GetPaymentMethod.optBoolean("succeeded")
            val methods = GetPaymentMethod.optJSONArray("methods")
            if (succeeded) {
                for (i in 0 until methods.length()) {
                    val jsonObject = methods.getJSONObject(i)
                    println("jsonObject11111111" + jsonObject)
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
                val toast: Toast =
                    Toast.makeText(PayME.context, message, Toast.LENGTH_SHORT)
                toast.view?.setBackgroundColor(
                    ContextCompat.getColor(
                        PayME.context,
                        R.color.scarlet
                    )
                )
                toast.show()
            }

        },
            onError = { jsonObject, code, message ->
                println("onError" + jsonObject)
                disableLoading()


                val toast: Toast =
                    Toast.makeText(PayME.context, message, Toast.LENGTH_SHORT)
                toast.view?.setBackgroundColor(
                    ContextCompat.getColor(
                        PayME.context,
                        R.color.scarlet
                    )
                )
                toast.show()
            }
        )
    }


    private fun onGotoResult(message: String) {
        this.disableLoading()
        val bundle: Bundle = Bundle()
        bundle.putString("message", message)
        val resultPaymentFragment: ResultPaymentFragment =
            ResultPaymentFragment()
        resultPaymentFragment.arguments = bundle
        val fragment = fragmentManager?.beginTransaction()
        fragment?.replace(R.id.frame_container, resultPaymentFragment)
        fragment?.commit()
    }

    fun setListViewHeightBasedOnChildren(listView: ListView) {
        val mAdapter: ListAdapter = listView.adapter
        var totalHeight = 0
        println("Adapter $mAdapter")
        for (i in 0 until mAdapter.getCount()) {
            val mView: View = mAdapter.getView(i, null, listView)
            println("M View $mView")
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
        val view: View? = inflater?.inflate(R.layout.list_method_payme_fragment, container, false)

        listView = view!!.findViewById(R.id.recipe_list_view)
        loadingProcess = view!!.findViewById(R.id.loadingListMethodPayment)
        loadingProcess.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(Color.parseColor(PayME.colorApp.startColor), PorterDuff.Mode.SRC_ATOP)
        methodAdapter = MethodAdapter(PayME.context, this.listMethod!!)
        this.listView.adapter = methodAdapter
        getBalance()



        this.listView.setOnItemClickListener { adapterView, view, i, l ->
            if (!loading) {
                val paymentApi = PaymentApi()
                val method = this.listMethod[i]
                PayME.methodSelected = method!!
                if (method.type == TYPE_PAYMENT.BANK_CARD) {
                    val fragment = fragmentManager?.beginTransaction()
                    fragment?.replace(R.id.frame_container_select_method, EnterAtmCardFragment())
                    fragment?.commit()


                } else if (method?.type == TYPE_PAYMENT.WALLET) {
                    println("PayME.balance"+PayME.balance)
                    println("PayMEayME.amount"+PayME.infoPayment?.amount)
                    if (PayME.balance < PayME.infoPayment?.amount!!) {
                       PayME.showError("Số dư trong ví không đủ")
                    } else {
                        var even: EventBus = EventBus.getDefault()
                        var myEven: ChangeTypePayment = ChangeTypePayment(TYPE_PAYMENT.WALLET, "")
                        even.post(myEven)
                    }

                } else if (method?.type == TYPE_PAYMENT.LINKED) {
                    paymentLinkedBank(method)
                } else {

                }
            }


        }
        return view
    }
}