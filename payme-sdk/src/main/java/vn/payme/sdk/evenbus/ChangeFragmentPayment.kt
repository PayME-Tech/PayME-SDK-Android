package vn.payme.sdk.evenbus

import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT

class ChangeFragmentPayment {
     val  typeFragment : TYPE_FRAGMENT_PAYMENT
     val  value : String?
     constructor(typeFragment: TYPE_FRAGMENT_PAYMENT,value: String?){
          this.typeFragment = typeFragment
          this.value = value
     }

}