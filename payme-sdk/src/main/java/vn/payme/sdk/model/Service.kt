package vn.payme.sdk.model

class Service {
    lateinit var code: String
    lateinit var description: String
    constructor(code: String,description: String){
        this.code = code
        this.description = description
    }
}