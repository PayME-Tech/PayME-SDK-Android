package vn.payme.sdk.component

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.store.Store

class Button : RelativeLayout {

    lateinit var progressBar: ProgressBar
    lateinit var containerTitle: ConstraintLayout
    lateinit var iconLeft: ImageView
    lateinit var textView: TextView
    var isLoadingShowing: Boolean = false
    var isActive: Boolean = true


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
    fun setVisible(isVisible:Boolean){
        isActive  = isVisible
        if(isVisible){
            this.background = Store.config.colorApp.backgroundColorRadius
        }else{
            this.background = resources.getDrawable(R.drawable.background_button_unactive)
        }
    }



    fun enableLoading() {
        progressBar!!.visibility = View.VISIBLE
        containerTitle!!.visibility = View.GONE
        isLoadingShowing = true
    }

    fun disableLoading() {
        progressBar!!.visibility = View.GONE
        containerTitle!!.visibility = View.VISIBLE
        isLoadingShowing = false
    }
    fun setButtonTypeBorder(){
        this.background  = Store.config.colorApp.backgroundColorRadiusBorder
        textView.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
    }


    private fun init(context: Context, attrs: AttributeSet?) {
        this.background = Store.config.colorApp.backgroundColorRadius
        isLoadingShowing = false
        LayoutInflater.from(getContext()).inflate(R.layout.payme_view_loading_button, this, true)
        progressBar = findViewById<View>(R.id.pb_progress) as ProgressBar
        textView = findViewById<View>(R.id.pb_text) as TextView
        containerTitle = findViewById<View>(R.id.container_title) as ConstraintLayout
        iconLeft = findViewById<View>(R.id.iconLeft) as ImageView
        textView.setTextColor(ContextCompat.getColor(PayME.context, R.color.white))
        textView.textSize = 16F
        val typeface: Typeface? = ResourcesCompat.getFont(PayME.context, R.font.main_bold)
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