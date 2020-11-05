package vn.payme.sdk.model

class Method {
    var linkedId : Number? = null
    var detail : String? = ""
    var bankCode : String? = ""
    var amount : Number? = 0
    var type : String? = null
    var cardNumber : String? = ""
    var swiftCode : String? = ""
    var selected : Boolean? = false


    constructor(detail: String?,linkedId:Number?,bankCode:String?,amount:Number?,type:String?,cardNumber:String?,swiftCode:String?,selected:Boolean?){
        this.linkedId = linkedId
        this.detail = detail
        this.bankCode = bankCode
        this.amount = amount
        this.type = type
        this.cardNumber = cardNumber
        this.swiftCode = swiftCode
        this.selected = selected


    }
}