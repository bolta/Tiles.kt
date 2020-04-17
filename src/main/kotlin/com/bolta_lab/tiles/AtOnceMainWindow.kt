package com.bolta_lab.tiles

import com.bolta_lab.tiles.divider.Divider
import com.bolta_lab.tiles.color.Color
import com.esri.core.geometry.*
import com.esri.core.geometry.Polygon
import processing.core.PApplet
import processing.core.PConstants
import java.util.*
import kotlin.coroutines.experimental.buildSequence

class AtOnceMainWindow(params: RenderingParameterSet, outFileName: String) : MainWindow(params, outFileName) {
	override fun setup() {
// デバッグ用にキャンバスの外側を見たい場合は有効にする
//		this.translate(params.size.x.toFloat() / 2, params.size.y.toFloat() / 2)
//		this.scale(0.5f)

		val start = System.currentTimeMillis()
//		this.g.noStroke()
		render(this.g, this.params)
		println((System.currentTimeMillis() - start).toString() + " ms")
		this.save(this.outFileName)

// デバッグ用にキャンバスの外側を見たい場合は有効にする（キャンバスの領域を示す）
//		this.stroke(255f, 0f, 0f)
//		this.noFill()
//		this.strokeWeight(3f)
//		this.rect(0f, 0f, params.size.x.toFloat(), params.size.y.toFloat())
	}
}

