package com.bolta_lab.tiles

import com.bolta_lab.tiles.color.defaultColorGen
import com.bolta_lab.tiles.divider.composite
import com.bolta_lab.tiles.divider.lrtb
import processing.core.PApplet
import java.util.*

fun main(args: Array<String>) {
//	val window = MainWindow(Rect(Vec2d(0, 0), Vec2d(1024, 768)),
//			composite(lrtb(Vec2d(128, 128)), lrtb(Vec2d(64, 64)), lrtb(Vec2d(8, 8))),
//			defaultColorGen(Random()))
//	val spec = parseSettings(StringReader("""
//		({
//			size: [512, 768],
//			divider: {
//				type: 'lrtb',
//				tileSize: [16, 16],
//
//			},
//			colors:{
//
//			},
//		})
//	""".trimIndent()))

	val params = RenderingParameterSet(Vec2d(1024.0, 768.0),
			composite(lrtb(Vec2d(128.0, 128.0)), lrtb(Vec2d(16.0, 16.0))),
			defaultColorGen(Random()))
	val window = MainWindow(params)

	PApplet.runSketch((listOf(window.javaClass.canonicalName) + args).toTypedArray(), window)
}
