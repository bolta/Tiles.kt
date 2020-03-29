package com.bolta_lab.tiles

import com.bolta_lab.tiles.color.defaultColorGen
import com.bolta_lab.tiles.divider.composite
import com.bolta_lab.tiles.divider.lrtb
import com.bolta_lab.tiles.spec.compileSpec
import processing.core.PApplet
import java.io.StringReader
import java.util.*

fun main(args: Array<String>) {
	val spec = """
		{
			size: [1024, 768]
			divider: {
				type: "lrtb"
				tileSize: [16, 8]
			}
			colors: {
				type: "default"
				seed: 40
			}
		}
	""".trimIndent()

	val params = spec.reader().use { compileSpec(it) }
	val window = MainWindow(params)

	PApplet.runSketch((listOf(window.javaClass.canonicalName) + args).toTypedArray(), window)
}
