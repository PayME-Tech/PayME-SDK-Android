package vn.payme.sdk.enums

class TYPE_PAYMENT {
    companion object{
        val WALLET :String = "WALLET"
        val NAPAS :String = "Napas"
        val PAYMENT_RESULT :String = "PAYMENT_RESULT"
        val CONFIRM_OTP_BANK :String = "CONFIRM_OTP_BANK"
        val CONFIRM_OTP_BANK_NAPAS :String = "CONFIRM_OTP_BANK_NAPAS"
        val LINKED :String = "LINKED"
        val BANK_CARD :String = "BANK_CARD"
    }
}