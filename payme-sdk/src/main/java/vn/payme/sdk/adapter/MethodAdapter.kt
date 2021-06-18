package vn.payme.sdk.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import vn.payme.sdk.R
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.hepper.AddInfoMethod
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store

class MethodAdapter(
    private val context: Context,
    private val dataSource: ArrayList<Method>
) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    //2
    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }





    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val method = dataSource[position]
        Log.d("LOGIN","getView:"+Store.userInfo.accountKycSuccess)

        if (
            method.type == TYPE_PAYMENT.WALLET
            && ( !Store.userInfo.accountActive || !Store.userInfo.accountKycSuccess ||Store.paymentInfo.infoPayment!!.amount > Store.userInfo.balance)
        ) {
            val rowView = inflater.inflate(R.layout.item_method_wallet_not_kyc, null, true)
            val titleText = rowView.findViewById(R.id.title) as TextView
            val noteMethod = rowView.findViewById(R.id.note_method) as TextView
            val txtButton = rowView.findViewById(R.id.txtButton) as TextView
            val txtDescription = rowView.findViewById(R.id.txtDescription) as TextView
            val button = rowView.findViewById(R.id.button) as ConstraintLayout
            button.background = Store.config.colorApp.backgroundColorRadiusBorder
            txtButton.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
            if (!Store.userInfo.accountActive) {
                txtButton.setText(R.string.active_now)
                txtDescription.setText(R.string.description_active_now)
            } else if (!Store.userInfo.accountKycSuccess) {
                txtButton.setText(R.string.kyc_now)
                txtDescription.setText(R.string.description_kyc_now)
            } else if (Store.paymentInfo.infoPayment!!.amount > Store.userInfo.balance) {
                txtButton.setText(R.string.deposit_now)
                txtDescription.setText(R.string.description_deposit_now)
            }
           AddInfoMethod().setTitle(method, titleText, noteMethod,null)
            return rowView

        } else {
            val rowView = inflater.inflate(R.layout.payment_item_method, null, true)

            val titleText = rowView.findViewById(R.id.title) as TextView
            val noteMethod = rowView.findViewById(R.id.note_method) as TextView
            val fee = rowView.findViewById(R.id.txtFee) as TextView
            val imageView = rowView.findViewById(R.id.image) as ImageView
            AddInfoMethod().setTitle(method, titleText, noteMethod,fee)
            AddInfoMethod().addImage(method, imageView)


            return rowView

        }

    }

}