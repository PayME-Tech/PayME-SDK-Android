package vn.payme.sdk

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.webkit.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONObject
import vn.payme.sdk.api.CryptoAES
import vn.payme.sdk.api.CryptoRSA
import vn.payme.sdk.cardmodules.ScanActivity
import vn.payme.sdk.cardmodules.ScanActivityImpl
import vn.payme.sdk.component.Button
import vn.payme.sdk.credit.CameraTakeProfileCreditActivity
import vn.payme.sdk.enums.Env
import vn.payme.sdk.enums.PAY_CODE
import vn.payme.sdk.enums.TypeCallBack
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.kyc.PermissionCamera
import vn.payme.sdk.model.JsObject
import vn.payme.sdk.payment.OpenSettingFragment
import vn.payme.sdk.store.Store
import java.net.URLEncoder
import kotlin.random.Random


class PayMEOpenSDKPopup : DialogFragment() {
    private lateinit var loading: ConstraintLayout
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var myWebView: WebView
    private var buttonBack: Button? = null
    private var buttonNext: Button? = null
    private lateinit var buttonClose: ImageView
    lateinit var containerErrorNetwork: ConstraintLayout
    lateinit var header: ConstraintLayout
    private var checkTimeoutLoadWebView = false
    var domain = ""
    private var enableSetting = false
    private var enableSettingCamera = false
    private var clickOpenSettings = false

    private val permissionCamera: PermissionCamera = PermissionCamera()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        PayMEOpenSDKPopup.isVisible = true
        setStyle(STYLE_NO_FRAME, R.style.DialogStyle);
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (isVisible) return
        super.show(manager, tag)
    }

    companion object {
        var image: String = ""
        var isVisible = false
    }

    private fun backScreen(): Unit {
        dismiss()
    }

    private fun openSetting() {
        if (enableSetting) {
            clickOpenSettings = true
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
        }
    }

    private fun checkPermissionAndroidManifest() {
        val permission = requireContext()
            .packageManager
            .getPackageInfo(
                requireContext().packageName,
                PackageManager.GET_PERMISSIONS
            ).requestedPermissions
        var check = false
        for (i in 0 until permission!!.size) {
            if (permission[i] == "android.permission.READ_CONTACTS") {
                check = true
            }
        }
        if (check) {
            getContacts()
        } else {
            val injectedJS = "       const script = document.createElement('script');\n" +
                    "          script.type = 'text/javascript';\n" +
                    "          script.async = true;\n" +
                    "          script.text = 'onContacts(${null})';\n" +
                    "          document.body.appendChild(script);\n" +
                    "          true; // note: this is required, or you'll sometimes get silent failures\n"
            val injectedJSPermission = "       const script = document.createElement('script');\n" +
                    "          script.type = 'text/javascript';\n" +
                    "          script.async = true;\n" +
                    "          script.text = 'onPermission(false)';\n" +
                    "          document.body.appendChild(script);\n" +
                    "          true; // note: this is required, or you'll sometimes get silent failures\n"
            requireActivity().runOnUiThread {
                myWebView.evaluateJavascript(
                    "(function() {\n$injectedJSPermission;\n})();",
                    null
                )
                myWebView.evaluateJavascript("(function() {\n$injectedJS;\n})();", null)
            }
        }
    }

    private fun getContacts() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_CONTACTS
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            val contacts = JSONArray()
            val cr: ContentResolver = requireContext().contentResolver
            val cur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null
            )

            if (cur?.count ?: 0 > 0) {
                while (cur != null && cur.moveToNext()) {
                    val name = cur.getString(
                        cur.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        )
                    )
                    val phoneNumber =
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            .replace("[^0-9]".toRegex(), "")
                    val phone = JSONObject("""{name:"$name",phone:"$phoneNumber"}""")
                    contacts.put(phone)
                }
            }
            cur?.close()


//            val cur = requireContext().contentResolver.query(
//                ContactsContract.Contacts.CONTENT_URI,
//                null,
//                null,
//                null,
//                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
//            )
//            if (cur?.count ?: 0 > 0) {
//                while (cur != null && cur.moveToNext()) {
//                    val hasPhoneNumber: Int =
//                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
//                            .toInt()
//                    if (hasPhoneNumber > 0) {
//                        val contactId =
//                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
//                        val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//                        val phoneCursor = requireContext().contentResolver.query(
//                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                            null,
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
//                            arrayOf<String?>(contactId),
//                            null
//                        )
//                        if (phoneCursor!!.moveToNext()) {
//                            val phoneNumber=
//                                phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                            val phone = JSONObject("""{name:"${name}",phone:"${phoneNumber}"}""")
//                            contacts.put(phone)
//                        }
//                        phoneCursor.close()
//                    }
//                }
//            }
//            cur?.close()
            val injectedJS = "       const script = document.createElement('script');\n" +
                    "          script.type = 'text/javascript';\n" +
                    "          script.async = true;\n" +
                    "          script.text = 'onContacts(${contacts})';\n" +
                    "          document.body.appendChild(script);\n" +
                    "          true; // note: this is required, or you'll sometimes get silent failures\n"
            val injectedJSPermission = "       const script = document.createElement('script');\n" +
                    "          script.type = 'text/javascript';\n" +
                    "          script.async = true;\n" +
                    "          script.text = 'onPermission(true)';\n" +
                    "          document.body.appendChild(script);\n" +
                    "          true; // note: this is required, or you'll sometimes get silent failures\n"
            requireActivity().runOnUiThread {
                myWebView.evaluateJavascript(
                    "(function() {\n$injectedJSPermission;\n})();",
                    null
                )
                myWebView.evaluateJavascript("(function() {\n$injectedJS;\n})();", null)
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
        }
    }

    private fun takeImage(): Unit {
//        val intent = Intent(this, CameraTakeProfileCreditActivity::class.java)
//        this?.startActivity(intent)
        val cameraTakeProfileCreditActivity = CameraTakeProfileCreditActivity()
        cameraTakeProfileCreditActivity.show(parentFragmentManager, null)
    }

    fun convertPixelsToDp(px: Float): Float {
        return run {
            val resources = PayME.context.resources
            val metrics = resources.displayMetrics
            px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    private fun copyToClipboard(text: String) {
        try {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Copy to clip", text)
            clipboard.setPrimaryClip(clipData)
            val injectedJS = "       const script = document.createElement('script');\n" +
                    "          script.type = 'text/javascript';\n" +
                    "          script.async = true;\n" +
                    "          script.text = 'onResultCopy(true)';\n" +
                    "          document.body.appendChild(script);\n" +
                    "          true; // note: this is required, or you'll sometimes get silent failures\n"
            requireActivity().runOnUiThread {
                myWebView.evaluateJavascript("(function() {\n$injectedJS;\n})();", null)
            }
        } catch (e: Exception) {
            println(e)
            val injectedJS = "       const script = document.createElement('script');\n" +
                    "          script.type = 'text/javascript';\n" +
                    "          script.async = true;\n" +
                    "          script.text = 'onResultCopy(false)';\n" +
                    "          document.body.appendChild(script);\n" +
                    "          true; // note: this is required, or you'll sometimes get silent failures\n"
            requireActivity().runOnUiThread {
                myWebView.evaluateJavascript("(function() {\n$injectedJS;\n})();", null)
            }
        }
    }

    private fun startScanner() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            ScanActivity.warmUp(requireContext())
            val intent = Intent(requireContext(), ScanActivityImpl::class.java)
            startActivityForResult(intent, 51234)
        } else {
            if (enableSettingCamera) {
                val openSettingFragment: OpenSettingFragment = OpenSettingFragment("camera")
                openSettingFragment.show(parentFragmentManager, null)
            } else {
                permissionCamera.requestCameraFragment(requireContext(), this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(
            R.layout.payme_open_wallet_webview,
            container, false
        )

        EventBus.getDefault().register(this)
//        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        WebView(requireContext()).clearCache(true)
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        dialog?.window?.setStatusBarColor(Color.TRANSPARENT);
        dialog?.window?.setBackgroundDrawable(Store.config.colorApp.backgroundColor);
        myWebView = v.findViewById(R.id.webview)
        loadingProgressBar = v.findViewById(R.id.loadingProgressBar)
        loading = v.findViewById(R.id.loading)
        buttonBack = v.findViewById(R.id.buttonBack)
        buttonNext = v.findViewById(R.id.buttonNext)
        header = v.findViewById(R.id.header)
        buttonClose = v.findViewById(R.id.buttonClose)
        containerErrorNetwork = v.findViewById(R.id.containerErrorNetwork)
        myWebView.clearCache(true);
        myWebView.clearFormData();
        myWebView.clearHistory();
        myWebView.clearSslPreferences();
        loadingProgressBar.indeterminateDrawable
            .mutate()
            .setColorFilter(
                Color.parseColor(Store.config.colorApp.startColor),
                PorterDuff.Mode.SRC_ATOP
            )

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        buttonBack?.setOnClickListener {
            dismiss()
        }
        buttonNext?.setOnClickListener {
            containerErrorNetwork.visibility = View.GONE
            loading.visibility = View.VISIBLE
            myWebView.reload()

        }

        buttonBack?.setButtonTypeBorder()

        myWebView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                if (errorCode == -2) {
                    requireActivity().runOnUiThread {
                        loading.visibility = View.GONE
                        containerErrorNetwork?.visibility = View.VISIBLE
                    }
                }
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (loading.visibility != View.GONE) {
                    loading.visibility = View.GONE
                }
                checkTimeoutLoadWebView = true


            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Thread {
                    try {
                        Thread.sleep(30000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    if (!checkTimeoutLoadWebView) {
                        if (isVisible) {
                            requireActivity().runOnUiThread {
                                loading.visibility = View.GONE
                                containerErrorNetwork.visibility = View.VISIBLE
                            }
                        }
                    }
                }.start()
            }
        }


        val webSettings: WebSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.setSupportMultipleWindows(true)
        webSettings.setGeolocationEnabled(true)
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        webSettings.databaseEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.allowContentAccess = true
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.setGeolocationEnabled(true)
        webSettings.loadWithOverviewMode = true
        webSettings.allowFileAccess = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE

        val jsObject: JsObject =
            fragmentManager?.let { fragmentManager ->
                JsObject(
                    back = { backScreen() },
                    showButtonClose = { b ->
                        showButtonClose(b)
                    },
                    onScanQR = {
                        val payme = PayME()
                        payme.openScanQR(
                            parentFragmentManager,
                            PAY_CODE.PAYME,
                            PayME.onSuccess,
                            PayME.onError
                        )
                    },
                    takeImage = { takeImage() },
                    getContact = {
                        checkPermissionAndroidManifest()
                    },
                    openSetting = {
                        openSetting()
                    },
                    onCopyToClipBoard = {
                        copyToClipboard(it)
                    },
                    pressScanCard = {
                        startScanner()
                    },
                    fragmentManager
                )
            }!!
        myWebView.addJavascriptInterface(jsObject, "messageHandlers")
        val action: String = Store.paymentInfo.action.toString()
        val showLog = if (Store.config.showLog) 1 else 0
        val description = Store.paymentInfo.content
        val extraData = Store.paymentInfo.extraData
        val data: JSONObject = JSONObject(
            """{
                      connectToken:  '${Store.config.connectToken}',
                      appToken: '${Store.config.appToken}',
                      publicKey: '${Store.config.publicKey}',
                      privateKey: '${Store.config.appPrivateKey}',
                      language: '${Store.config.language.toString().toLowerCase()}',
                      xApi: '${Store.config.appID}',
                      env: '${Store.config.env.toString()}',
                      showLog: '${showLog}',
                      clientId: '${Store.config.clientId}',
                      amount:${Store.paymentInfo.amount},
                      configColor: ['${Store.config.configColor?.get(0)}', '${
                Store.config.configColor?.get(
                    1
                )
            }'],
                      dataInit:${Store.userInfo.dataInit?.toString()},
                      partner : {
                        type:'ANDROID'
                      },
                      actions:{
                        type:${action},
                        closeWhenDone:${Store.config.closeWhenDone},
                        serviceCode:${Store.paymentInfo.service?.code},
                        amount:${Store.paymentInfo.amount},
                        description:'${description}',
                        extraData:'${extraData}'
                      }
                    }"""
        )

        val cryptoRSA = CryptoRSA()
        val nextValues = Random.nextInt(0, 10000000)
        val encryptKey = nextValues.toString()
        val publicKey = "-----BEGIN PUBLIC KEY-----\n" +
                "MFswDQYJKoZIhvcNAQEBBQADSgAwRwJAUtmXPKzZWoKT0taQFyNecMIxI57EdfpJ\n" +
                "AOznurDJAXrVW0fB9tnem1k6nQmRiUeCzWQ8lkgGitk0rA/+37vWawIDAQAB\n" +
                "-----END PUBLIC KEY-----"
        val xAPIKey = cryptoRSA.encryptWebView(publicKey, encryptKey)
        val cryptoAES = CryptoAES()
        val xAPIData = cryptoAES.encryptAES(encryptKey, data.toString())
        val encode: String = URLEncoder.encode(xAPIData, "utf-8")
        cookieManager.setAcceptThirdPartyCookies(myWebView, true)

        domain = when (Store.config.env) {
            Env.DEV -> {
                "https://dev-sdk.payme.com.vn/"
            }

            Env.SANDBOX -> {
                "https://sbx-sdk.payme.com.vn/"
            }

            Env.STAGING -> {
                "https://staging-sdk.payme.com.vn/"
            }

            else -> {
                "https://sdk.payme.com.vn/"
            }
        }
        buttonClose.setOnClickListener {
            header.visibility = View.GONE
            myWebView.loadUrl(domain)
        }
        myWebView.loadUrl("${domain}activeWithKey?key=${URLEncoder.encode(xAPIKey)}&data=${encode}")


        if (!isNetworkConnected()) {
            containerErrorNetwork.visibility = View.VISIBLE
        }
        return v
    }


    override fun onDestroy() {
        myWebView.removeAllViews();
        myWebView.destroy()
        PayMEOpenSDKPopup.isVisible = false
        EventBus.getDefault().unregister(this);
        super.onDestroy()
    }

    @Subscribe
    fun onText(myEven: MyEven) {

        if (myEven.type == TypeCallBack.onReload) {

            val injectedJS = "       const script = document.createElement('script');\n" +
                    "          script.type = 'text/javascript';\n" +
                    "          script.async = true;\n" +
                    "          script.text = 'onReloadKYC()';\n" +
                    "          document.body.appendChild(script);\n" +
                    "          true; // note: this is required, or you'll sometimes get silent failures\n"
            myWebView.evaluateJavascript("(function() {\n$injectedJS;\n})();", null)
        }
        if (myEven.type == TypeCallBack.onExpired) {
            dismiss()
        }
        if (myEven.type == TypeCallBack.onTakeImageResult) {
            if (myEven.value != null) {
                image = myEven.value.toString()
            }
            val injectedJS = "       const script = document.createElement('script');\n" +
                    "          script.type = 'text/javascript';\n" +
                    "          script.async = true;\n" +
                    "          script.text = 'nativeAndroidSendBase64Image()';\n" +
                    "          document.body.appendChild(script);\n" +
                    "          true; // note: this is required, or you'll sometimes get silent failures\n"
            myWebView.evaluateJavascript("(function() {\n$injectedJS;\n})();", null)
        }
        if (myEven.type == TypeCallBack.onUpdateIdentify) {

            val injectedJS = "       const script = document.createElement('script');\n" +
                    "          script.type = 'text/javascript';\n" +
                    "          script.async = true;\n" +
                    "          script.text = 'onUpdateIdentify()';\n" +
                    "          document.body.appendChild(script);\n" +
                    "          true; // note: this is required, or you'll sometimes get silent failures\n"
            myWebView.evaluateJavascript("(function() {\n$injectedJS;\n})();", null)

        }

    }

    private fun showButtonClose(isShow: Boolean) {
        requireActivity().runOnUiThread {
            if (isShow) {
                header.visibility = View.VISIBLE
            } else {
                header.visibility = View.GONE
            }
        }
    }

    private fun checkRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val valid = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (valid) {
            if (requestCode == permissionCamera.CAMERA_REQUEST_CODE) {
                ScanActivity.warmUp(requireContext())
                val intent = Intent(requireContext(), ScanActivityImpl::class.java)
                startActivityForResult(intent, 51234)
                return
            }
            if (requestCode == 1) {
                getContacts()
                return
            }
        } else {
            if (requestCode == 1) {
                val injectedJSPermission =
                    "       const script = document.createElement('script');\n" +
                            "          script.type = 'text/javascript';\n" +
                            "          script.async = true;\n" +
                            "          script.text = 'onPermission(false)';\n" +
                            "          document.body.appendChild(script);\n" +
                            "          true; // note: this is required, or you'll sometimes get silent failures\n"
                requireActivity().runOnUiThread {
                    myWebView.evaluateJavascript(
                        "(function() {\n$injectedJSPermission;\n})();",
                        null
                    )
                }
            }
            if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(
                    permissions[0]
                )
            ) {
                if (requestCode == 1) {
                    enableSetting = true
                }
                if (requestCode == permissionCamera.CAMERA_REQUEST_CODE) {
                    enableSettingCamera = true
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        if (clickOpenSettings) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_CONTACTS
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                clickOpenSettings = false
                getContacts()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            override fun onBackPressed() {
                if (myWebView.url == domain) {
                    dismiss()
                } else if (myWebView.canGoBack()) {
                    myWebView.goBack()
                } else {
                    dismiss()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51234) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val number = data?.getStringExtra("cardNumber")
                    val result = JSONObject()
                    result.put("cardNumber", number)
                    val injectedJS = "       const script = document.createElement('script');\n" +
                            "          script.type = 'text/javascript';\n" +
                            "          script.async = true;\n" +
                            "          script.text = 'onScanCard($result)';\n" +
                            "          document.body.appendChild(script);\n" +
                            "          true; // note: this is required, or you'll sometimes get silent failures\n"
                    requireActivity().runOnUiThread {
                        myWebView.evaluateJavascript("(function() {\n$injectedJS;\n})();", null)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    Log.d("HIEU", "Scan canceled")
                }
                else -> {
                    Log.d("HIEU", "Scan failed")
                }
            }
        }
    }
}
