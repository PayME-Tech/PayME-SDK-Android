package vn.payme.sdk.store

import vn.payme.sdk.enums.Env
import vn.payme.sdk.enums.LANGUAGES

public class Store {
    companion object{
         var config : Config = Config("","","","",false,Env.SANDBOX,arrayOf<String>("#75255b", "#9d455f"),LANGUAGES.VN,"")
         var paymentInfo: PaymentInfo =PaymentInfo(null,0,"",null,null,null,null,null,null,true,true)
         var userInfo: UserInfo = UserInfo(0,false,false,"",null)
         var description = ""
    }
}