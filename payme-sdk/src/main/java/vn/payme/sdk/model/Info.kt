package vn.payme.sdk.model

class Info {
    var label : String? = null
    var value : String? = null
    var labelColor : Int? = null
    var valueColor : Int? = null
    var isEnd : Boolean = false
    var valueTextSize : Float = 14f
    constructor(label : String?,value : String?,labelColor : Int?,valueColor :Int?,isEnd:Boolean){
        this.label = label
        this.value = value
        this.labelColor = labelColor
        this.valueColor = valueColor
        this.isEnd = isEnd
    }
}