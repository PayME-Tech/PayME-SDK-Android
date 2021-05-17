package vn.payme.sdk.adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store
import java.text.DecimalFormat

class MethodAdapter(
    private val context: Context,
    private val dataSource: ArrayList<Method>
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

    fun setTitle(method: Method, titleText: TextView, noteMethod: TextView) {
        if (method.type == TYPE_PAYMENT.BANK_CARD) {
            titleText.text = method.title
        } else if (method.type == TYPE_PAYMENT.WALLET) {
            titleText.text = method.title
            val decimal = DecimalFormat("#,###")
            noteMethod.text = "(${decimal.format(Store.userInfo.balance?.toLong())}đ)"
            noteMethod.textSize = 12F
            return
        } else if (method.type == TYPE_PAYMENT.NAPAS || method.type == TYPE_PAYMENT.LINKED) {
            titleText.text = method.title!!
            noteMethod.text = method.label!!
            return
        } else {
            titleText.text = method.title
        }
    }

    private fun addImage(method: Method, imageView: ImageView) {
        if (method.type == TYPE_PAYMENT.BANK_CARD) {
            imageView.setImageResource(R.drawable.icon_atm)
        } else if (method.type == TYPE_PAYMENT.WALLET) {
            imageView.setImageResource(R.drawable.iconwallet)
        } else if (method.type == TYPE_PAYMENT.NAPAS || method.type == TYPE_PAYMENT.LINKED) {
            val picasso = Picasso.get()
            picasso.setIndicatorsEnabled(false)
            picasso.load("https://firebasestorage.googleapis.com/v0/b/vn-mecorp-payme-wallet.appspot.com/o/image_bank%2Fimage_method%2Fmethod${method.data?.swiftCode}.png?alt=media&token=28cdb30e-fa9b-430c-8c0e-5369f500612e")
                .resize(50, 50)
                .centerInside()
                .into(imageView)

        } else {
            imageView.setImageResource(R.drawable.iconwallet)

        }

    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val method = dataSource[position]


        if (
            method.type == TYPE_PAYMENT.WALLET
            && ( !Store.userInfo.accountActive || !Store.userInfo.accountKycSuccess ||Store.paymentInfo.amount > Store.userInfo.balance)
        ) {
            val rowView = inflater.inflate(R.layout.item_method_wallet_not_kyc, null, true)
            val titleText = rowView.findViewById(R.id.title) as TextView
            val noteMethod = rowView.findViewById(R.id.note_method) as TextView
            val txtButton = rowView.findViewById(R.id.txtButton) as TextView
            val txtDescription = rowView.findViewById(R.id.txtDescription) as TextView
            val button = rowView.findViewById(R.id.button) as ConstraintLayout
            button.background = Store.config.colorApp.backgroundColorRadiusBorder
            txtButton.setTextColor(Color.parseColor(Store.config.colorApp.startColor))
            button.setOnClickListener {
                val paymeSDK = PayME(
                    PayME.context,
                    Store.config.appToken,
                    Store.config.publicKey,
                    Store.config.connectToken,
                    Store.config.appPrivateKey,
                    Store.config.configColor!!,
                    Store.config.language,
                    Store.config.env!!,
                    Store.config.showLog
                )
                if (!Store.userInfo.accountActive) {
                    paymeSDK.openWallet(PayME.onSuccess, PayME.onError)
                } else if (!Store.userInfo.accountKycSuccess) {
                    paymeSDK.openWallet(PayME.onSuccess, PayME.onError)
                } else if (Store.paymentInfo.amount > Store.userInfo.balance) {
                    paymeSDK.deposit(0, "", "", PayME.onSuccess, PayME.onError)
                }
                var even: EventBus = EventBus.getDefault()
                var myEven: MyEven = MyEven(TypeCallBack.onClose, "")
                even.post(myEven)
            }
            if (!Store.userInfo.accountActive) {
                txtButton.setText(R.string.active_now)
                txtDescription.setText(R.string.description_active_now)
            } else if (!Store.userInfo.accountKycSuccess) {
                txtButton.setText(R.string.kyc_now)
                txtDescription.setText(R.string.description_kyc_now)
            } else if (Store.paymentInfo.amount > Store.userInfo.balance) {
                txtButton.setText(R.string.deposit_now)
                txtDescription.setText(R.string.description_deposit_now)
            }
            setTitle(method, titleText, noteMethod)
            return rowView

        } else {
            val rowView = inflater.inflate(R.layout.item_method, null, true)

            val titleText = rowView.findViewById(R.id.title) as TextView
            val noteMethod = rowView.findViewById(R.id.note_method) as TextView
            val imageView = rowView.findViewById(R.id.image) as ImageView

            val wrapImageMethod = rowView.findViewById(R.id.wrap_image_method) as CardView
            val imageWallet = rowView.findViewById(R.id.imageWallet) as ImageView
            if (method.type == TYPE_PAYMENT.WALLET) {
                imageWallet.visibility = View.VISIBLE
                wrapImageMethod.visibility = View.GONE
            }

            setTitle(method, titleText, noteMethod)

            addImage(method, imageView)


            return rowView

        }

    }

}