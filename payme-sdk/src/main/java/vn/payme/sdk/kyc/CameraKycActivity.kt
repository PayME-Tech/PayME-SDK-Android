package vn.payme.sdk.kyc

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.Volley
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.UploadKycApi
import vn.payme.sdk.api.VolleyMultipartRequest
import vn.payme.sdk.component.Button
import vn.payme.sdk.evenbus.ChangeTypeIdentify
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.model.TypeCallBack
import vn.payme.sdk.model.TypeIdentify
import vn.payme.sdk.payment.PopupSelectTypeIdentify
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*

class CameraKycActivity : AppCompatActivity() {
    // Button cho capture ảnh
    private var takePictureButton: ImageView? = null

    // preview camera
    private var textureView: AutoFitTextureView? = null

    companion object {
        private const val TAG = "AndroidCameraApi"

        // kiểm tra trạng thái  ORIENTATION của ảnh đầu ra
        private val ORIENTATIONS = SparseIntArray()
        private const val REQUEST_CAMERA_PERMISSION = 200

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    private var buttonBack: Button? = null
    private var buttonBackHeader: ImageView? = null
    private var buttonBackHeader2: ImageView? = null
    private var buttonNext: Button? = null
    private val preView: ConstraintLayout? = null
    var imagePreView: ImageView? = null
    var layoutConfirm: ConstraintLayout? = null
    private var layoutUpload: ConstraintLayout? = null
    private var saveImage = ""
    private var imageFront = ""
    private var imageBackSide = ""
    private var buttonSelectTypeIdentify: ConstraintLayout? = null
    private var cameraId: String? = null
    protected var cameraDevice: CameraDevice? = null
    protected var cameraCaptureSessions: CameraCaptureSession? = null
    protected var captureRequest: CaptureRequest? = null
    protected var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private var textGuiTakePicture: TextView? = null
    private val popupSelectTypeIndentify: PopupSelectTypeIdentify? = null
    private var textTypeIdentify: TextView? = null
    private var typeIdentify = "CMND"
    private var file1: File? = null
    private var file2: File? = null

    // LƯU RA FILE
    private val file: File? = null
    private val mFlashSupported = false
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_fragment)
        EventBus.getDefault().register(this)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        textureView = findViewById<View>(R.id.previewCamera) as AutoFitTextureView

        buttonBack = findViewById<View>(R.id.buttonBack) as Button
        buttonNext = findViewById<View>(R.id.buttonNext) as Button
        buttonBackHeader = findViewById<View>(R.id.buttonBackHeader) as ImageView
        buttonBackHeader2 = findViewById<View>(R.id.buttonBackHeader2) as ImageView
        layoutConfirm = findViewById<View>(R.id.confirm_screen) as ConstraintLayout
        layoutUpload = findViewById<View>(R.id.upLoadKyc) as ConstraintLayout
        textGuiTakePicture = findViewById<View>(R.id.textGuiTakePicture) as TextView

        textTypeIdentify = findViewById<View>(R.id.title_type_identify) as TextView
        buttonSelectTypeIdentify =
            findViewById<View>(R.id.buttonSelectTypeIdentify) as ConstraintLayout

        imagePreView = findViewById<View>(R.id.previewImage) as ImageView
        layoutUpload!!.background = PayME.colorApp.backgroundColor
        assert(textureView != null)
        textureView!!.surfaceTextureListener = textureListener
        takePictureButton = findViewById<View>(R.id.btn_takepicture) as ImageView
        assert(takePictureButton != null)
        takePictureButton!!.setOnClickListener { takePicture() }
        buttonBackHeader2!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
            createCameraPreview()
        }
        buttonBackHeader!!.setOnClickListener { finish() }
        buttonBack!!.setOnClickListener {
            layoutConfirm!!.visibility = View.GONE
            createCameraPreview()
        }
        buttonNext!!.setOnClickListener {
            if (imageFront.length > 0) {
                layoutUpload!!.visibility = View.VISIBLE
                imageBackSide = saveImage
//                imagePreView!!.setImageBitmap(BitmapFactory.decodeFile(imageFront))

                upload1()

            } else {
                textGuiTakePicture?.text = "Mặt sau"
                layoutConfirm!!.visibility = View.GONE
                imageFront = saveImage
                createCameraPreview()
            }
        }
        buttonSelectTypeIdentify?.setOnClickListener {
            val popupSelectTypeIdentify = PopupSelectTypeIdentify()
            popupSelectTypeIdentify.show(this.supportFragmentManager, "ModalBottomSheet")
        }


    }


    var textureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            // Open camera khi ready
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // Transform you image captured size according to the surface width and height, và thay đổi kích thước ảnh
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }
    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            // Camera opened
            Log.e(TAG, "onOpened")
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice!!.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }
    val captureCallbackListener: CaptureCallback = object : CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            Toast.makeText(this@CameraKycActivity, "Saved:$file", Toast.LENGTH_SHORT).show()
            createCameraPreview()
        }
    }

    protected fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    protected fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    protected fun takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null")
            return
        }
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(
                cameraDevice!!.id
            )
            val jpegSizes: Array<Size>? =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                    .getOutputSizes(ImageFormat.JPEG)

            // CAPTURE IMAGE với tuỳ chỉnh kích thước
            var width = 480
            var height = 640
            if (jpegSizes != null && jpegSizes.isNotEmpty()) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }
            val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces: MutableList<Surface> = ArrayList(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView!!.surfaceTexture))
            val captureBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            // kiểm tra orientation tuỳ thuộc vào mỗi device khác nhau như có nói bên trên
            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder.set(
                CaptureRequest.JPEG_ORIENTATION,
                ORIENTATIONS[rotation]
            )
            var nameImage =  "pic"
            if(imageFront.length>0){
                nameImage = "picImageBackSide"
            }else{
                nameImage = "picImageFront"
            }
            val file = File(Environment.getExternalStorageDirectory().toString() + "/${nameImage}.jpg")
            val readerListener: OnImageAvailableListener = object : OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader) {
                    var image: Image? = null
                    try {
                        image = reader.acquireLatestImage()
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer[bytes]
                        save(bytes)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        image?.close()
                    }
                }

                // Lưu ảnh
                @Throws(IOException::class)
                private fun save(bytes: ByteArray) {
                    var output: OutputStream? = null
                    try {
                        output = FileOutputStream(file)
                        output.write(bytes)
                    } finally {
                        output?.close()
                    }
                }
            }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener: CaptureCallback = object : CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    runOnUiThread {
                        layoutConfirm!!.visibility = View.VISIBLE
                        imagePreView!!.setImageBitmap(BitmapFactory.decodeFile(file.path))
                        saveImage = file.path
                    }
                }
            }
            cameraDevice!!.createCaptureSession(
                outputSurfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        try {
                            session.capture(
                                captureBuilder.build(),
                                captureListener,
                                mBackgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    // Khởi tạo camera để preview trong textureview
    protected fun createCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            cameraDevice!!.createCaptureSession(
                Arrays.asList(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        //The camera is already closed
                        if (null == cameraDevice) {
                            return
                        }
                        // When the session is ready, we start displaying the preview.
                        cameraCaptureSessions = cameraCaptureSession
                        updatePreview()
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        Toast.makeText(
                            this@CameraKycActivity,
                            "Configuration change",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        Log.e(TAG, "is camera open")
        try {
            cameraId = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraId!!)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            // Add permission for camera and let user grant the permission
            // Kiểm tra permission với android sdk >= 23
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@CameraKycActivity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }
            manager.openCamera(cameraId!!, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        Log.e(TAG, "openCamera X")
    }

    protected fun updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return")
        }
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions!!.setRepeatingRequest(
                captureRequestBuilder!!.build(),
                null,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        if (null != cameraDevice) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (null != imageReader) {
            imageReader!!.close()
            imageReader = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(
                    this@CameraKycActivity,
                    "Sorry!!!, you can't use this app without granting permission",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera()
        } else {
            textureView!!.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        Log.e(TAG, "onPause")
        //closeCamera();
        stopBackgroundThread()
        super.onPause()
    }

    @Subscribe
    fun onChange(myEven: TypeIdentify) {
        textTypeIdentify?.text = myEven.title
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy()

    }

    private fun upload1() {
        println("REQUESSSSSSSSSSSSSSSSSSSS1IFront${imageFront}")
        val bitmap = BitmapFactory.decodeFile(imageFront)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()

        val queue = Volley.newRequestQueue(this)
        val b = object : VolleyMultipartRequest(
            Method.POST,
            "https://sbx-static.payme.vn/Upload",
            { response ->
                val a = response.data
                val b = String(a, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(b)
                if (jsonObject.getInt("code") == 1000) {
                    val arrayJson = jsonObject.getJSONArray("data")
                    val jsonObject = arrayJson.getJSONObject(0)
                    var path = jsonObject.optString("path")
                    println("REQUESSSSSSSSSSSSSSSSSSSS1patht${path}")

                    upload2(path)

                }else{
                    val message = jsonObject.getString("message")
                    val toast: Toast =
                        Toast.makeText(PayME.context, message, Toast.LENGTH_SHORT)
                    toast.view?.setBackgroundColor(
                        ContextCompat.getColor(
                            PayME.context,
                            R.color.scarlet
                        )
                    )
                }


            },
            { error ->
                val toast: Toast =
                    Toast.makeText(PayME.context, "Kết nối mạng bị sự cố, vui lòng kiểm tra và thử lại. Xin cảm ơn !", Toast.LENGTH_SHORT)
                toast.view?.setBackgroundColor(
                    ContextCompat.getColor(
                        PayME.context,
                        R.color.scarlet
                    )
                )
            }
        ) {
            override fun getByteData(): MutableMap<String, DataPart> {
                val params: MutableMap<String, DataPart> = HashMap()
                params["files"] = DataPart(
                    "file_Payme_Identify_1.jpg",
                    bytes,
                    "image/jpeg"
                )

                return params
            }
        }
        val defaultRetryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        b.retryPolicy = defaultRetryPolicy
        queue.add(b)
    }

    private fun upload2(image1: String) {
        println("REQUESSSSSSSSSSSSSSSSSSSS1IimageBackSide${imageBackSide}")

        val bitmap = BitmapFactory.decodeFile(imageBackSide)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()

        val queue = Volley.newRequestQueue(this)
        val b = object : VolleyMultipartRequest(
            Method.POST,
            "https://sbx-static.payme.vn/Upload",
            { response ->
                val a = response.data
                val b = String(a, StandardCharsets.UTF_8)
                val jsonObject = JSONObject(b)
                if (jsonObject.getInt("code") == 1000) {
                    val arrayJson = jsonObject.getJSONArray("data")
                    val jsonObject = arrayJson.getJSONObject(0)
                    var path = jsonObject.optString("path")
                    val uploadApi = UploadKycApi()
                    uploadApi.uploadKycInfo(typeIdentify,image1, path, onSuccess = {
                        println("REQUESSSSSSSSSSSSSSSSSSSS1patht222${path}")

                        finish()
                        var even: EventBus = EventBus.getDefault()
                        var myEven: MyEven = MyEven(TypeCallBack.onReload,"")
                        even.post(myEven)
                    }, onError = { jsonObject, code, message ->
                        layoutUpload!!.visibility = View.GONE
                        val toast: Toast =
                            Toast.makeText(PayME.context, message, Toast.LENGTH_SHORT)
                        toast.view?.setBackgroundColor(
                            ContextCompat.getColor(
                                PayME.context,
                                R.color.scarlet
                            )
                        )
                        toast.show()


                    })

                }else{
                    val message = jsonObject.getString("message")
                    val toast: Toast =
                        Toast.makeText(PayME.context, message, Toast.LENGTH_SHORT)
                    toast.view?.setBackgroundColor(
                        ContextCompat.getColor(
                            PayME.context,
                            R.color.scarlet
                        )
                    )
                }


            },
            { error ->
                val toast: Toast =
                    Toast.makeText(PayME.context, "Kết nối mạng bị sự cố, vui lòng kiểm tra và thử lại. Xin cảm ơn !", Toast.LENGTH_SHORT)
                toast.view?.setBackgroundColor(
                    ContextCompat.getColor(
                        PayME.context,
                        R.color.scarlet
                    )
                )
            }
        ) {
            override fun getByteData(): MutableMap<String, DataPart> {
                val params: MutableMap<String, DataPart> = HashMap()
                params["files"] = DataPart(
                    "file_Payme_Identify_2.jpg",
                    bytes,
                    "image/jpeg"
                )

                return params
            }
        }
        val defaultRetryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        b.retryPolicy = defaultRetryPolicy
        queue.add(b)
    }
}