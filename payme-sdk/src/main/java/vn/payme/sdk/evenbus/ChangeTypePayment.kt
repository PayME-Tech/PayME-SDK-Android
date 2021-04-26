package vn.payme.sdk.evenbus

import org.json.JSONObject

class ChangeTypePayment {
    var value: String?
    var type: String?
    var data: JSONObject? = null

    constructor(type: String, value: String, data: JSONObject?) {
        this.type = type
        this.value = value
        this.data = data
    }
}