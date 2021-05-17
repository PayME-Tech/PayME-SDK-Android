package vn.payme.sdk.component

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.adapter.InfoPaymentAdapter
import vn.payme.sdk.adapter.MethodAdapter
import vn.payme.sdk.model.Info

class InfoPayment : RelativeLayout {

    lateinit var listView: ListView
    lateinit var textView: TextView
    private lateinit var methodAdapter: InfoPaymentAdapter
//    private lateinit var listMethod:ArrayList<Info>


    //endregion
    //region Constructors
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }


    fun setText(text: String?) {
        textView.text = text

    }
   public fun updateData(list : ArrayList<Info>){
        methodAdapter = InfoPaymentAdapter(context,list)
        this.listView.adapter = methodAdapter
       setListViewHeightBasedOnChildren(this.listView)
    }


    private fun init(context: Context,attrs: AttributeSet?) {
        LayoutInflater.from(getContext()).inflate(R.layout.info_payment, this, true)
        listView = findViewById<View>(R.id.recipe_list_view) as ListView


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



}