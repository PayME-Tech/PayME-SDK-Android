package vn.payme.sdk.component

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.cardmodules.ScanActivity
import vn.payme.sdk.cardmodules.ScanActivityImpl
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.kyc.CameraKycPopup
import vn.payme.sdk.kyc.PermissionCamera
import vn.payme.sdk.store.Store

class InputInfo : RelativeLayout {
    lateinit  var txtTitle : TextView
    lateinit  var txtTitleRight : TextView
    lateinit  var imageRight : ImageView
    lateinit  var input : EditText
    lateinit  var containerInput : ConstraintLayout
    lateinit  var progressBar : ProgressBar
    lateinit  var buttonScan : ImageView
    var titleDefault  = ""
    val backgroundError = GradientDrawable()
    var isError = false
    var isFocus = false

    fun setError(message:String){
        isError = true
        containerInput.background = backgroundError
        txtTitle.text = message
        txtTitle.setTextColor(ContextCompat.getColor(context,R.color.red))
    }

    fun setDefault(title:String?){
        isError = false
        if(title!=null){
            txtTitle.text = title
        }else{
            txtTitle.text = titleDefault
        }
        txtTitle.setTextColor(ContextCompat.getColor(context,R.color.nb40))
        if (isFocus){
            containerInput.background = Store.config.colorApp.backgroundColorRadiusBorder
        }else{
            containerInput.background = ContextCompat.getDrawable(PayME.context, R.drawable.background_gray_radius)
        }
    }

    private fun setColorFocus(){
        isFocus = true
        if(isError){
            containerInput.background = backgroundError
        }else{
            containerInput.background = Store.config.colorApp.backgroundColorRadiusBorder
        }
        input.background = ContextCompat.getDrawable(PayME.context, R.drawable.background_radius)
    }

    private fun setColorDefault(){
        isFocus = false
        containerInput.background = ContextCompat.getDrawable(PayME.context, R.drawable.background_gray_radius)
        input.background = ContextCompat.getDrawable(PayME.context, R.drawable.background_gray_radius)
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(getContext()).inflate(R.layout.payme_input, this, true)
        txtTitle = findViewById<View>(R.id.title) as TextView
        txtTitleRight = findViewById<View>(R.id.titleRight) as TextView
        input = findViewById<View>(R.id.input) as EditText
        imageRight = findViewById<View>(R.id.imageRight) as ImageView
        containerInput = findViewById<View>(R.id.containerInput) as ConstraintLayout
        buttonScan = findViewById<View>(R.id.imageScan) as ImageView
        buttonScan.setOnClickListener {
            val currentFragment = PayME.fragmentManager.findFragmentByTag("inputFragment")
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                ScanActivity.warmUp(context)
                val intent = Intent(context, ScanActivityImpl::class.java)
                currentFragment?.startActivityForResult(intent, 51234)
            } else {
                currentFragment?.let { it1 ->
                    if (PayME.enableSetting) {
                        EventBus.getDefault()
                            .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.OPEN_SETTING, null))
                    } else {
                        PermissionCamera().requestCameraFragment(context, it1)
                    }
                }
            }
        }
        ChangeColorImage().changeColor(context,buttonScan,R.drawable.ic_scan,1)
        progressBar = findViewById(R.id.loading)
        progressBar.indeterminateDrawable
            .mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )

        backgroundError.setStroke(2, ContextCompat.getColor(context,R.color.red))
        backgroundError.cornerRadius = 30f

        containerInput.setOnClickListener {
            input.requestFocus()
            Keyboard.showKeyboard(context)
        }

        input.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                setColorDefault()
            }else{
                setColorFocus()
            }
        }

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.Input,
                0, 0
            )
            try {
                val title = a.getString(R.styleable.Input_pbTitle)
                titleDefault = title.toString()
                val hint = a.getString(R.styleable.Input_pbHint)
                val digits = a.getString(R.styleable.Input_pbDigits)
                val inputType = a.getString(R.styleable.Input_pbInputType)
                val maxlength = a.getInteger(R.styleable.Input_pbMaxlength,0)
                txtTitle.text = title
                if (title.equals(context.getString(R.string.card_number)) && Store.config.scanModuleEnable) {
                    buttonScan.visibility = View.VISIBLE
                }
                input.hint = hint
                input.filters = arrayOf(InputFilter.LengthFilter(maxlength),InputFilter.AllCaps())
                if(digits!=null){
                    input.keyListener =  DigitsKeyListener.getInstance(digits)
                }
                if (inputType=="number"){
                    input.inputType  = InputType.TYPE_CLASS_NUMBER
                }
                if (inputType=="textPassword|number"){
                    input.inputType  = InputType.TYPE_CLASS_NUMBER
                    input.transformationMethod = PasswordTransformationMethod.getInstance();
                }
                if (inputType=="textFilter"){
                    input.setRawInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                }
            } finally {
                a.recycle()
            }
        }
    }

}