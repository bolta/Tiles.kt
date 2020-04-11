package com.bolta_lab.tiles

import com.bolta_lab.tiles.divider.Divider
import com.bolta_lab.tiles.color.Color
import com.esri.core.geometry.*
import com.esri.core.geometry.Polygon
import processing.core.PApplet
import processing.core.PConstants
import java.util.*
import kotlin.coroutines.experimental.buildSequence

class TileByTileMainWindow(params: RenderingParameterSet, outFileName: String) : MainWindow(params, outFileName) {
	private val renderingIter: Iterator<() -> Unit> by lazy {
		val procs = renderAsSequence(this.g, this.params) + {
			this.save(this.outFileName)
			println("rendering completed and image saved as ${this.outFileName}")
		}
		procs.iterator()
	}

	override fun draw() {
		if (! this.renderingIter.hasNext()) return

		this.renderingIter.next()()
	}
}
