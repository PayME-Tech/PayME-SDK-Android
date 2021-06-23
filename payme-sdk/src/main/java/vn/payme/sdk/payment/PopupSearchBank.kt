package vn.payme.sdk.payment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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
import vn.payme.sdk.evenbus.ListBankTransfer
import vn.payme.sdk.model.BankTransferInfo


class PopupSearchBank : DialogFragment() {
    lateinit var inputSearch: EditText
    lateinit var buttonClose: ImageView
    lateinit var buttonBack: ImageView
    fun changeAliasCardHolder(alias:String) : String {
        var str = alias

//                    str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, 'a')
//                    str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, 'e')
//                    str = str.replace(/ì|í|ị|ỉ|ĩ/g, 'i')
//                    str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, 'o')
//                    str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, 'u')
//                    str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g, 'y')
//                    str = str.replace(/đ/g, 'd')
//                    str = str.replace(/À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ/g, 'A')
//                    str = str.replace(/È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ/g, 'E')
//                    str = str.replace(/Ì|Í|Ị|Ỉ|Ĩ/g, 'I')
//                    str = str.replace(/Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ/g, 'O')
//                    str = str.replace(/Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ/g, 'U')
//                    str = str.replace(/Ỳ|Ý|Ỵ|Ỷ|Ỹ/g, 'Y')
//                    str = str.replace(/Đ/g, 'D')
//                    // Combining Diacritical Marks
//                    str = str.replace(/\u0300|\u0301|\u0303|\u0309|\u0323/g, '') // huyền, sắc, hỏi, ngã, nặng
//                    str = str.replace(/\u02C6|\u0306|\u031B/g, '') // mũ â (ê), mũ ă, mũ ơ (ư)

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
            R.layout.payment_popup_search_bank,
            container, false
        )

        val flowersAdapter = BankAdapter { flower -> adapterOnClick(flower) }
        val recyclerView: RecyclerView = v.findViewById(R.id.recycler_view)
        inputSearch = v.findViewById(R.id.inputSearch)
        buttonClose = v.findViewById(R.id.buttonClose)
        buttonBack = v.findViewById(R.id.buttonBack)
        onClick()
        inputSearch.addTextChangedListener { text ->
            val listBankNew = arrayListOf<BankTransferInfo>()
            val listBankInfo = EventBus.getDefault().getStickyEvent(ListBankTransfer::class.java)
            listBankInfo.listBankTransferInfo.forEach { bank->
                if (bank.bankName.contains(text.toString(),ignoreCase = true)){
                    listBankNew.add(bank)
                }
            }
            flowersAdapter.submitList(listBankNew as MutableList<BankTransferInfo>)

        }
        val listBankInfo = EventBus.getDefault().getStickyEvent(ListBankTransfer::class.java)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        flowersAdapter.submitList(listBankInfo.listBankTransferInfo as MutableList<BankTransferInfo>)
        recyclerView.adapter = flowersAdapter

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
        EventBus.getDefault().post(bank)
        dismiss()

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        return dialog
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, R.layout.payment_layout, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
    }


}