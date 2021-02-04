package com.minhkhoa.androidpaymesdk

import android.content.ClipData
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.ClipboardManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.payme.sdk.PayME


internal class AdapterRecycleviewLog(private var dataList: List<String>) :
    RecyclerView.Adapter<AdapterRecycleviewLog.MyViewHolder>(){
    lateinit var adapterContext: Context

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnLongClickListener {
        val titleLogText: TextView = view.findViewById(R.id.textTitleLog)
        val contentLogText: TextView = view.findViewById(R.id.textContentLog)
        val logItem: LinearLayout = view.findViewById(R.id.logItem)

        init {
            logItem.setOnLongClickListener(this)
        }

        override fun onLongClick(v: View?): Boolean {
            setClipboard(adapterContext, dataList[adapterPosition])
//            PayME.showError("Copied successfully")
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRecycleviewLog.MyViewHolder {
        adapterContext = parent.context
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycleview_row_log,
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: AdapterRecycleviewLog.MyViewHolder, position: Int) {
        holder.titleLogText.text = "Log${position}"
        holder.contentLogText.text = dataList[position]
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    private fun setClipboard(context: Context, text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = text
        } else {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText("Copied Log", text)
            clipboard.setPrimaryClip(clip)
        }
    }

}


