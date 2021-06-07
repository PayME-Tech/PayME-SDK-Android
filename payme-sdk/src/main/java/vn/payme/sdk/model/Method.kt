package vn.payme.sdk.model

class Method {
    var data : DataMethod? = null
    var fee : Number? = null
    var label : String? = null
    var methodId : Number? = null
    var minFee : Number? = null
    var title : String = ""
    var type : String? = null
    var feeDescription : String? = ""

    constructor(data : DataMethod?,fee : Number?,label : String?,methodId : Number?,minFee : Number?,feeDescription : String?,title : String,type : String?){
        this.data= data
        this.fee = fee
        this.label = label
        this.methodId = methodId
        this.minFee = minFee
        if(feeDescription!= null  && feeDescription!= "null" ){
            this.feeDescription = feeDescription
        }
        this.title = title
        this.type = type
    }


}