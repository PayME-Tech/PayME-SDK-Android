package vn.payme.sdk.model

class BankTransferInfo(
    val bankAccountName :String,
    val bankAccountNumber :String,
    val bankBranch :String,
    val bankCity :String,
    val bankName :String,
    val content :String,
    val swiftCode :String,
    val qrContent :String,
) {

}