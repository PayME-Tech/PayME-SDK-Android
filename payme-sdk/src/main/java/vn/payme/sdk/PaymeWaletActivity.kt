package vn.payme.sdk

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import kotlinx.android.synthetic.main.webview_activity.*
import org.json.JSONObject
import vn.payme.sdk.model.Env
import vn.payme.sdk.model.JsObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import android.hardware.camera2.CameraManager
import android.util.DisplayMetrics
import android.view.KeyEvent
import vn.payme.sdk.kyc.CameraKyc
import java.lang.Exception


internal class PaymeWaletActivity : AppCompatActivity() {
    val html: String = "<!DOCTYPE html><html><body>\n" +
            "      <button onclick=\"onClick()\">Click me</button>\n" +
            "      <script>\n" +
            "      function onClick() {\n" +
            "       window.messageHandlers.onFlash(true)" +
            "      }\n" +
            "      </script>\n" +
            "      </body></html>\n"

    private var cameraPermission: PermissionRequest? = null
    private var lottie: LottieAnimationView? = null
    private val CAMERA_PERMISSION_REQUEST = 1111
    private var mCM: String? = null
    private var mUM: ValueCallback<Uri>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null
    private val FCR = 1
    private val FILECHOOSER_RESULTCODE = 1
    private lateinit var cameraManager: CameraManager
    private lateinit var myWebView: WebView


    private fun backScreen(): Unit {

        runOnUiThread {
            onBackPressed()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<Uri>? = null
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {
                    if (null == mUMA) {
                        return
                    }
                    if (intent == null) { //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }
            mUMA!!.onReceiveValue(results)
            mUMA = null
        } else {
            if (requestCode == FCR) {
                if (null == mUM) return
                val result =
                    if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
                mUM!!.onReceiveValue(result)
                mUM = null
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir: File =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    fun openFileChooser(uploadMsg: ValueCallback<Uri?>?) {
        this.openFileChooser(uploadMsg, "*/*")
    }

    fun openFileChooser(
        uploadMsg: ValueCallback<Uri?>?,
        acceptType: String?
    ) {
        this.openFileChooser(uploadMsg, acceptType, null)
    }

    fun openFileChooser(
        uploadMsg: ValueCallback<Uri?>?,
        acceptType: String?,
        capture: String?
    ) {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "*/*"
        this@PaymeWaletActivity.startActivityForResult(
            Intent.createChooser(i, "File Browser"),
            FILECHOOSER_RESULTCODE
        )
    }

    private fun checkCamera() {
        val hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA)
        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 113)

        } else {
            cameraPermission!!.grant(cameraPermission!!.resources)

        }
    }

    fun convertPixelsToDp(px: Float): Float {
        return if (PayME.context != null) {
            val resources = PayME.context.resources
            val metrics = resources.displayMetrics
            px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        } else {
            val metrics = Resources.getSystem().displayMetrics
            px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraPermission!!.grant(cameraPermission!!.resources)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        checkCamera()
        super.onCreate(savedInstanceState)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager


        WebView(applicationContext).clearCache(true)
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();


        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.DKGRAY
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(R.layout.webview_activity)
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        val intent: Intent = Intent(PayME.context, CameraKyc::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        PayME.context?.startActivity(intent)

        myWebView = findViewById(R.id.webview)
//        lottie = findViewById(R.id.loadingWebViewPaymeSDK)
        myWebView.clearCache(true);
        myWebView.clearFormData();
        myWebView.clearHistory();
        myWebView.clearSslPreferences();
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)


        myWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
//                lottie?.visibility = View.INVISIBLE
            }
        })
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );


        val webSettings: WebSettings = myWebView.getSettings()
        webSettings.javaScriptEnabled = true
        webSettings.setAppCacheEnabled(true)
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
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        myWebView.setWebChromeClient(object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                if (mUMA != null) {
                    mUMA!!.onReceiveValue(null)
                }
                mUMA = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(this@PaymeWaletActivity.getPackageManager()) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCM)
                    } catch (ex: IOException) {
                        Log.e("Webview", "Image file creation failed", ex)
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.getAbsolutePath()
                        takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile)
                        )
                    } else {
                        takePictureIntent = null
                    }
                }
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                val intentArray: Array<Intent>
                intentArray = takePictureIntent?.let { arrayOf(it) } ?: arrayOf<Intent>()
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, FCR)
                return true
            }


            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d(
                    "LOG WEB", consoleMessage?.message() + " -- From line "
                            + consoleMessage?.lineNumber() + " of "
                            + consoleMessage?.sourceId()
                );
                return super.onConsoleMessage(consoleMessage)
            }

            // Grant permissions for cam
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionRequest(request: PermissionRequest) {
                cameraPermission = request
                checkCamera()
            }
        })
        val jsObject: JsObject =
            JsObject(back = { backScreen() }, this.supportFragmentManager, cameraManager)
        myWebView.addJavascriptInterface(jsObject, "messageHandlers")
        var action: String = PayME.action.toString()

        var data: JSONObject = JSONObject(
            """{
                      connectToken:  '${PayME.connectToken}',
                      appToken: '${PayME.appToken}',
                      clientInfo: {
                        clientId: '${PayME.clientInfo.deviceId}',
                        platform: 'ANDROID',
                        appVersion: '${PayME.clientInfo.appVersion}', 
                        sdkVesion: '${PayME.clientInfo.sdkVerSion}', 
                        sdkType: 'native',
                        appPackageName: '${PayME.clientInfo.appPackageName}'
                      },
                      partner: 'ANDROID',
                      action:'${action}',
                      amount:${PayME.amount},
                      configColor: ['${PayME.configColor?.get(0)}', '${PayME.configColor?.get(1)}'],
                      partner : {
                        type:'ANDROID',
                        paddingTop:${convertPixelsToDp(statusBarHeight.toFloat())}
                      },
                      actions:{
                        type:${action},
                        amount:${PayME.amount}
                      }
                    }"""
        )

        val encode: String = URLEncoder.encode(data.toString(), "utf-8")
        cookieManager.setAcceptThirdPartyCookies(myWebView, true)
        println("https://sbx-sdk.payme.com.vn/active/${encode}")
        if (PayME.env === Env.SANDBOX) {
            myWebView.loadUrl("https://sbx-sdk.payme.com.vn/active/${encode}")
//            myWebView.loadData(html, "text/html", "UTF-8");


        } else {
            myWebView.loadUrl("https://sdk.payme.com.vn/active/${encode}")
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
        } catch (e: Exception) {
        }

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {

        if (this.myWebView.canGoBack()) {
            this.myWebView.goBack()
        } else {
            finish()
        }
        return true
    }


}
