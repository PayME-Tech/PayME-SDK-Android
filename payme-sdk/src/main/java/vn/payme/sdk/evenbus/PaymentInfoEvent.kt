package vn.payme.sdk.evenbus

import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.model.Info
import vn.payme.sdk.model.Method

class PaymentInfoEvent {
     var infoTop: ArrayList<Info>
     var infoBottom: ArrayList<Info>
     var cardInfo: CardInfo?

    constructor(infoTop : ArrayList<Info>, infoBottom : ArrayList<Info>,cardInfo: CardInfo?){
        this.infoTop = infoTop
        this.infoBottom = infoBottom
        this.cardInfo  = cardInfo
    }

}