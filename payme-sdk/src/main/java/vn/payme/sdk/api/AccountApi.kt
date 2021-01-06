package vn.payme.sdk.api

import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.model.Env

internal class  AccountApi {

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
        variables["registerInput"] = PayME.clientInfo.getClientInfo()
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, PayME.accessToken!!, params,ENV_API.IS_SECURITY)
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
        val query = "mutation InitMutation(\$initInput: CheckInitInput) {\n" +
                "  OpenEWallet {\n" +
                "    Init(input: \$initInput) {\n" +
                "      accessToken\n" +
                "      handShake\n" +
                "      isExistInMainWallet\n" +
                "      kyc {\n" +
                "        state\n" +
                "      }\n" +
                "      succeeded\n" +
                "    }\n" +
                "  }\n" +
                "}"
        params["query"] = query
        initInput["appToken"] = PayME.appToken
        initInput["connectToken"] = PayME.connectToken
        initInput["clientId"] = PayME.clientId
        variables["initInput"] = initInput
        params["variables"] = variables
        val request = NetworkRequest(PayME.context!!, ENV_API.API_FE, path, PayME.accessToken!!, params,ENV_API.IS_SECURITY)
        request.setOnRequestCrypto(
            onError = onError,
            onSuccess = onSuccess,
           )

    }
}