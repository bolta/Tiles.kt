package com.bolta_lab.tiles

import processing.core.PConstants
import processing.core.PGraphics

data class Vec2d(val x: Double, val y: Double)
operator fun Vec2d.plus(that: Vec2d) = Vec2d(this.x + that.x, this.y + that.y)
operator fun Vec2d.minus(that: Vec2d) = Vec2d(this.x - that.x, this.y - that.y)
operator fun Vec2d.times(that: Double) = Vec2d(this.x * that, this.y * that)
//operator fun Double.times(that: Vec2d) = that * this
// TODO 他にも必要なものを追加


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

	abstract val vertices: List<Vec2d>
}

data class Rect(val leftTop: Vec2d, val size: Vec2d): Figure() {
	val left: Double = this.leftTop.x
	val top: Double = this.leftTop.y
	val right: Double = this.left + this.size.x
	val bottom: Double = this.top + this.size.y

	val width: Double = this.size.x
	val height: Double = this.size.y

	override fun paint(g: PGraphics) {
		g.rectMode(PConstants.CORNER)
		g.rect(this.left.toFloat(), this.top.toFloat(), this.width.toFloat(), this.height.toFloat())
	}

	override val circumscribedRect get() = this

	override val vertices: List<Vec2d> get() = listOf(
			this.leftTop,
			Vec2d(this.right, this.top),
			Vec2d(this.right, this.bottom),
			Vec2d(this.left, this.bottom))

	val center get() = Vec2d(this.left + this.width / 2, this.top + this.height / 2)
}

data class Polygon(override val vertices: List<Vec2d>): Figure() {
	init {
		if (vertices.isEmpty()) throw IllegalArgumentException("empty polygon")
	}

	override fun paint(g: PGraphics) {
		// TODO 並んだ矩形を塗るとき、上に接する矩形の下枠線を潰してしまう（結果、横枠線がなくなる）。どうすればいいのか？
//		if (Math.random() < 0.25) {
		g.beginShape()
		this.vertices.forEach {
			g.vertex(it.x.toFloat(), it.y.toFloat())
//println(it)
		}
		this.vertices.firstOrNull()?.let {
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

		Rect(Vec2d(left, top), Vec2d(right - left, bottom - top))
	}
}
