package vn.payme.sdk.model

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import vn.payme.sdk.PayME

class ColorApp {
    var startColor:String = "#08941f"
    var endColor:String = "#0eb92a"
    var backgroundColorRadius : GradientDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor)))
    var backgroundColor: GradientDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor)))

    constructor(startColor: String,endColor:String){
        println("startColor"+startColor)
        println("endColor"+endColor)
        this.startColor = startColor
        this.endColor = endColor
        this.backgroundColorRadius = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor)))
        this.backgroundColorRadius.cornerRadius = 30F
        this.backgroundColor = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor)))

    }
}