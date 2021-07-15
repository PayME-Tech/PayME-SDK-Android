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
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.R
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.evenbus.FeeInfo
import vn.payme.sdk.hepper.AddInfoMethod
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store
import java.util.*
import kotlin.collections.ArrayList

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
        val total = Store.paymentInfo.infoPayment!!.amount + EventBus.getDefault().getStickyEvent(FeeInfo::class.java).feeWallet

        if (
            method.type == TYPE_PAYMENT.WALLET
            && ( !Store.userInfo.accountActive || !Store.userInfo.accountKycSuccess ||total > Store.userInfo.balance)
        ) {
            val rowView = inflater.inflate(R.layout.item_method_wallet_not_kyc, null, true)
            val titleText = rowView.findViewById(R.id.title) as TextView
            val noteMethod = rowView.findViewById(R.id.note_method) as TextView
            val txtButton = rowView.findViewById(R.id.txtButton) as TextView
            val txtDescription = rowView.findViewById(R.id.txtDescription) as TextView
            val button = rowView.findViewById(R.id.button) as ConstraintLayout
            button.background = Store.config.colorApp.backgroundColorRadiusBorder30
            txtButton.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
            if (!Store.userInfo.accountActive) {
                txtButton.setText(R.string.active_now)
                txtDescription.setText(R.string.description_active_now)
            } else if (!Store.userInfo.accountKycSuccess) {
                txtButton.setText(R.string.kyc_now)
                txtDescription.setText(R.string.description_kyc_now)
            } else if (total > Store.userInfo.balance) {
                txtButton.setText(R.string.deposit_now)
                txtDescription.setText(R.string.description_deposit_now)
            }
           AddInfoMethod().setTitle(method, titleText, noteMethod,null)
            return rowView

        } else {

            val rowView = inflater.inflate(R.layout.payment_item_method, null, true)
            val titleText = rowView.findViewById(R.id.title) as TextView
            val noteMethod = rowView.findViewById(R.id.note_method) as TextView
            val txtFee = rowView.findViewById(R.id.txtFee) as TextView
            val container = rowView.findViewById(R.id.container) as CardView
            val imageView = rowView.findViewById(R.id.image) as ImageView
            val buttonSelect = rowView.findViewById(R.id.buttonSelect) as ImageView
            if(method.methodId == Store.paymentInfo.methodSelected?.methodId && method.type == Store.paymentInfo.methodSelected?.type){
                container.background = Store.config.colorApp.backgroundColorRadiusBorder30
                ChangeColorImage().changeColor(context,buttonSelect,R.drawable.ic_checked,1)
            }else{
                buttonSelect.setImageResource(R.drawable.ic_uncheck)

            }

            AddInfoMethod().setTitle(method, titleText, noteMethod,txtFee)

            AddInfoMethod().addImage(method, imageView)


            return rowView

        }

    }

}