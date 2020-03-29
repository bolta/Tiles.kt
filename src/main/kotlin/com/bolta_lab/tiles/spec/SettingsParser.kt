package com.bolta_lab.tiles.spec

import com.bolta_lab.js.engine
import com.bolta_lab.js.evalAs
import com.bolta_lab.js.required
import com.bolta_lab.tiles.RenderingParameterSet
import com.bolta_lab.tiles.Vec2d
import com.bolta_lab.tiles.color.defaultColorGen
import com.bolta_lab.tiles.divider.lrtb
import java.io.Reader
import java.util.*
import javax.script.Bindings

fun parseSettings(script: Reader): RenderingParameterSet {
	val js = engine()
	val settings = js.evalAs<Bindings>(script)

	val size = parseSize(settings.required<Bindings>("size"))

	// TODO 仮
	val divider = lrtb(Vec2d(16.0, 16.0))
	// TODO 仮
	val colors = defaultColorGen(Random())

	return RenderingParameterSet(size, divider, colors)
}

private fun parseSize(array: Bindings): Vec2d = Vec2d(array.required<Number>("0").toDouble(), array.required<Number>("1").toDouble())

