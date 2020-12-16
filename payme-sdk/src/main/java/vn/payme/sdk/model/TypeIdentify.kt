package vn.payme.sdk.model

class TypeIdentify {
    var title: String = ""
    var type: String = ""
    var selected: Boolean? = false

    constructor(title: String,type:String, selected: Boolean) {
        this.title = title
        this.type = type
        this.selected = selected
    }
}