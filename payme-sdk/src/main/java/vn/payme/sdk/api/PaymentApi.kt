package vn.payme.sdk.api

import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.enum.TYPE_PAYMENT
import vn.payme.sdk.model.Method
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

internal class PaymentApi {
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
                "      depositable\n" +
                "      enName\n" +
                "      id\n" +
                "      shortName\n" +
                "      swiftCode\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(
            PayME.context!!,
            ENV_API.API_FE,
            path,
            PayME.accessToken!!,
            params,
            ENV_API.IS_SECURITY
        )
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun getSecuriryCode(
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
        createCodeByPasswordInput["clientId"] = PayME.clientId
        createCodeByPasswordInput["password"] = password
        variables["createCodeByPasswordInput"] = createCodeByPasswordInput
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(
            PayME.context!!,
            ENV_API.API_FE,
            path,
            PayME.accessToken!!,
            params,
            ENV_API.IS_SECURITY
        )
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun payment(
        method: Method,
        securityCode: String?,
        cardNumber: String?,
        cardHolder: String?,
        cardDate: String?,
        otp: String?,
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
                "        message\n" +
                "        succeeded\n" +
                "        payment {\n" +
                "          ... on PaymentWalletResponsed {\n" +
                "            accountId\n" +
                "            message\n" +
                "            statePaymentWalletResponsed : state\n" +
                "          }\n" +
                "          ... on PaymentBankCardResponsed {\n" +
                "            html\n" +
                "            message\n" +
                "            statePaymentBankCardResponsed : state\n" +
                "          }\n" +
                "          ... on PaymentLinkedResponsed {\n" +
                "            html\n" +
                "            linkedId\n" +
                "            statePaymentLinkedResponsed : state\n" +
                "            message\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        payInput["clientId"] = PayME.clientId
        payInput["storeId"] = PayME.infoPayment?.storeId!!
        payInput["amount"] = PayME.infoPayment?.amount!!
        payInput["orderId"] = PayME.infoPayment?.orderId!!
        payInput["note"] = PayME.infoPayment?.note!!
        if (PayME.extraData != null) {
            payInput["referExtraData"] = PayME.extraData!!
        }
        if (method.type == TYPE_PAYMENT.WALLET) {
            val wallet: MutableMap<String, Any> = mutableMapOf()
            wallet["active"] = true
            wallet["securityCode"] = securityCode!!
            payment["wallet"] = wallet
        } else if (method.type == TYPE_PAYMENT.BANK_CARD) {

            val bankCard: MutableMap<String, Any> = mutableMapOf()
            bankCard["cardNumber"] = cardNumber!!
            bankCard["cardHolder"] = cardHolder!!
            bankCard["cardDate"] = cardDate!!
            payment["bankCard"] = bankCard
        } else if (method.type == TYPE_PAYMENT.LINKED) {
            val linked: MutableMap<String, Any> = mutableMapOf()
            linked["linkedId"] = method.data?.linkedId!!
            linked["otp"] = otp!!
            linked["envName"] = "MobileApp"
            payment["linked"] = linked

        }

        payInput["payment"] = payment
        variables["payInput"] = payInput

        params["variables"] = variables
        val request = NetworkRequest(
            PayME.context!!,
            ENV_API.API_FE,
            path,
            PayME.accessToken!!,
            params,
            ENV_API.IS_SECURITY
        )
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun getTransferMethods(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
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
                    "          }\n" +
                    "          ... on WalletMethodInfo {\n" +
                    "            accountId\n" +
                    "          }\n" +
                    "        }\n" +
                    "        fee\n" +
                    "        label\n" +
                    "        methodId\n" +
                    "        minFee\n" +
                    "        title\n" +
                    "        type\n" +
                    "      }\n" +
                    "      succeeded\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"
        getPaymentMethodInput["serviceType"] = "OPEN_EWALLET_PAYMENT"
        variables["getPaymentMethodInput"] = getPaymentMethodInput
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(
            PayME.context!!,
            ENV_API.API_FE,
            path,
            PayME.accessToken!!,
            params,
            ENV_API.IS_SECURITY
        )
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
        val request = NetworkRequest(
            PayME.context!!,
            ENV_API.API_FE,
            path,
            PayME.accessToken!!,
            params,
            ENV_API.IS_SECURITY
        )
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
        detectInput["clientId"] = PayME.clientId
        detectInput["qrContent"] = dataQR
        variables["detectInput"] = detectInput
        params["query"] = query
        params["variables"] = variables
        val request = NetworkRequest(
            PayME.context!!,
            ENV_API.API_FE,
            path,
            PayME.accessToken!!,
            params,
            ENV_API.IS_SECURITY
        )
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }


    fun postTransferPVCBVerify(
        transferId: String,
        OTP: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/v1/Transfer/PVCBank/Verify"
        val params: MutableMap<String, Any> = mutableMapOf()
        val data: MutableMap<String, Any> = mutableMapOf()
        data["orderId"] = PayME.orderId!!
        data["content"] = PayME.content!!
        params["connectToken"] = PayME.connectToken
        params["transferId"] = transferId
        params["OTP"] = OTP
        params["destination"] = "AppPartner"
        params["clientInfo"] = PayME.clientInfo.getClientInfo()
        params["data"] = data
        val request = NetworkRequest(
            PayME.context!!,
            ENV_API.API_FE,
            path,
            PayME.accessToken!!,
            params,
            ENV_API.IS_SECURITY
        )
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }
}