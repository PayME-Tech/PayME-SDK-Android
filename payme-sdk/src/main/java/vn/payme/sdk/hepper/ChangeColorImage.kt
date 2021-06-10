package vn.payme.sdk.hepper

import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import com.devs.vectorchildfinder.VectorChildFinder
import com.devs.vectorchildfinder.VectorDrawableCompat
import vn.payme.sdk.store.Store

class ChangeColorImage {
    fun changeColor (context: Context,imageView: ImageView, vectorRes:Int,numberChange:Int){
        val vector = VectorChildFinder(context, vectorRes, imageView)
        for (i in 1..numberChange) {
            addColor(vector,"${i}")
        }
        imageView.invalidate()
    }
   private fun addColor(vector : VectorChildFinder, name:String){
        val path1: VectorDrawableCompat.VFullPath = vector.findPathByName("colorChange${name}")
        path1.fillColor = Color.parseColor(Store.config.colorApp.startColor)
    }
}