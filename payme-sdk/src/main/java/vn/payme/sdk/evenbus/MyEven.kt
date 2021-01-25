package vn.payme.sdk.evenbus

import vn.payme.sdk.enums.TypeCallBack

class MyEven {
    var value: String?
    var type: TypeCallBack?

    constructor(type : TypeCallBack, value:String){
        this.type = type
        this.value = value
    }
}