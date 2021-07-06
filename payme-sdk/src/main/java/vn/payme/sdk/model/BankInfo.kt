package vn.payme.sdk.model

import kotlin.properties.Delegates

class BankInfo {
  lateinit var cardPrefix : String
  var depositable by Delegates.notNull<Boolean>()
  var vietQRAccepted by Delegates.notNull<Boolean>()
  var cardNumberLength : Int = 16
  lateinit  var shortName : String
  lateinit  var swiftCode : String
  constructor(depositable : Boolean,vietQRAccepted : Boolean,cardPrefix : String,cardNumberLength : Int,shortName : String,swiftCode : String){
    this.depositable =depositable
    this.vietQRAccepted =vietQRAccepted
    this.cardPrefix = cardPrefix
    this.cardNumberLength = cardNumberLength
    this.shortName = shortName
    this.swiftCode = swiftCode
  }

}