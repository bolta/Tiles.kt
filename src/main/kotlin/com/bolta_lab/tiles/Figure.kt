package com.bolta_lab.tiles

import processing.core.PGraphics

data class Vec2d(val x: Int, val y: Int)

abstract class Figure {
	/**
	 * この図形がこれ以上分割されないものとして、この図形を g に描画する。
	 * stroke, fill 等はすでに設定されているものとする
	 */
	abstract fun paint(g: PGraphics)

	/**
	 * この図形に外接する矩形を返す
	 */
	abstract val circumscribedRect: Rect
}

data class Rect(val leftTop: Vec2d, val size: Vec2d): Figure() {
	val left: Int = this.leftTop.x
	val top: Int = this.leftTop.y
	val right: Int = this.left + this.size.x
	val bottom: Int = this.top + this.size.y

	val width: Int = this.size.x
	val height: Int = this.size.y

	override fun paint(g: PGraphics) {
		g.rect(this.left.toFloat(), this.top.toFloat(), this.right.toFloat(), this.bottom.toFloat())
	}

	override val circumscribedRect get() = this
}

data class Polygon(val vertices: List<Vec2d>): Figure() {
	// TODO 空の vertices は不可

	override fun paint(g: PGraphics) {
		g.beginShape()
		this.vertices.forEach {
			g.vertex(it.x.toFloat(), it.y.toFloat())
		}
		g.endShape()
	}

	override val circumscribedRect by lazy {
		// TODO 1 回のループで効率よく書き直せる
		val left = vertices.minBy { it.x }!!.x
		val top = vertices.minBy { it.y }!!.y
		val right = vertices.maxBy { it.x }!!.x
		val bottom = vertices.maxBy { it.y }!!.y

		Rect(Vec2d(left, top), Vec2d(right, bottom))
	}
}
