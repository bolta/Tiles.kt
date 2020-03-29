package com.bolta_lab.tiles

import processing.core.PConstants
import processing.core.PGraphics
import java.util.*

fun render(grp: PGraphics, params: RenderingParameterSet) {
	val outerRect = Rect(Vec2d(0.0, 0.0), params.size)
	val figures = params.divider(outerRect)
	grp.rectMode(PConstants.CORNER)

	figures.zip(params.colors).forEach { (figure, color) ->
		// TODO 外形で clip する
		if (/*figure !== null &&*/ figure.vertices.count() > 0) { // TODO 本当は zip する前に飛ばす

			grp.fill(256 * color.red, 256 * color.green, 256 * color.blue)
			figure.paint(grp)
		}
	}
}
