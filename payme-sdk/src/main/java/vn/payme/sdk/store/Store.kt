package vn.payme.sdk.store

class Store {
    companion object{
        lateinit var config : Config
         var paymentInfo: PaymentInfo =PaymentInfo(null,0,"",null,null,null,null,null,null,true,true)
         var userInfo: UserInfo = UserInfo(0,false,false,"",null)
    }
}