package vn.payme.sdk.model

import android.R.attr.shape
import android.graphics.Color
import android.graphics.drawable.GradientDrawable


class ColorApp {
    var startColor: String = "#08941f"
    var endColor: String = "#0eb92a"
    var backgroundColorRadius: GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
    )
    var backgroundColorRadiusBorder: GradientDrawable = GradientDrawable()
    var backgroundColorRadiusAlpha: GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
    )
    var backgroundColor: GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
    )
    var backgroundColorRadiusTop: GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
    )
   var backgroundColorRadiusTopWhite: GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(Color.parseColor("#ffffff"), Color.parseColor("#ffffff"))
    )

    constructor(startColor: String, endColor: String) {
        this.startColor = startColor
        this.endColor = endColor
        this.backgroundColorRadiusBorder.cornerRadius = 30f
        this.backgroundColorRadiusBorder.setStroke(1, Color.parseColor(startColor))
        this.backgroundColorRadius = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        )
        this.backgroundColorRadius.cornerRadius = 60F
        this.backgroundColor = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        )
        this.backgroundColorRadiusTop = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        )
        backgroundColorRadiusTop.cornerRadii = (floatArrayOf(60f, 60f, 60f, 60f, 0f, 0f, 0f, 0f))
        backgroundColorRadiusTopWhite.cornerRadii = (floatArrayOf(65f, 65f, 65f, 65f, 0f, 0f, 0f, 0f))
        backgroundColorRadiusAlpha  = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor)))

        this.backgroundColorRadiusAlpha.alpha = 100
        this.backgroundColorRadiusAlpha.cornerRadius = 60F


    }
}