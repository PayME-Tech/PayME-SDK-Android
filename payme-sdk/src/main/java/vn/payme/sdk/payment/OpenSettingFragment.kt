package vn.payme.sdk.payment

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.api.PaymentApi
import vn.payme.sdk.component.Button
import vn.payme.sdk.component.PinView
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.MyEven
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.hepper.Keyboard
import vn.payme.sdk.kyc.PermissionCamera
import vn.payme.sdk.kyc.TakePictureAvatarFragment
import vn.payme.sdk.kyc.TakePictureIdentifyFragment
import vn.payme.sdk.kyc.TakeVideoKycFragment
import vn.payme.sdk.store.Store

class OpenSettingFragment(val typeSetting: String) : DialogFragment() {
    private var buttonBackHeaderErrorCamera: ImageView? = null
    lateinit var imageErrorCamera: ImageView
    private var containerErrorCamera: ConstraintLayout? = null
    private var buttonOpenSetting: Button? = null
    private lateinit var textNoteErrorCamera: TextView
    private lateinit var titleErrorCamera: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.payme_error_camera_layout, container, false)
        containerErrorCamera = view.findViewById(R.id.containerErrorCamera)
        imageErrorCamera = view.findViewById(R.id.imageErrorCamera)
        buttonOpenSetting = view.findViewById(R.id.buttonOpenSetting)
        textNoteErrorCamera = view.findViewById(R.id.textNoteErrorCamera)
        titleErrorCamera = view.findViewById(R.id.titleErrorCamera)
        buttonBackHeaderErrorCamera = view.findViewById(R.id.buttonBackHeaderErrorCamera)
        containerErrorCamera?.visibility = View.VISIBLE
        if (typeSetting == "write_external_storage") {
            titleErrorCamera.text = getString(R.string.grant_permission_access_storage)
            textNoteErrorCamera.text = getString(R.string.grant_permission_access_storage_desc)
        }
        ChangeColorImage().changeColor(
            requireContext(),
            imageErrorCamera,
            R.drawable.icon_error_camera,
            3
        )

        buttonOpenSetting!!.setOnClickListener {
            PermissionCamera().openSetting(requireActivity())
        }
        buttonBackHeaderErrorCamera!!.setOnClickListener {
            dismiss()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if ((ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                    && typeSetting == "camera") || (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    == PackageManager.PERMISSION_GRANTED
                    && typeSetting == "write_external_storage")
        ) {
            dismiss()
        }
    }

}