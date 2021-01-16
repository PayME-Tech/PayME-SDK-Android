package vn.payme.sdk.model

class AccountInfo(val accountKyc: Boolean, val accountActive: Boolean) {
    var accountKycSuccess: Boolean = false
    var accountActiveSuccess: Boolean = false

    init {
        accountKycSuccess= accountKyc
        accountActiveSuccess= accountActive
    }

}