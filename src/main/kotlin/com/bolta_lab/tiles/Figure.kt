package com.bolta_lab.tiles

data class Vec2d(val x: Int, val y: Int)

// とりあえず矩形のみを扱うこととして Figure ではなく Rect で扱うが、
// いずれは Rect は Figure のサブクラスとしたい

data class Rect(val leftTop: Vec2d, val size: Vec2d) {
	val left: Int = this.leftTop.x
	val top: Int = this.leftTop.y
	val right: Int = this.left + this.size.x
	val bottom: Int = this.top + this.size.y

	val width: Int = this.size.x
	val height: Int = this.size.y
}
