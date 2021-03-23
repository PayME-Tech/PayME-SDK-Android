package vn.payme.sdk.model

class Service {
    lateinit var code: String
    lateinit var description: String
    var disable: Boolean = false
    var enable: Boolean = false
    constructor(code: String,description: String){
        this.code = code
        this.description = description


    }

}