package com.bolta_lab.tiles

import processing.core.PConstants
import processing.core.PGraphics
import kotlin.coroutines.experimental.buildSequence

/**
 * 全ての図形・色を描画する
 */
fun render(grp: PGraphics, params: RenderingParameterSet) {
	renderAsSequence(grp, params).forEach { it() }
}

/**
 * 図形・色を 1 つずつ描画する（1 つ 1 つを描画する手続きのシーケンスを返す）
 */
fun renderAsSequence(grp: PGraphics, params: RenderingParameterSet) : Sequence<() -> Unit> = buildSequence {
	val outerRect = Rect(Vec2d(0.0, 0.0), params.size)
	val figures = params.divider(outerRect)
	grp.rectMode(PConstants.CORNER)

	figures.zip(params.colors).forEach { (figure, color) ->
		// TODO 外形で clip する
		if (/*figure !== null &&*/ figure.vertices.count() > 0) { // TODO 本当は zip する前に飛ばす

			grp.fill(256 * color.red, 256 * color.green, 256 * color.blue)
			yield() {
				figure.paint(grp)
			}
		}
	}
}
