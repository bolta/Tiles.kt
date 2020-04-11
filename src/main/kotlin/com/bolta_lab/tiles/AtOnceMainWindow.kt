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
		val start = System.currentTimeMillis()
//		this.g.noStroke()
		render(this.g, this.params)
		println((System.currentTimeMillis() - start).toString() + " ms")
		this.save(this.outFileName)
	}
}

