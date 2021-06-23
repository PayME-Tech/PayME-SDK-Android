package vn.payme.sdk.model

class DataMethod {
    var linkedId : Double? = null
    var swiftCode : String? = null
    var issuer : String? = null
    constructor(linkedId : Double?,swiftCode : String?,issuer:String?){
        this.linkedId = linkedId
        this.swiftCode = swiftCode
        this.issuer = issuer
    }

}