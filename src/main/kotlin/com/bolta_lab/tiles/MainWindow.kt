package com.bolta_lab.tiles

import com.bolta_lab.tiles.divider.Divider
import com.bolta_lab.tiles.color.Color
import processing.core.PApplet
import processing.core.PConstants

class MainWindow(val rect: Rect, val divider: Divider, val colors: Sequence<Color>) : PApplet() {
	override fun settings() {
		// TODO leftTop は (0, 0) を期待している
		this.size(this.rect.right, this.rect.bottom)
	}

	override fun setup() {
		val rects = this.divider(this.rect)
		this.rectMode(PConstants.CORNER)



		rects.zip(this.colors).forEach { (rect, color) ->
			this.fill(256 * color.red, 256 * color.green, 256 * color.blue)
			this.rect(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat())
		}
	}
}
