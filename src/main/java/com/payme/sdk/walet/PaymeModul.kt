package com.payme.sdk.walet

import android.content.Context
import android.content.Intent
import com.payme.sdk.walet.modul.UserInfor

class PaymeModul {
    companion object {
        var transID: String? = ""
        var userID: String? = ""
        var tokenLink: String? = ""
        var action: String? = ""
        var money: Number? = 0
        var userInfor: UserInfor? = null
    }


    public fun openLinkWallet(
        context: Context,
        transID: String?,
        userID: String?,
        tokenLink: String?,
        action: String?,
        money: Number?,
        userInfor: UserInfor?
    ) {
        PaymeModul.transID = transID
        PaymeModul.userID = userID
        PaymeModul.tokenLink = tokenLink
        PaymeModul.action = action
        PaymeModul.money = money
        PaymeModul.userInfor = userInfor

        val intent: Intent = Intent(context, PaymeWaletActivity::class.java)
        context.startActivity(intent)
    }
}