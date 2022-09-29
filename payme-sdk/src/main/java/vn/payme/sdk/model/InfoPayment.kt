package vn.payme.sdk.model

class InfoPayment(
    var action: String?,
    var amount: Int,
    var note: String?,
    var orderId: String,
    var storeId: Long?,
    var type: String?,
    var referExtraData: String?,
    var userName: String?
) {

}