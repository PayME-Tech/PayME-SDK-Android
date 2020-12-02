package vn.payme.sdk.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.adapter.MethodAdapter
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.model.Method
import vn.payme.sdk.model.MyEven
import vn.payme.sdk.model.TypeCallBack
import java.text.DecimalFormat

class SelectMethodFragment : Fragment() {
    private var methodSelected: Int = 0
    private lateinit var listView: ListView
    private lateinit var buttonSubmit: Button
    private lateinit var buttonClose: ImageView
    private lateinit var textAmount: TextView
    private lateinit var textNote: TextView
    private lateinit var layout: ConstraintLayout
    private var loading: Boolean = false
    val listMethod: ArrayList<Method> = ArrayList<Method>()
    fun showLoading() {
        loading = true
        buttonSubmit.enableLoading()
    }

    fun disableLoading() {
        loading = false
        buttonSubmit.disableLoading()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater?.inflate(R.layout.select_method_layout, container, false)

        listView = view!!.findViewById(R.id.recipe_list_view)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        buttonClose = view.findViewById(R.id.buttonClose)
        textAmount = view.findViewById(R.id.money)
        textNote = view.findViewById(R.id.note)
        layout = view.findViewById(R.id.content)
        textNote.text = PayME.content


        val decimal = DecimalFormat("#,###")
        textAmount.text = "${decimal.format(PayME.amount)} đ"
        val paymentApi = PaymentApi()

        val methodAdapter: MethodAdapter = MethodAdapter(PayME.context, this.listMethod!!)
        this.listView.adapter = methodAdapter
        this.showLoading()
        paymentApi.getTransferMethods(onSuccess = { jsonObject ->
            this.disableLoading()
            val jsonArray = jsonObject.optJSONArray("items")
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                var linkedId = jsonObject.optInt("linkedId")
                val detail = jsonObject.optString("detail")
                val bankCode = jsonObject.optString("bankCode")
                val amount = jsonObject.optInt("amount")
                val type = jsonObject.optString("type")
                val cardNumber = jsonObject.optString("cardNumber")
                val swiftCode = jsonObject.optString("swiftCode")
                this.listMethod.add(
                    Method(
                        detail,
                        linkedId,
                        bankCode,
                        amount,
                        type,
                        cardNumber,
                        swiftCode,
                        selected = i === 0
                    )
                )
                methodAdapter.notifyDataSetChanged()
            }
        },
            onError = { jsonObject, code, message ->
                this.disableLoading()
                val toast: Toast =
                    Toast.makeText(PayME.context, message, Toast.LENGTH_SHORT)
                toast.view.setBackgroundColor(
                    ContextCompat.getColor(
                        PayME.context,
                        R.color.scarlet
                    )
                )
                toast.show()
            }
        )




        this.listView.setOnItemClickListener { adapterView, view, i, l ->
            if (!this.listMethod[i].selected!! && !loading) {
                this.listMethod[i].selected = true
                this.listMethod[methodSelected].selected = false
                this.methodSelected = i

                methodAdapter.notifyDataSetChanged()
            }

        }


        layout.background = PayME.colorApp.backgroundColor

        buttonSubmit.setOnClickListener {

                if (!loading && this.listMethod.size>0) {
                val paymentApi = PaymentApi()
                this.showLoading()

                val method = this.listMethod[this.methodSelected]
                if (method?.type == TYPE_PAYMENT.APP_WALLET) {
                    if(method.amount?.toInt()!! < PayME.amount){
                        this.disableLoading()
                        val toast: Toast =
                            Toast.makeText(PayME.context, "Số dư trong ví không đủ", Toast.LENGTH_SHORT)
                        toast.view.setBackgroundColor(
                            ContextCompat.getColor(
                                PayME.context,
                                R.color.scarlet
                            )
                        )
                        toast.show()

                    }else{
                        paymentApi.postTransferAppWallet(onSuccess = { jsonObject ->

                            val fragment = fragmentManager?.beginTransaction()
                            fragment?.replace(R.id.frame_container, ResultPaymentFragment())
                            fragment?.commit()
                            this.disableLoading()
                        },
                            onError = { jsonObject, code, message ->
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
                        )
                    }

                } else if (method?.type == TYPE_PAYMENT.NAPAS) {
                    paymentApi.postTransferNapas(method, onSuccess = { jsonObject ->
                        this.disableLoading()
                        val form = jsonObject.getString("form")
                        val bundle: Bundle = Bundle()
                        bundle.putString("form", form)
                        val confirmOtpNapasFragment: ConfirmOtpNapasFragment =
                            ConfirmOtpNapasFragment()
                        confirmOtpNapasFragment.arguments = bundle
                        val fragment = fragmentManager?.beginTransaction()
                        fragment?.replace(R.id.frame_container, confirmOtpNapasFragment)
                        fragment?.commit()

                    },
                        onError = { jsonObject, code, message ->
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
                    )

                } else if (method?.type == TYPE_PAYMENT.PVCB) {
                    paymentApi.postTransferPVCB(method, onSuccess = { jsonObject ->
                        this.disableLoading()
                        val fragment = fragmentManager?.beginTransaction()
                        fragment?.replace(R.id.frame_container, ResultPaymentFragment())
                        fragment?.commit()

                    },
                        onError = { jsonObject, code, message ->
                            this.disableLoading()
                            if (code == 1004) {
                                val bundle: Bundle = Bundle()
                                val transferId = jsonObject?.getString("transferId")
                                bundle.putString("transferId", transferId)
                                val confirmOtpFragment: ConfirmOtpFragment = ConfirmOtpFragment()
                                confirmOtpFragment.arguments = bundle
                                val fragment = fragmentManager?.beginTransaction()
                                fragment?.replace(R.id.frame_container, confirmOtpFragment)
                                fragment?.commit()

                            } else {
                                val bundle: Bundle = Bundle()
                                bundle.putString("message", message)
                                val resultPaymentFragment: ResultPaymentFragment =
                                    ResultPaymentFragment()
                                resultPaymentFragment.arguments = bundle
                                val fragment = fragmentManager?.beginTransaction()
                                fragment?.replace(R.id.frame_container, resultPaymentFragment)
                                fragment?.commit()
                            }


                        }
                    )

                } else {
                    this.disableLoading()

                }
            }


        }
        buttonClose.setOnClickListener {
            if (!loading) {
                var even: EventBus = EventBus.getDefault()
                var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
                even.post(myEven)
            }


        }
        return view
    }


}