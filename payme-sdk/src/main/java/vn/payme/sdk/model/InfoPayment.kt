package vn.payme.sdk.model

class InfoPayment {
    var action : String? = null
    var amount : Int? = null
    var note : String? = null
    var orderId : Int? = null
    var storeId : Int? = null
    var type : String? = null

    constructor(action : String?,amount : Int?,note : String?,orderId : Int?,storeId : Int?,type : String?){
        this.action = action
        this.amount = amount
        this.note = note
        this.orderId = orderId
        this.storeId = storeId
        this.type = type
    }
}