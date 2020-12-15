package vn.payme.sdk.model

class TypeIdentify {
    var title: String = ""
    var selected: Boolean? = false

    constructor(title: String, selected: Boolean) {
        this.title = title
        this.selected = selected
    }
}