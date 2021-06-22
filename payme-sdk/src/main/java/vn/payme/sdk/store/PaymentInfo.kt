package vn.payme.sdk.store

import android.content.Context
import androidx.fragment.app.FragmentManager
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.enums.Action
import vn.payme.sdk.enums.Env
import vn.payme.sdk.enums.LANGUAGES
import vn.payme.sdk.model.*

class PaymentInfo(
    var action: Action? = null,
    var amount: Int = 0,
    var content: String = "",
    var infoPayment: InfoPayment? = null,
    var methodSelected: Method? = null,
    var extraData : String? = "",
    var transaction : String = "",
    var listService: ArrayList<Service> = arrayListOf(),
    var listMethod: ArrayList<Method> = arrayListOf(),
    var service: Service? = null,
    var isShowResultUI : Boolean = true,
    var isChangeMethod : Boolean = true,
    var storeName : String = "",
    var storeImage : String = "",
) {

}