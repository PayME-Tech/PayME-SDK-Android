package vn.payme.sdk

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator

class PayMEQRCode : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScan()
    }

    private fun initScan() {
        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            captureActivity = AnyOrientationCaptureActivity::class.java
            setPrompt("")
            setCameraId(0)
            setBeepEnabled(false)
            setOrientationLocked(false)
            initiateScan()
        }
    }
}