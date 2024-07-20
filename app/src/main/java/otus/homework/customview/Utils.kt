package otus.homework.customview

import android.content.res.Resources.getSystem

val Int.asDp: Int get() = (this * getSystem().displayMetrics.density).toInt()
val Int.asSp: Int get() = (this * getSystem().displayMetrics.scaledDensity).toInt()