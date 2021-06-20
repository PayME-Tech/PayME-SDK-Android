package vn.payme.sdk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import vn.payme.sdk.R
import vn.payme.sdk.model.BankInfo
import vn.payme.sdk.model.BankTransferInfo


class BankAdapter(private val onClick: (BankTransferInfo) -> Unit) :
    ListAdapter<BankTransferInfo, BankAdapter.BankViewHolder>(FlowerDiffCallback) {
    class BankViewHolder(itemView: View, val onClick: (BankTransferInfo) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val imageBank: ImageView = itemView.findViewById(R.id.imageBank)
        private var currentFlower: BankTransferInfo? = null

        init {
            itemView.setOnClickListener {
                currentFlower?.let {
                    onClick(it)
                }
            }
        }
        /* Bind flower name and image. */
        fun bind(bank: BankTransferInfo) {
            currentFlower = bank
            val picasso = Picasso.get()
            picasso.setIndicatorsEnabled(false)
            picasso.load("https://firebasestorage.googleapis.com/v0/b/vn-mecorp-payme-wallet.appspot.com/o/image_bank%2Ficon_banks%2Ficon${bank.swiftCode}%402x.png?alt=media&token=0c6cd79a-9a4f-4ea2-b178-94e0b4731ac2")
                .resize(150, 150)
                .centerInside()
                .into(imageBank)
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bank, parent, false)
        return BankViewHolder(view, onClick)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        val flower = getItem(position)
        holder.bind(flower)

    }
}

object FlowerDiffCallback : DiffUtil.ItemCallback<BankTransferInfo>() {
    override fun areItemsTheSame(oldItem: BankTransferInfo, newItem: BankTransferInfo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: BankTransferInfo, newItem: BankTransferInfo): Boolean {
        return oldItem.swiftCode == newItem.swiftCode
    }
}