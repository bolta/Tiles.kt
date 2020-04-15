package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.*
import java.lang.Math.sqrt
import kotlin.coroutines.experimental.buildSequence
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun radial(count: Int) = fun (figure: Figure) = buildSequence {
	if (count < 2) {
		yield(figure)
		return@buildSequence
	}

	val outerRect = figure.circumscribedRect
	val origin = outerRect.center

	// origin を中心とした円の半径で figure を切っていく。
	// figure を切るのに（origin が outerRect 内のどこにあっても）十分な半径の長さは outerRect の対角線の長さである
	// TODO これだと origin が outerRect の外側にあると対応できない
	val radius = sqrt(outerRect.width * outerRect.width + outerRect.height * outerRect.height)

	val vertices = (0 until count).map { i ->
		val arg = 2 * PI * i / count
		origin + Vec2d(cos(arg), sin(arg)) * radius
	}

	(vertices + vertices[0]).zipWithNext { a, b ->
		val clippingTriangle = Polygon(listOf(a, b, origin))
		clip(figure, clippingTriangle)
	}
			.filter { it !== null}
			.forEach { yield(it !!) }
}
