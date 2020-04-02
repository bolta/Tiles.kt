package com.bolta_lab.tiles

import com.bolta_lab.tiles.divider.Divider
import com.bolta_lab.tiles.color.Color
import com.esri.core.geometry.*
import com.esri.core.geometry.Polygon
import processing.core.PApplet
import processing.core.PConstants
import java.util.*
import kotlin.coroutines.experimental.buildSequence

class MainWindow(private val params: RenderingParameterSet, private val outFileName: String) : PApplet() {
	override fun settings() {
		this.size(this.params.size.x.toInt(), this.params.size.y.toInt())
	}

	override fun setup() {
		val start = System.currentTimeMillis()
//		this.g.noStroke()
		render(this.g, this.params)
		println((System.currentTimeMillis() - start).toString() + " ms")
		this.save(this.outFileName)
	}

// ゆっくり描画するときはこっちを有効化
//	private var renderingIter: Iterator<() -> Unit>? = null // TODO もっと正しい書き方があったはずなのだが
//	override fun setup() {
//		val procs = renderAsSequence(this.g, this.params)
//		this.renderingIter = procs.iterator()
//		this.g.noStroke()
//	}
//	override fun draw() {
//		if (! this.renderingIter !!.hasNext()) return
//
//		this.renderingIter !!.next()()
//	}
// ゆっくり描画ここまで
}
