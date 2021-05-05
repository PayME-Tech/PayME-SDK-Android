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
    fun getAccountInfo(
        onSuccess: (JSONObject) -> Unit,
        onError: (JSONObject?, Int?, String) -> Unit
    ) {
        val path = "/graphql"
        val params: MutableMap<String, Any> = mutableMapOf()
        val variables: MutableMap<String, Any> = mutableMapOf()
        val query = "query GetAccountInfo(\$accountPhone: String) {\n" +
                "    Account(phone: \$accountPhone) {\n" +
                "      accountId\n" +
                "      fullname\n" +
                "      alias\n" +
                "      phone\n" +
                "      avatar\n" +
                "      email\n" +
                "      gender\n" +
                "      isVerifiedEmail\n" +
                "      isWaitingEmailVerification\n" +
                "      birthday\n" +
                "      address {\n" +
                "        street\n" +
                "        city {\n" +
                "          title\n" +
                "          identifyCode\n" +
                "        }\n" +
                "        district {\n" +
                "          title\n" +
                "          identifyCode\n" +
                "        }\n" +
                "        ward {\n" +
                "          title\n" +
                "          identifyCode\n" +
                "        }\n" +
                "      }\n" +
                "      kyc {\n" +
                "        kycId\n" +
                "        state\n" +
                "        reason\n" +
                "        identifyNumber\n" +
                "        details {\n" +
                "          identifyNumber\n" +
                "          issuedAt\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }"
        params["query"] = query
        val phone = PayME.dataInit?.getString("phone")
        variables["accountPhone"] = phone.toString()
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
                "      appEnv\n" +
                "      handShake\n" +
                "      isExistInMainWallet\n" +
                "      kyc {\n" +
                "        details {\n" +
                "          video {\n" +
                "            video\n" +
                "            state\n" +
                "          }\n" +
                "          state\n" +
                "          sentAt\n" +
                "          reason\n" +
                "          placeOfIssue\n" +
                "          issuedAt\n" +
                "          image {\n" +
                "            back\n" +
                "            front\n" +
                "            state\n" +
                "          }\n" +
                "          identifyType\n" +
                "          identifyNumber\n" +
                "          gender\n" +
                "          fullname\n" +
                "          face {\n" +
                "            face\n" +
                "            state\n" +
                "          }\n" +
                "          birthday\n" +
                "          approvedTimeExpected\n" +
                "          address {\n" +
                "            city {\n" +
                "              path\n" +
                "              title\n" +
                "              identifyCode\n" +
                "            }\n" +
                "            district {\n" +
                "              title\n" +
                "              identifyCode\n" +
                "              path\n" +
                "            }\n" +
                "            street\n" +
                "            ward {\n" +
                "              identifyCode\n" +
                "              path\n" +
                "              title\n" +
                "            }\n" +
                "          }\n" +
                "          accountId\n" +
                "        }\n" +
                "        identifyNumber\n" +
                "        kycId\n" +
                "        reason\n" +
                "        sentAt\n" +
                "        state\n" +
                "      }\n" +
                "      linkedFlow\n" +
                "      message\n" +
                "      phone\n" +
                "      succeeded\n" +
                "      updateToken\n" +
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