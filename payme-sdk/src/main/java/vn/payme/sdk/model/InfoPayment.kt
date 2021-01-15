package vn.payme.sdk.model

class InfoPayment {
    var action : String? = null
    var amount : Int? = null
    var note : String? = null
    var orderId : Long? = null
    var storeId : Long? = null
    var type : String? = null

    constructor(action : String?,amount : Int?,note : String?,orderId : Long?,storeId : Long?,type : String?){
        this.action = action
        this.amount = amount
        this.note = note
        this.orderId = orderId
        this.storeId = storeId
        this.type = type
    }
}