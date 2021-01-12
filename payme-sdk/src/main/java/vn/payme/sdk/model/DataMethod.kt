package vn.payme.sdk.model

class DataMethod {
    var linkedId : String? = null
    var swiftCode : String? = null
    constructor(linkedId : String?,swiftCode : String?){
        this.linkedId = linkedId
        this.swiftCode = swiftCode
    }

}