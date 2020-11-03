package vn.payme.sdk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import vn.payme.sdk.R
import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.model.Method
import java.text.DecimalFormat

class MethodAdapter(private val context: Context,
                    private val dataSource: ArrayList<Method>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

    fun getTitle(method: Method, titleText: TextView, noteMethod: TextView) {

        if (method.type == TYPE_PAYMENT.APP_WALLET) {
            titleText.text = "Số dư ví "
            val decimal = DecimalFormat("#,###")
            noteMethod.text = "(${decimal.format(method.amount?.toLong())}đ)"
            noteMethod.textSize = 12F
            return
        } else if (method.type == TYPE_PAYMENT.NAPAS || method.type == TYPE_PAYMENT.PVCB) {
            titleText.text = method.bankCode!!
            noteMethod.text = method.cardNumber
            return

        } else {
            titleText.text = method.detail
        }
    }

    fun getImage(method: Method): Int {

        if (method.type === TYPE_PAYMENT.APP_WALLET) {
            return R.drawable.iconwallet
        }
        if (method.type === TYPE_PAYMENT.NAPAS || method.type === TYPE_PAYMENT.PVCB) {
            if (method.swiftCode === "BFTVVNVX") {
                return R.drawable.bank_vietcom_bank_ico
            }
            if (method.swiftCode === "VBAAVNVX") {
                return R.drawable.bank_agri_bank_ico
            }
            if (method.swiftCode === "WBVNVNVX") {
                return R.drawable.bank_pvcombank_ico
            }
            if (method.swiftCode === "VTCBVNVX") {
                return R.drawable.bank_techcombank_ico
            }
            if (method.swiftCode === "TPBVVNVX") {
                return R.drawable.bank_tpbank_ico
            }
        }
        return R.drawable.iconwallet

    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val method = dataSource[position]
        val rowView = inflater.inflate(R.layout.item_method, null, true)
        val titleText = rowView.findViewById(R.id.title) as TextView
        val noteMethod = rowView.findViewById(R.id.note_method) as TextView
        val imageView = rowView.findViewById(R.id.image) as ImageView
        val checkBox = rowView.findViewById(R.id.checkbox) as ImageView
        if (method.selected!!) {
            checkBox.setImageResource(R.drawable.checked)
        } else {
            checkBox.setImageResource(R.drawable.uncheck)
        }
        getTitle(method, titleText, noteMethod)

        imageView.setImageResource(getImage(method))


        return rowView
    }

}