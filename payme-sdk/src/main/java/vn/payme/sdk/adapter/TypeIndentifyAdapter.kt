package vn.payme.sdk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import vn.payme.sdk.R
import vn.payme.sdk.model.TypeIdentify

class TypeIndentifyAdapter(
    private val context: Context,
    private val dataSource: ArrayList<TypeIdentify>
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
    fun getTitle(typeIdentify : TypeIdentify, titleText: TextView) {
            titleText.text = typeIdentify.title
    }
    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val method = dataSource[position]
        val rowView = inflater.inflate(R.layout.item_type_indentify, null, true)
        val titleText = rowView.findViewById(R.id.title) as TextView
        val checkBox = rowView.findViewById(R.id.checkbox) as ImageView
        if (method.selected!!) {
            checkBox.setImageResource(R.drawable.checked)
        } else {
            checkBox.setImageResource(R.drawable.uncheck)
        }
        getTitle(method, titleText)
        return rowView
    }

}