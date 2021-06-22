package vn.payme.sdk.api

import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.enums.TYPE_PAYMENT
import vn.payme.sdk.model.CardInfo
import vn.payme.sdk.model.Method
import vn.payme.sdk.store.Store

internal class PaymentApi {
    fun getInfoMerchant(
        storeId:Long,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ){
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val query = "mutation StoreImageMutation(\$getInfoMerchantInput: OpenEWalletGetInfoMerchantInput!) {\n" +
                "  OpenEWallet {\n" +
                "    GetInfoMerchant(input: \$getInfoMerchantInput) {\n" +
                "      storeImage\n" +
                "      storeName\n" +
                "      succeeded\n" +
                "      message\n" +
                "    }\n" +
                "  }\n" +
                "}"
        val getInfoMerchantInput: MutableMap<String, Any> = mutableMapOf()
        getInfoMerchantInput["appId"] = Store.config.appID.toString()
        params["query"] = query
        variables["getInfoMerchantInput"]= getInfoMerchantInput
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, Store.userInfo.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )
    }

    fun checkVisa(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
        ) {
            val path = "/graphql"
            val params: MutableMap<String, Any> = mutableMapOf()
            val variables: MutableMap<String, Any> = mutableMapOf()
        val query = "mutation SucceededMutation(\$getTransactionInfoInput: GetTransactionInfoInput!) {\n" +
                "   OpenEWallet {\n" +
                "     Payment {\n" +
                "       GetTransactionInfo(input: \$getTransactionInfoInput) {\n" +
                "         succeeded\n" +
                "         message\n" +
                "         transaction\n" +
                "         state\n" +
                "         fee\n" +
                "         description\n" +
                "       }\n" +
                "     }\n" +
                "   }\n" +
                " }"
        val getTransactionInfoInput: MutableMap<String, Any> = mutableMapOf()

        getTransactionInfoInput["transaction"] = Store.paymentInfo.transaction!!
        params["query"] = query
        variables["getTransactionInfoInput"]= getTransactionInfoInput
        params["variables"] = variables
            val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, Store.userInfo.accessToken!!, params,ENV_API.IS_SECURITY)
            request.setOnRequestCrypto(
                onError = onError,
                onSuccess = onSuccess,
            )
    }

    fun getListBanks(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val query = "query Query {\n" +
                "  Setting {\n" +
                "    banks {\n" +
                "      cardPrefix\n" +
                "      cardNumberLength\n" +
                "      depositable\n" +
                "      shortName\n" +
                "      swiftCode\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, Store.userInfo.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }
    fun getFee(
        amount:Int,
        method:Method,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val payment: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val getFeeInput: MutableMap<String, Any> = mutableMapOf()
        val query = "mutation GetFeeMutation(\$getFeeInput: GetFeeInput) {\n" +
                "  Utility {\n" +
                "    GetFee(input: \$getFeeInput) {\n" +
                "      fee {\n" +
                "        ... on GeneralFee {\n" +
                "          fee\n" +
                "        }\n" +
                "      }\n" +
                "      message\n" +
                "      state\n" +
                "      succeeded\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        params["variables"] = variables
        getFeeInput["clientId"] = Store.config.clientId
        getFeeInput["serviceType"] = "OPEN_EWALLET_PAYMENT"
        getFeeInput["amount"] = amount
        variables["getFeeInput"] = getFeeInput
        if (method.type == TYPE_PAYMENT.WALLET) {
            val wallet: MutableMap<String, Any> = mutableMapOf()
            wallet["active"] = true
            payment["wallet"] = wallet
        } else if (method.type == TYPE_PAYMENT.BANK_CARD) {
            val bankCard: MutableMap<String, Any> = mutableMapOf()
            bankCard["cardNumber"] = ""
            payment["bankCard"] = bankCard
        } else if (method.type == TYPE_PAYMENT.LINKED) {
            val linked: MutableMap<String, Any> = mutableMapOf()
            linked["linkedId"] = method.data?.linkedId!!.toBigInteger().toDouble()
            linked["envName"] = "MobileApp"
            payment["linked"] = linked
        }else if(method.type==TYPE_PAYMENT.CREDIT_CARD){
            val creditCard: MutableMap<String, Any> = mutableMapOf()
            creditCard["cardNumber"] = ""
            creditCard["expiredAt"] = ""
            creditCard["cvv"] = ""
            payment["creditCard"] = creditCard
        }else if(method.type==TYPE_PAYMENT.BANK_TRANSFER){
            val bankTransfer: MutableMap<String, Any> = mutableMapOf()
            bankTransfer["active"] = true
            payment["bankTransfer"] = bankTransfer
        }

        getFeeInput["payment"] = payment
        val accessToken = if(Store.userInfo.accountKycSuccess)   Store.userInfo.accessToken!! else ""

        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path,accessToken, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }
    fun getSettings(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val listKey = arrayListOf<String>("limit.param.amount.payment","limit.param.amount.all","service.main.visible","kyc.mode.enable")
        val query = "query Query(\$configsAppId: String, \$configsKeys: [String]) {\n" +
                "  Setting {\n" +
                "    configs(appId: \$configsAppId, keys: \$configsKeys) {\n" +
                "      key\n" +
                "      value\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        params["variables"] = variables
        variables["configsAppId"] = Store.config.appID.toString()
        variables["configsKeys"] = listKey
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, Store.userInfo.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }
    fun  detectCardHolder(
        swiftCode:String,
        cardNumber:String,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ){
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val getBankNameInput: MutableMap<String, Any> = mutableMapOf()
        val query = "mutation GetBankNameMutation(\$getBankNameInput: AccountBankInfoInput) {\n" +
                "  Utility {\n" +
                "    GetBankName(input: \$getBankNameInput) {\n" +
                "      accountName\n" +
                "      message\n" +
                "      succeeded\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        getBankNameInput["swiftCode"] = swiftCode
        getBankNameInput["cardNumber"] = cardNumber
        getBankNameInput["type"] = "CARD"
        variables["getBankNameInput"] = getBankNameInput
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, Store.userInfo.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )
    }

    fun getSecurityCode(
        password: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val createCodeByPasswordInput: MutableMap<String, Any> = mutableMapOf()
        val query =
            "mutation CreateCodeByPasswordMutation(\$createCodeByPasswordInput: CreateSecurityCodeByPassword!) {\n" +
                    "  Account {\n" +
                    "    SecurityCode {\n" +
                    "      CreateCodeByPassword(input: \$createCodeByPasswordInput) {\n" +
                    "        message\n" +
                    "        securityCode\n" +
                    "        succeeded\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"
        createCodeByPasswordInput["clientId"] = Store.config.clientId
        createCodeByPasswordInput["password"] = password
        variables["createCodeByPasswordInput"] = createCodeByPasswordInput
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, Store.userInfo.accessToken!!, params,ENV_API.IS_SECURITY)

        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun payment(
        method: Method,
        securityCode: String?,
        cardInfo: CardInfo?,
        otp: String?,
        transaction: String?,
        recheck: Boolean?,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val payInput: MutableMap<String, Any> = mutableMapOf()
        val payment: MutableMap<String, Any> = mutableMapOf()

        val query = "mutation PayMutation(\$payInput: OpenEWalletPaymentPayInput!) {\n" +
                "  OpenEWallet {\n" +
                "    Payment {\n" +
                "      Pay(input: \$payInput) {\n" +
                "        history {\n" +
                "          payment {\n" +
                "            transaction\n" +
                "          }\n" +
                "        }\n" +
                "        message\n" +
                "        payment {\n" +
                "          ... on PaymentWalletResponsed {\n" +
                "            message\n" +
                "            statePaymentWalletResponsed : state\n" +
                "          }\n" +
                "          ... on PaymentBankCardResponsed {\n" +
                "            html\n" +
                "            message\n" +
                "            statePaymentBankCardResponsed : state\n" +
                "          }\n" +
                "          ... on PaymentCreditCardResponsed {\n" +
//                "            html\n" +
                "            message\n" +
                "            statePaymentCreditCardResponsed : state\n" +
                "          }\n" +
                "          ... on PaymentBankTransferResponsed {\n" +
                "            bankList {\n" +
                "              bankAccountName\n" +
                "              bankBranch\n" +
                "              bankCity\n" +
                "              bankName\n" +
                "              content\n" +
                "              swiftCode\n" +
                "              bankAccountNumber\n" +
                "              fastSupport\n" +
                "            }\n" +
                "            message\n" +
                "            statePaymentBankTransferResponsed : state\n" +
                "            paymentInfo {\n" +
                "              fee\n" +
                "              amount\n" +
                "              bankTransaction\n" +
                "              createdAt\n" +
                "              description\n" +
                "              id\n" +
                "              reason\n" +
                "              state\n" +
                "              total\n" +
                "              transaction\n" +
                "            }\n" +
                "        }\n" +
                "          ... on PaymentLinkedResponsed {\n" +
                "            html\n" +
                "            linkedId\n" +
                "            message\n" +
                "            statePaymentLinkedResponsed : state\n" +
                "            transaction\n" +
                "          }\n" +
                "         \n" +
                "        }\n" +
                "        succeeded\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        payInput["clientId"] = Store.config.clientId
        payInput["storeId"] = Store.paymentInfo.infoPayment?.storeId!!
        payInput["amount"] = Store.paymentInfo.infoPayment?.amount!!
        payInput["orderId"] = Store.paymentInfo.infoPayment?.orderId!!
        payInput["note"] = Store.paymentInfo.infoPayment?.note!!
        if (Store.paymentInfo.infoPayment?.referExtraData != null) {
            payInput["referExtraData"] = Store.paymentInfo.infoPayment?.referExtraData!!
        }
        if (method.type == TYPE_PAYMENT.WALLET) {
            val wallet: MutableMap<String, Any> = mutableMapOf()
            wallet["active"] = true
            wallet["securityCode"] = securityCode!!
            payment["wallet"] = wallet
        } else if (method.type == TYPE_PAYMENT.BANK_CARD) {
            val bankCard: MutableMap<String, Any> = mutableMapOf()
            bankCard["cardNumber"] = cardInfo?.cardNumber!!
            bankCard["cardHolder"] = cardInfo?.cardHolder!!
            bankCard["issuedAt"] = cardInfo?.cardDate!!
            payment["bankCard"] = bankCard
        } else if(method.type==TYPE_PAYMENT.CREDIT_CARD){
            val creditCard: MutableMap<String, Any> = mutableMapOf()
            creditCard["cardNumber"] = cardInfo?.cardNumber!!
            creditCard["expiredAt"] = cardInfo?.cardDateView
            creditCard["cvv"] = cardInfo?.cvv!!
            payment["creditCard"] = creditCard
        } else if(method.type==TYPE_PAYMENT.BANK_TRANSFER){
            val bankTransfer: MutableMap<String, Any> = mutableMapOf()
            bankTransfer["active"] = true
            bankTransfer["recheck"] = recheck!!
            payment["bankTransfer"] = bankTransfer
        } else if (method.type == TYPE_PAYMENT.LINKED) {
            val linked: MutableMap<String, Any> = mutableMapOf()
            linked["linkedId"] = method.data?.linkedId!!.toBigInteger().toDouble()
            if(otp!=null){
                linked["otp"] = otp
            }
            linked["envName"] = "MobileApp"
            payment["linked"] = linked
        }
        payInput["payment"] = payment

        if(transaction!=null){
            payInput["transaction"] = transaction
        }
        variables["payInput"] = payInput
        params["variables"] = variables
        val accessToken = if(Store.userInfo.accountKycSuccess)   Store.userInfo.accessToken!! else ""
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, accessToken, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun getTransferMethods(
        storeId:Long,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val extraData: MutableMap<String, Any> = mutableMapOf()
        val getPaymentMethodInput: MutableMap<String, Any> = mutableMapOf()
        val query =
            "mutation GetPaymentMethodMutation(\$getPaymentMethodInput: PaymentMethodInput) {\n" +
                    "  Utility {\n" +
                    "    GetPaymentMethod(input: \$getPaymentMethodInput) {\n" +
                    "      message\n" +
                    "      methods {\n" +
                    "        data {\n" +
                    "          ... on LinkedMethodInfo {\n" +
                    "            linkedId\n" +
                    "            swiftCode\n" +
                    "            issuer\n" +
                    "          }\n" +
                    "          ... on WalletMethodInfo {\n" +
                    "            accountId\n" +
                    "          }\n" +
                    "        }\n" +
                    "        fee\n" +
                    "        label\n" +
                    "        methodId\n" +
                    "        minFee\n" +
                    "        feeDescription\n" +
                    "        title\n" +
                    "        type\n" +
                    "      }\n" +
                    "      succeeded\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"
        getPaymentMethodInput["serviceType"] = "OPEN_EWALLET_PAYMENT"
        extraData["storeId"] = storeId
        getPaymentMethodInput["extraData"] = extraData
        variables["getPaymentMethodInput"] = getPaymentMethodInput
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, Store.userInfo.accessToken!!, params,ENV_API.IS_SECURITY)

        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun getBalance(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val query = "query Query {\n" +
                "  Wallet {\n" +
                "    balance\n" +
                "    cash\n" +
                "    lockCash\n" +
                "  }\n" +
                "}"
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, Store.userInfo.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }


    fun postCheckDataQr(
        dataQR: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val detectInput: MutableMap<String, Any> = mutableMapOf()
        val query = "mutation ActionMutation(\$detectInput: OpenEWalletPaymentDetectInput!) {\n" +
                "  OpenEWallet {\n" +
                "    Payment {\n" +
                "      Detect(input: \$detectInput) {\n" +
                "        action\n" +
                "        amount\n" +
                "        message\n" +
                "        note\n" +
                "        orderId\n" +
                "        storeId\n" +
                "        succeeded\n" +
                "        type\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
        detectInput["clientId"] = Store.config.clientId
        detectInput["qrContent"] = dataQR
        variables["detectInput"] = detectInput
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, Store.userInfo.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }



}