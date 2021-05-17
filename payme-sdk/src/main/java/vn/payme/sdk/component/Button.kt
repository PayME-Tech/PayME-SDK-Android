package vn.payme.sdk.component

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.store.Store

class Button : RelativeLayout {

    lateinit var progressBar: ProgressBar
    lateinit var textView: TextView
    var isLoadingShowing: Boolean = false


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


    fun enableLoading() {
            progressBar!!.visibility = View.VISIBLE
            isLoadingShowing = true
    }
    fun disableLoading() {
            progressBar!!.visibility = View.INVISIBLE
            isLoadingShowing = false
    }



    private fun init(context: Context, attrs: AttributeSet?) {
        this.background = Store.config.colorApp.backgroundColorRadius
        isLoadingShowing = false
        LayoutInflater.from(getContext()).inflate(R.layout.view_loading_button, this, true)
        progressBar = findViewById<View>(R.id.pb_progress) as ProgressBar
        textView = findViewById<View>(R.id.pb_text) as TextView
        textView.setTextColor(ContextCompat.getColor(PayME.context, R.color.white))
        textView.textSize = 16F
        val typeface: Typeface? = ResourcesCompat.getFont(PayME.context, R.font.semi_bold)
        textView.setTypeface(typeface)

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.LoadingButton,
                0, 0
            )
            try {

                val text = a.getString(R.styleable.LoadingButton_pbText)
                setText(text)

                val progressColor = ContextCompat.getColor(PayME.context, R.color.white)
                setProgressColor(progressColor)
            } finally {
                a.recycle()
            }
        } else {

        }
    }

    fun setProgressColor(colorRes: Int) {
        progressBar.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(colorRes, PorterDuff.Mode.SRC_ATOP)
    }

}