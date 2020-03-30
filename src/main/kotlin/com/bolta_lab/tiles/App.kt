package com.bolta_lab.tiles

import com.bolta_lab.tiles.color.defaultColorGen
import com.bolta_lab.tiles.divider.composite
import com.bolta_lab.tiles.divider.lrtb
import com.bolta_lab.tiles.spec.compileSpec
import processing.core.PApplet
import java.io.StringReader
import java.io.StringWriter
import java.util.*

fun main(args: Array<String>) {
	val spec = """
		{
			size: [1024, 768]
			divider: {
				type: composite
				dividers: [
					{
						type: lrtb
						tileSize: [64, 64]
					}
					{
						type: lrtb
						tileSize: [16, 16]
					}
				]
			}
			colors: {
				type: fixedTest
//				seed: 40
			}
		}
	""".trimIndent()

	val masterSeed = System.currentTimeMillis()

	val writer = StringWriter()
	val params = writer.buffered().use {
		spec.reader().use { reader -> compileSpec(reader, masterSeed, writer) }
	}
	writer.close()
	println(writer.toString())

	val window = MainWindow(params)
	PApplet.runSketch((listOf(window.javaClass.canonicalName) + args).toTypedArray(), window)
}
