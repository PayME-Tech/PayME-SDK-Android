package vn.payme.sdk.model

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import vn.payme.sdk.PayME


class ColorApp {
    var startColor: String = "#08941f"
    var endColor: String = "#0eb92a"
    var backgroundColorRadius: GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.RIGHT_LEFT,
        intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
    )
    var backgroundColorRadiusBorder: GradientDrawable = GradientDrawable()
    var backgroundColorRadiusBorder30: GradientDrawable = GradientDrawable()

    var backgroundColorRadiusAlpha: GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.RIGHT_LEFT,
        intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
    )
    var backgroundColor: GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.RIGHT_LEFT,
        intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
    )
    var backgroundColorRadiusTop: GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.RIGHT_LEFT,
        intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
    )
   var backgroundColorRadiusTopWhite: GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.RIGHT_LEFT,
        intArrayOf(Color.parseColor("#ffffff"), Color.parseColor("#ffffff"))
    )
    fun dbToFloat (f: Float) : Float{
       return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, f,
            PayME.context.getResources().getDisplayMetrics()
        )

    }

    constructor(startColor: String, endColor: String) {
        this.startColor = startColor
        this.endColor = endColor
        this.backgroundColorRadiusBorder.cornerRadius = dbToFloat(15f)
        this.backgroundColorRadiusBorder30.cornerRadius = dbToFloat(15f)
        this.backgroundColorRadiusBorder.setStroke(2, Color.parseColor(startColor))
        this.backgroundColorRadiusBorder30.setStroke(2, Color.parseColor(startColor))
        this.backgroundColorRadius = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        )
        this.backgroundColorRadius.cornerRadius =dbToFloat(30f)
        this.backgroundColor = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        )
        this.backgroundColorRadiusTop = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        )
        backgroundColorRadiusTop.cornerRadii = (floatArrayOf(dbToFloat(30f), dbToFloat(30f), dbToFloat(30f), dbToFloat(30f), 0f, 0f, 0f, 0f))
        backgroundColorRadiusTopWhite.cornerRadii = (floatArrayOf(dbToFloat(35f), dbToFloat(35f), dbToFloat(35f), dbToFloat(35f), 0f, 0f, 0f, 0f))
        backgroundColorRadiusAlpha  = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor)))

        this.backgroundColorRadiusAlpha.alpha = 100
        this.backgroundColorRadiusAlpha.cornerRadius = 60F


    }
}