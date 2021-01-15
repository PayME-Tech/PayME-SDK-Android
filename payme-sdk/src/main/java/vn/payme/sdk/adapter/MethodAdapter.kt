package vn.payme.sdk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.model.Method
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

    fun getTitle(method: Method, titleText: TextView, noteMethod: TextView) {
        if(method.type==TYPE_PAYMENT.BANK_CARD){
            titleText.text = method.title
        }else if (method.type == TYPE_PAYMENT.WALLET) {
            titleText.text = method.title
            val decimal = DecimalFormat("#,###")
            noteMethod.text = "(${decimal.format(PayME.balance?.toLong())}đ)"
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
        println("Method"+method.toString())
        if(method.type==TYPE_PAYMENT.BANK_CARD){
            imageView.setImageResource(R.drawable.icon_atm)
        }else if (method.type == TYPE_PAYMENT.WALLET) {
            imageView.setImageResource(R.drawable.iconwallet)
        }else if (method.type == TYPE_PAYMENT.NAPAS || method.type == TYPE_PAYMENT.LINKED) {
            println("LoadPICA")
            val picasso = Picasso.get()
            picasso.setIndicatorsEnabled(true)
            picasso.load("https://firebasestorage.googleapis.com/v0/b/vn-mecorp-payme-wallet.appspot.com/o/image_bank%2Fimage_method%2Fmethod${method.data?.swiftCode}.png?alt=media&token=28cdb30e-fa9b-430c-8c0e-5369f500612e")
                .resize(50, 50)
                .centerInside()
                .into(imageView)

        }else{
            imageView.setImageResource(R.drawable.iconwallet)

        }

    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val method = dataSource[position]
        val rowView = inflater.inflate(R.layout.item_method, null, true)
        val titleText = rowView.findViewById(R.id.title) as TextView
        val noteMethod = rowView.findViewById(R.id.note_method) as TextView
        val imageView = rowView.findViewById(R.id.image) as ImageView

        getTitle(method, titleText, noteMethod)

        addImage(method, imageView)


        return rowView
    }

}