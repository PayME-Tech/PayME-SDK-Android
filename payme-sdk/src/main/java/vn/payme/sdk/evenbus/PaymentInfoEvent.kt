package vn.payme.sdk.evenbus

import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.model.Info
import vn.payme.sdk.model.Method

class PaymentInfoEvent {
     var cardInfo: CardInfo?

    constructor(cardInfo: CardInfo?){
        this.cardInfo  = cardInfo
    }

}