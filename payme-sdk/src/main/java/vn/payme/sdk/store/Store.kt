package vn.payme.sdk.store

import vn.payme.sdk.enums.Env
import vn.payme.sdk.enums.LANGUAGES

internal class Store {
    companion object{
        var config : Config = Config("","","","",false,Env.SANDBOX,arrayOf<String>("#75255b", "#9d455f"),LANGUAGES.VI)
        var paymentInfo: PaymentInfo =PaymentInfo(null,0,"",null,null,null,"",
            arrayListOf(), arrayListOf(),null,true,"")
        var userInfo: UserInfo = UserInfo(0,
            accountKycSuccess = false,
            accountLoginSuccess = false,
            accountActive = false,
            accessToken = "",
            dataInit = null
        )
         var description = ""
    }
}