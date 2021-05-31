package vn.payme.sdk.enums

class ERROR_CODE {
    companion object{
       val  EXPIRED = 401
       val  NETWORK = -1
       val  SYSTEM = -2
       val  LITMIT = -3
       val  ACCOUNT_NOT_ACTIVATED = -4
        val ACCOUNT_NOT_LOGIN = -9
        val ACCOUNT_NOT_KYC = -5
        val PAYMENT_ERROR = -6
        val ERROR_KEY_ENCODE = -7
        val USER_CANCELLED = -8
        val OTHER = 0

    }

}