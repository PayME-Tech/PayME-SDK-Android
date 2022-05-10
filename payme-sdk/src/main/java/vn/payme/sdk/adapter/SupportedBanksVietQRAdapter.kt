package vn.payme.sdk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import vn.payme.sdk.R


class SupportedBanksVietQRAdapter() :
    ListAdapter<String, SupportedBanksVietQRAdapter.BankViewHolder>(SupportedBanksDiffCallback) {
    class BankViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val imageBank: ImageView = itemView.findViewById(R.id.imageBank)
        init {}
        fun bind(swiftCode: String) {
            val picasso = Picasso.get()
            picasso.setIndicatorsEnabled(false)
            picasso.load("https://static.payme.vn/image_bank/icon_banks/icon${swiftCode}@2x.png")
                .resize(150, 150)
                .centerInside()
                .into(imageBank)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.payme_item_supported_bank_vietqr, parent, false)
        return BankViewHolder(view)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        val bank = getItem(position)
        holder.bind(bank)
    }
}

object SupportedBanksDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}