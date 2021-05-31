package vn.payme.sdk.api

import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.store.Store

internal class AccountApi {

    fun registerClient(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val registerInput: MutableMap<String, Any> = mutableMapOf()
        val query = "mutation ClientMutation(\$registerInput: ClientRegisterInput!) {\n" +
                "  Client {\n" +
                "    Register(input: \$registerInput) {\n" +
                "      clientId\n" +
                "      succeeded\n" +
                "      message\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        variables["registerInput"] = Store.config.clientInfo?.getClientInfo()!!
        params["variables"] = variables
        val request = NetworkRequest(
            PayME.context!!,
            ENV_API.API_FE,
            path,
            Store.userInfo.accessToken!!,
            params,
            ENV_API.IS_SECURITY
        )
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun getAccountInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {

        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val query = "query Query(\$accountPhone: String) {\n" +
                "  Account(phone: \$accountPhone) {\n" +
                "    accountId\n" +
                "    fullname\n" +
                "    alias\n" +
                "    birthday\n" +
                "    avatar\n" +
                "    email\n" +
                "    gender\n" +
                "    isVerifiedEmail\n" +
                "    isWaitingEmailVerification\n" +
                "    phone\n" +
                "    state\n" +
                "    kyc {\n" +
                "      state\n" +
                "      sentAt\n" +
                "      reason\n" +
                "      kycId\n" +
                "      identifyNumber\n" +
                "      details {\n" +
                "        video {\n" +
                "          state\n" +
                "        }\n" +
                "        image {\n" +
                "          state\n" +
                "        }\n" +
                "        face {\n" +
                "          state\n" +
                "        }\n" +
                "        identifyNumber\n" +
                "        issuedAt\n" +
                "      }\n" +
                "    }\n" +
                "    address {\n" +
                "      street\n" +
                "      city {\n" +
                "        identifyCode\n" +
                "        path\n" +
                "        title\n" +
                "      }\n" +
                "      ward {\n" +
                "        identifyCode\n" +
                "        path\n" +
                "        title\n" +
                "      }\n" +
                "      district {\n" +
                "        identifyCode\n" +
                "        path\n" +
                "        title\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        val phone = Store.userInfo.dataInit?.getString("phone")
        variables["accountPhone"] = phone.toString()
        params["variables"] = variables
        val request = NetworkRequest(
            PayME.context!!,
            ENV_API.API_FE,
            path,
            Store.userInfo.accessToken!!,
            params,
            ENV_API.IS_SECURITY
        )
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }

    fun intAccount(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val initInput: MutableMap<String, Any> = mutableMapOf()
        val query = "mutation AccountInitMutation(\$initInput: CheckInitInput) {\n" +
                "    OpenEWallet {\n" +
                "      Init(input: \$initInput) {\n" +
                "        succeeded\n" +
                "        message\n" +
                "        handShake\n" +
                "        accessToken\n" +
                "        kyc {\n" +
                "          kycId\n" +
                "          state\n" +
                "          reason\n" +
                "        }\n" +
                "        phone\n" +
                "        appEnv\n" +
                "        storeName\n" +
                "      }\n" +
                "    }\n" +
                "  }"
        params["query"] = query
        initInput["appToken"] = Store.config.appToken
        initInput["connectToken"] = Store.config.connectToken
        initInput["clientId"] = Store.config.clientId
        variables["initInput"] = initInput
        params["variables"] = variables
        val request = NetworkRequest(
            PayME.context!!,
            ENV_API.API_FE,
            path,
            Store.userInfo.accessToken!!,
            params,
            ENV_API.IS_SECURITY
        )
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
        )

    }
}