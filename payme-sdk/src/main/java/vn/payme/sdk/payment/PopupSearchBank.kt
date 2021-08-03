package vn.payme.sdk.payment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.greenrobot.eventbus.EventBus
import vn.payme.sdk.R
import vn.payme.sdk.adapter.BankAdapter
import vn.payme.sdk.enums.TYPE_FRAGMENT_PAYMENT
import vn.payme.sdk.evenbus.ChangeFragmentPayment
import vn.payme.sdk.evenbus.ListBankAtm
import vn.payme.sdk.evenbus.ListBankTransfer
import vn.payme.sdk.hepper.ChangeColorImage
import vn.payme.sdk.model.BankTransferInfo


class PopupSearchBank : DialogFragment() {
    lateinit var inputSearch: EditText
    lateinit var buttonClose: ImageView
    lateinit var buttonBack: ImageView
    lateinit var imageSearchBank: ImageView
    lateinit var containerNotFoundBank: LinearLayout
    lateinit var title: TextView
    var listBankInfo = arrayListOf<BankTransferInfo>()

    fun changeAliasCardHolder(alias:String) : String {
        var str = alias

                    return str

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(
            R.layout.payme_payment_popup_search_bank,
            container, false
        )

        val flowersAdapter = BankAdapter { flower -> adapterOnClick(flower) }
        val recyclerView: RecyclerView = v.findViewById(R.id.recycler_view)
        inputSearch = v.findViewById(R.id.inputSearch)
        buttonClose = v.findViewById(R.id.buttonClose)
        buttonBack = v.findViewById(R.id.buttonBack)
        imageSearchBank = v.findViewById(R.id.imageSearchBank)
        containerNotFoundBank = v.findViewById(R.id.containerNotFoundBank)
        title = v.findViewById(R.id.title)
        val isListBankSupport = arguments?.getBoolean("isListBankSupport")
        val listBanks = EventBus.getDefault().getStickyEvent(ListBankAtm::class.java).listBankATM.filter { bankInfo -> bankInfo.vietQRAccepted }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        ChangeColorImage().changeColor(requireContext(),imageSearchBank,R.drawable.ic_not_found_bank,3)
        if(isListBankSupport != null && isListBankSupport){
            title.setText(getString(R.string.list_bank_support))
            val listBankVietQR : ArrayList<BankTransferInfo> = arrayListOf<BankTransferInfo>()
            for (i in 0 until listBanks.size){
                listBankVietQR.add(BankTransferInfo("","","","",listBanks[i].shortName,"",listBanks[i].swiftCode,""))
            }
            listBankInfo = listBankVietQR
        }else{
            val listBankTransfer = EventBus.getDefault().getStickyEvent(ListBankTransfer::class.java)
            listBankInfo = listBankTransfer.listBankTransferInfo
        }

        flowersAdapter.submitList( this.listBankInfo as MutableList<BankTransferInfo>)
        recyclerView.adapter = flowersAdapter
        if(listBankInfo.size==0){
            containerNotFoundBank.visibility = View.VISIBLE
        }else{
            containerNotFoundBank.visibility = View.GONE
        }
        inputSearch.addTextChangedListener { text ->
            val listBankNew = arrayListOf<BankTransferInfo>()
            listBankInfo.forEach { bank->
                if (bank.bankName.contains(text.toString(),ignoreCase = true)){
                    listBankNew.add(bank)
                }
            }
            if(listBankNew.size==0){
                containerNotFoundBank.visibility = View.VISIBLE
            }else{
                containerNotFoundBank.visibility = View.GONE

            }
            flowersAdapter.submitList(listBankNew as MutableList<BankTransferInfo>)

        }
        onClick()

        return  v
    }
    private fun onClick(){
        buttonBack.setOnClickListener {
            dismiss()
        }
        buttonClose.setOnClickListener {
            EventBus.getDefault()
                .post(ChangeFragmentPayment(TYPE_FRAGMENT_PAYMENT.CLOSE_PAYMENT, null))
        }

    }
    private fun adapterOnClick(bank: BankTransferInfo) {
        val isListBankSupport = arguments?.getBoolean("isListBankSupport")
        if(isListBankSupport ==null ){
            EventBus.getDefault().post(bank)
            dismiss()
        }


    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        return dialog
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, R.layout.payme_payment_layout, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
    }


}