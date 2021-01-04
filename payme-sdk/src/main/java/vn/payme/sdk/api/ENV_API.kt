package vn.payme.sdk.api

import vn.payme.sdk.PayME
import vn.payme.sdk.model.Env

class ENV_API() {
    companion object {
        private fun getAPIStatic(): String {
            if (PayME.env == Env.SANDBOX) {
                return "https://sbx-static.payme.vn/Upload"
            } else if (PayME.env == Env.PRODUCTION) {
                return "https://static.payme.vn/Upload"
            } else if (PayME.env == Env.DEV) {
                return "https://sbx-static.payme.vn/Upload"
            }
            return "https://sbx-static.payme.vn/Upload"
        }

        private fun getAPIfe(): String {
            if (PayME.env == Env.SANDBOX) {
                return "https://sbx-fe.payme.vn/"
            } else if (PayME.env == Env.PRODUCTION) {
                return "https://fe.payme.vn/"
            } else if (PayME.env == Env.DEV) {
                return "https://dev-fe.payme.net.vn"
            }
            return "https://dev-fe.payme.net.vn/"
        }

        private fun getSecurity(): Boolean {
            if (PayME.env == Env.SANDBOX) {
                return true
            } else if (PayME.env == Env.PRODUCTION) {
                return true
            } else if (PayME.env == Env.DEV) {
                return false
            }
            return false
        }

        var API_STATIC: String = getAPIStatic()
        var API_FE: String = getAPIfe()
        var IS_SECURITY: Boolean = getSecurity()
        fun updateEnv() {
            API_STATIC = getAPIStatic()
            API_FE = getAPIfe()
            IS_SECURITY = getSecurity()
        }

    }

}