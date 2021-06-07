package vn.payme.sdk.store

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import es.dmoral.toasty.Toasty
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.enums.Action
import vn.payme.sdk.enums.Env
import vn.payme.sdk.enums.LANGUAGES
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.*

class UserInfo(
    var balance: Long,
    var accountKycSuccess: Boolean,
    var accountLoginSuccess: Boolean,
    var accountActive: Boolean,
    var accessToken: String = "",
    var dataInit: JSONObject? = null,
) {


}