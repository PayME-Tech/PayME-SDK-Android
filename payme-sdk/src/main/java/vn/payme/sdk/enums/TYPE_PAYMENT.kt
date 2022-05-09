package vn.payme.sdk.enums

class TYPE_PAYMENT {
    companion object{
        const val WALLET :String = "WALLET"
        const val LINKED :String = "LINKED"
        const val BANK_CARD :String = "BANK_CARD"
        const val BANK_QR_CODE :String = "BANK_QR_CODE"
        const val BANK_TRANSFER :String = "BANK_TRANSFER"
        const val CREDIT_CARD :String = "CREDIT_CARD"
        const val MOMO_PG :String = "MOMO_PG"
        const val ZALOPAY_PG :String = "ZALOPAY_PG"
        const val CREDIT_BALANCE :String = "CREDIT_BALANCE"
        const val VIET_QR :String = "VIET_QR"
    }
}