package vn.payme.sdk.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import vn.payme.sdk.R
import vn.payme.sdk.model.Info


class InfoPayment : RelativeLayout {

    lateinit var listView: LinearLayout
    lateinit var textView: TextView


    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    fun setText(text: String?) {
        textView.text = text

    }
    fun updateData(list : ArrayList<Info>){
       for (info in list) {
           val button = ItemInfo(context,info)
           listView.addView(button)
       }
    }


    private fun init(context: Context,attrs: AttributeSet?) {
        LayoutInflater.from(getContext()).inflate(R.layout.info_payment, this, true)
        listView = findViewById<View>(R.id.recipe_list_view) as LinearLayout


    }




}