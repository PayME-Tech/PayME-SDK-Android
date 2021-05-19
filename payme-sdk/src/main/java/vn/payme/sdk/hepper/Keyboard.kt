package vn.payme.sdk.hepper

import android.app.Activity
import android.content.Context
import android.inputmethodservice.Keyboard

import android.view.inputmethod.InputMethodManager
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import vn.payme.sdk.PayME

class Keyboard {


    companion object {
        fun closeKeyboard(context: Context) {
            if(KeyboardVisibilityEvent.isKeyboardVisible(context as Activity?)){
                val inputMethodManager: InputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            }


        }

        fun showKeyboard(context: Context) {
            if(!KeyboardVisibilityEvent.isKeyboardVisible(context as Activity?)) {
                val inputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
        }
    }

}