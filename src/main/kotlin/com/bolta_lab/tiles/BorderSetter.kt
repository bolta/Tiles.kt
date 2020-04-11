package com.bolta_lab.tiles

import com.bolta_lab.tiles.color.Color
import com.bolta_lab.tiles.color.Rgb
import java.lang.Float.max
import java.lang.Float.min

typealias BorderSetter = (Color) -> BorderSettings

data class BorderSettings(
		val borderExists: Boolean = true,
		val color: Color = Rgb(0f, 0f, 0f),
		val width: Float = 1f)

fun none(): BorderSetter = { BorderSettings(borderExists = false) }

fun times(coeff: Float, width: Float): BorderSetter = { color ->
	fun applyTimes(component: Float) = min(max(0f, component * coeff), 1f)

	BorderSettings(
			color = Rgb(applyTimes(color.red), applyTimes(color.green), applyTimes(color.blue)),
			width = width)
}
