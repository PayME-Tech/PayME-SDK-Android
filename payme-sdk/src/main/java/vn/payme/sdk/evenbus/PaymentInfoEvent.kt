package vn.payme.sdk.evenbus

import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.model.Info
import vn.payme.sdk.model.Method

class PaymentInfoEvent {
     var cardInfo: CardInfo?
     var fee: Int = 0

    constructor(cardInfo: CardInfo?,fee:Int){
        this.cardInfo  = cardInfo
        this.fee  = fee
    }

}