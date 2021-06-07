package vn.payme.sdk.evenbus

import vn.payme.sdk.model.CardInfo

class CheckInputAtm(
   val isCheck : Boolean,
   val cardInfo: CardInfo?
) {

}