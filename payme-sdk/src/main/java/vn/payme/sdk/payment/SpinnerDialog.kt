package vn.payme.sdk.payment

import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.payme.sdk.R
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.store.Store


class SpinnerDialog : DialogFragment() {
    lateinit var progressBar : ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view  =  inflater.inflate(R.layout.payme_loading_full_screen, container)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.getIndeterminateDrawable()
            .mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )
        EventBus.getDefault().register(this)

        return view

    }

    override fun show(manager: FragmentManager, tag: String?) {
        if(isVisible) return
        super.show(manager, tag)

    }
       @Subscribe
    fun onText(myEven: MyEven) {
        if (myEven.type == TypeCallBack.onExpired) {
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.isCancelable = false
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }
    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, R.layout.payme_payment_layout, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
    }
}