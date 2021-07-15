package vn.payme.sdk.hepper

import java.util.regex.Pattern


enum class CardType {
    UNKNOWN, VISA, MASTERCARD, JCB;

    private var pattern: Pattern?

    constructor() {
        pattern = null
    }

    constructor(pattern: String) {
        this.pattern = Pattern.compile(pattern)
    }

    companion object {
        fun detect(cardNumber: String?): CardType {
            if (cardNumber?.length!! > 0) {
                if (cardNumber?.subSequence(0, 1) == "3") return JCB
                if (cardNumber?.subSequence(0, 1) == "5") return MASTERCARD
                if (cardNumber?.subSequence(0, 1) == "4") return VISA
            }


            return UNKNOWN
        }
    }
}