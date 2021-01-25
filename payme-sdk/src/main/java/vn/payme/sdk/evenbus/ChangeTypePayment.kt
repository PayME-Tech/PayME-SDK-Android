package vn.payme.sdk.evenbus

class ChangeTypePayment {
    var value: String?
    var type: String

    constructor(type : String, value:String){
        this.type = type
        this.value = value
    }
}