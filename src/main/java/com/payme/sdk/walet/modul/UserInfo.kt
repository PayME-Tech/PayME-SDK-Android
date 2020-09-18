package com.payme.sdk.walet.modul

class UserInfo {
    var phone : String? = ""
    var fullName : String? = ""
    var address : String? = ""
    var identify : String? = ""


    constructor(phone :String,fullName:String,address:String,identify:String){
        setPhone(phone)
        setFullName(fullName)
        setAddress(address)
        setIdentify(identify)
    }

    internal fun setPhone(phoneNumber: String){
         phone = phoneNumber
    }
    internal fun setFullName(fullNameUser: String){
        fullName = fullNameUser
    }
    internal fun setAddress(addressUser: String){
        address = addressUser
    }
    internal fun setIdentify(identifyUser: String){
        identify = identifyUser
    }
    public fun toJson(): String {
        return "{phone:${phone},fullName:${fullName},address:${address},identify:${identify}}"
    }
}