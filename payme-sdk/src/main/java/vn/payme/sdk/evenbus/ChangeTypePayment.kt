package vn.payme.sdk.evenbus

import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.model.TypeCallBack

class ChangeTypePayment {
    var value: String?
    var type: String

    constructor(type : String, value:String){
        this.type = type
        this.value = value
    }
}