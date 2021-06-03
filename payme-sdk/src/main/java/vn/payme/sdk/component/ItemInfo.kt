package vn.payme.sdk.component

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import vn.payme.sdk.R
import vn.payme.sdk.model.Info

class ItemInfo : RelativeLayout {

    constructor(context: Context,info: Info) : super(context) {
        init(context, info)
    }
    private fun init(context: Context, info: Info) {
        LayoutInflater.from(getContext()).inflate(R.layout.item_info, this, true)
        val txtLabel = findViewById<View>(R.id.txtLabel) as TextView
        val txtValue = findViewById<View>(R.id.txtValue) as TextView
        val dotted = findViewById<View>(R.id.dotted) as ImageView
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
    }



}