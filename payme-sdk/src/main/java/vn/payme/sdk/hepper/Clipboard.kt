package vn.payme.sdk.hepper

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.text.ClipboardManager
import android.widget.Toast
import es.dmoral.toasty.Toasty
import vn.payme.sdk.PayME
import vn.payme.sdk.R

class Clipboard {
     fun setClipboard(context: Context, text: String) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
        Toasty.success(context, context.getString(R.string.copied), Toast.LENGTH_SHORT, true)
            .show();

    }
}