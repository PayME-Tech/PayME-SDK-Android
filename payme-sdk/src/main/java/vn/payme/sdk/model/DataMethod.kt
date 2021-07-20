package vn.payme.sdk.model

class DataMethod {
    var linkedId : Long? = null
    var swiftCode : String? = null
    var issuer : String? = null
    constructor(linkedId : Long?,swiftCode : String?,issuer:String?){
        this.linkedId = linkedId
        this.swiftCode = swiftCode
        this.issuer = issuer
    }

}