package vn.payme.sdk.api

import org.json.JSONObject
import vn.payme.sdk.PayME

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
        val query = "mutation Mutation(\$initInput: CheckInitInput) {\n" +
                "  OpenEWallet {\n" +
                "    Init(input: \$initInput) {\n" +
                "      accessToken\n" +
                "      appEnv\n" +
                "      handShake\n" +
                "      isExistInMainWallet\n" +
                "      kyc {\n" +
                "        details {\n" +
                "          video {\n" +
                "            state\n" +
                "            video\n" +
                "          }\n" +
                "          accountId\n" +
                "          address {\n" +
                "            city {\n" +
                "              identifyCode\n" +
                "              path\n" +
                "              title\n" +
                "            }\n" +
                "            district {\n" +
                "              path\n" +
                "              identifyCode\n" +
                "              title\n" +
                "            }\n" +
                "            street\n" +
                "            ward {\n" +
                "              identifyCode\n" +
                "              path\n" +
                "              title\n" +
                "            }\n" +
                "          }\n" +
                "          approvedTimeExpected\n" +
                "          birthday\n" +
                "          face {\n" +
                "            face\n" +
                "            state\n" +
                "          }\n" +
                "          fullname\n" +
                "          gender\n" +
                "          identifyNumber\n" +
                "          identifyType\n" +
                "          image {\n" +
                "            back\n" +
                "            front\n" +
                "            state\n" +
                "          }\n" +
                "          issuedAt\n" +
                "          placeOfIssue\n" +
                "          reason\n" +
                "          sentAt\n" +
                "          state\n" +
                "        }\n" +
                "        identifyNumber\n" +
                "        kycId\n" +
                "        reason\n" +
                "        sentAt\n" +
                "        state\n" +
                "      }\n" +
                "      message\n" +
                "      phone\n" +
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