package vn.payme.sdk.evenbus

class FeeInfo(
    val fee : Int,
    val feeWallet : Int,
    val state : String,
    val message : String
) {
}