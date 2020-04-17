package com.bolta_lab.tiles

import com.bolta_lab.tiles.color.Color
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.coroutines.experimental.buildSequence

/**
 * 全ての図形・色を描画する
 */
fun render(grp: PGraphics, params: RenderingParameterSet) {
//	grp.background(0f)
	renderAsSequence(grp, params).forEach { it() }
}

/**
 * 図形・色を 1 つずつ描画する（1 つ 1 つを描画する手続きのシーケンスを返す）
 */
fun renderAsSequence(grp: PGraphics, params: RenderingParameterSet) : Sequence<() -> Unit> = buildSequence {
	yield() {
		renderBackground(grp, params.background)
	}

	val outerRect = Rect(Vec2d(0.0, 0.0), params.size)
	val figures = params.divider(outerRect)
	grp.rectMode(PConstants.CORNER)

	figures.zip(params.colors).forEach { (figure, color) ->
		// TODO 外形で clip する
		if (/*figure !== null &&*/ figure.vertices.count() > 0) { // TODO 本当は zip する前に飛ばす
			yield() {
				color.toP5Color().let { (r, g, b) -> grp.fill(r, g, b) }
				setBorder(grp, params.border, color)
				figure.paint(grp)
			}
		}
	}
}

private fun renderBackground(grp: PGraphics, background: Background) {
	if (background.color !== null) {
		background.color.toP5Color().let { (r, g, b) -> grp.background(r, g, b) }
	}
}

private fun setBorder(grp: PGraphics, setter: BorderSetter, fillColor: Color) {
	val settings = setter(fillColor)
	if (! settings.borderExists) {
		grp.noStroke()
		return
	}

	settings.color.toP5Color().let { (r, g, b) -> grp.stroke(r, g, b) }
	grp.strokeWeight(settings.width)
}

private data class P5Color(val red: Float, val green: Float, val blue: Float)
private fun Color.toP5Color() = P5Color(256 * this.red, 256 * this.green, 256 * this.blue)
