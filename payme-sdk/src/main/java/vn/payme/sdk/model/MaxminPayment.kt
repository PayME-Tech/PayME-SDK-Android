package vn.payme.sdk.model

class MaxminPayment {
    var min: Int = 2000
    var max: Int = 100000
    constructor(min: Int,max: Int){
        this.min  = min
        this.max  = max

    }
}