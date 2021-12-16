package vn.payme.sdk.model

class InfoPayment {
    var action : String? = null
    var amount : Int = 0
    var note : String? = null
    var orderId : String
    var storeId : Long?= null
    var type : String? = null
    var userName : String? = null
    var referExtraData : String? = null

    constructor(action : String?,amount : Int,note : String?,orderId : String,storeId : Long?,type : String?,referExtraData:String?,userName:String?){
        this.action = action
        this.amount = amount
        this.note = note
        this.orderId = orderId
        this.storeId = storeId
        this.type = type
        this.referExtraData = referExtraData
        this.userName = userName
    }
}