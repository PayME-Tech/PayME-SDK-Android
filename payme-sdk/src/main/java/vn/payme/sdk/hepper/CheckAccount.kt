package vn.payme.sdk

import org.json.JSONObject
import vn.payme.sdk.enums.ERROR_CODE
import vn.payme.sdk.store.Store

enum class RULE_CHECK_ACCOUNT {
    LOGGIN_ACTIVE_KYC,
    LOGGIN_ACTIVE,
    LOGGIN,
}

class CheckAccount {
    fun check(
        rule: RULE_CHECK_ACCOUNT,
        onError: (JSONObject?, Int, String?) -> Unit
    ): Boolean {
        if (rule == RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE_KYC) {
            if (!Store.userInfo.accountLoginSuccess) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_LOGIN, PayME.context.getString(R.string.account_not_logged_in))
                return false
            } else if (!Store.userInfo.accountActive) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVATED, PayME.context.getString(R.string.account_not_activated))
                return false
            } else if (!Store.userInfo.accountKycSuccess) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_KYC, PayME.context.getString(R.string.account_unknown))
                return false
            }
            return true
        }
        if (rule == RULE_CHECK_ACCOUNT.LOGGIN_ACTIVE) {
            if (!Store.userInfo.accountLoginSuccess) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_LOGIN, PayME.context.getString(R.string.account_not_logged_in))
                return false
            } else if (!Store.userInfo.accountActive) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_ACTIVATED, PayME.context.getString(R.string.account_not_activated))
                return false
            }
            return true
        }
        if (rule == RULE_CHECK_ACCOUNT.LOGGIN) {
            if (!Store.userInfo.accountLoginSuccess) {
                onError(null, ERROR_CODE.ACCOUNT_NOT_LOGIN,PayME.context.getString(R.string.account_not_logged_in))
                return false
            }
            return true
        }
        return true

    }
}