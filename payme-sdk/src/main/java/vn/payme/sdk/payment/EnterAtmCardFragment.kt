package vn.payme.sdk.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import vn.payme.sdk.PayME
import vn.payme.sdk.R
import vn.payme.sdk.component.Button

class EnterAtmCardFragment :Fragment() {
    private lateinit var buttonSubmit: Button
    private lateinit var contentButtonChangeMethod: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater?.inflate(R.layout.enter_atm_card_fragment, container, false)
        buttonSubmit = view!!.findViewById(R.id.buttonSubmit)
        contentButtonChangeMethod = view!!.findViewById(R.id.contentButtonChangeMethod)
        contentButtonChangeMethod.background  = PayME.colorApp.backgroundColorRadiusAlpha
        buttonSubmit.setOnClickListener {
        }
        return view
    }

}