package vn.payme.sdk.model

class MyEven {
    var value: String?
    var type: TypeCallBack?

    constructor(type : TypeCallBack, value:String){
        this.type = type
        this.value = value
    }
}