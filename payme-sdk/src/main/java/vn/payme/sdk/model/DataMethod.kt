package vn.payme.sdk.model

class DataMethod {
    var linkedId : Long? = null
    var swiftCode : String? = null
    var issuer : String? = null
    var supplierLinkedId : String? = null
    constructor(linkedId : Long?,swiftCode : String?,issuer:String?, supplierLinkedId: String?){
        this.linkedId = linkedId
        this.swiftCode = swiftCode
        this.issuer = issuer
        this.supplierLinkedId = supplierLinkedId
    }
}