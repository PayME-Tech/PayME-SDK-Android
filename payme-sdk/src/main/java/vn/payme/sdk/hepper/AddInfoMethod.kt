package vn.payme.sdk.hepper

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import vn.payme.sdk.R
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store
import java.text.DecimalFormat

class AddInfoMethod {
    fun addImage(method: Method, imageView: ImageView) {
        if (method.type == TYPE_PAYMENT.ZALOPAY_PG) {
            imageView.setImageResource(R.drawable.ic_zalo)
        }else if (method.type == TYPE_PAYMENT.BANK_TRANSFER) {
            imageView.setImageResource(R.drawable.ic_bank_transfer)
        }else  if (method.type == TYPE_PAYMENT.MOMO_PG) {
            imageView.setImageResource(R.drawable.ic_momo)
        }else  if (method.type == TYPE_PAYMENT.CREDIT_CARD) {
            imageView.setImageResource(R.drawable.ic_credit_card)
        }else  if (method.type == TYPE_PAYMENT.BANK_QR_CODE) {
            imageView.setImageResource(R.drawable.ic_qr_code)
        }else if (method.type == TYPE_PAYMENT.BANK_CARD) {
            imageView.setImageResource(R.drawable.ic_atm)
        } else if (method.type == TYPE_PAYMENT.WALLET) {
            imageView.setImageResource(R.drawable.ic_payme)
        } else if (method.type == TYPE_PAYMENT.LINKED) {
            val picasso = Picasso.get()
            picasso.setIndicatorsEnabled(false)
            val imageCode = if(method.data?.issuer!= "null") method.data?.issuer else method.data?.swiftCode
            picasso.load("https://firebasestorage.googleapis.com/v0/b/vn-mecorp-payme-wallet.appspot.com/o/image_bank%2Fimage_method%2Fmethod${imageCode}.png?alt=media&token=28cdb30e-fa9b-430c-8c0e-5369f500612e")
                .resize(50, 50)
                .centerInside()
                .into(imageView)

        } else {
            imageView.setImageResource(R.drawable.ic_payme)
        }

    }
    fun setTitle(method: Method, titleText: TextView, noteMethod: TextView?,txtFee:TextView?) {
        if(txtFee!=null){
            txtFee.text = method.feeDescription
        }
        if(method.type == TYPE_PAYMENT.WALLET){
            if(Store.userInfo.accountKycSuccess && Store.userInfo.accountActive){
                noteMethod?.visibility = View.VISIBLE
            }else{
                noteMethod?.visibility = View.GONE
            }
        }
        if (method.type == TYPE_PAYMENT.BANK_CARD) {
            titleText.text = method.title
        } else if (method.type == TYPE_PAYMENT.WALLET) {
            titleText.text = method.title
            val decimal = DecimalFormat("#,###")
            if(Store.userInfo.accountKycSuccess){
                noteMethod?.text = "(${decimal.format(Store.userInfo.balance?.toLong())}đ)"
            }
            return
        } else if (method.type == TYPE_PAYMENT.LINKED) {
            titleText.text = method.title!!
            noteMethod?.text = method.label!!
            return
        } else {
            titleText.text = method.title
        }

    }
}