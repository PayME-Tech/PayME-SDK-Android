package vn.payme.sdk.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import vn.payme.sdk.R
import vn.payme.sdk.model.Info

class InfoPaymentAdapter(
    private val context: Context,
    private val dataSource: ArrayList<Info>
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
        val info = dataSource[position]

            val rowView = inflater.inflate(R.layout.item_info, null, true)
            val txtLabel = rowView.findViewById(R.id.txtLabel) as TextView
            val txtValue = rowView.findViewById(R.id.txtValue) as TextView
            val dotted = rowView.findViewById(R.id.dotted) as ImageView

            txtLabel.text = info.label
            txtValue.text = info.value
            if(info.isEnd){
                dotted.visibility = View.GONE
            }
            if(info.labelColor!=null){
                txtLabel.setTextColor(info.labelColor!!)
            }
            if(info.valueColor!=null){
                txtValue.setTextColor(info.valueColor!!)
            }


            return rowView


    }

}