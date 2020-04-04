package com.bolta_lab.tiles

import com.bolta_lab.tiles.spec.compileSpec
import processing.core.PApplet
import java.io.FileReader
import java.io.FileWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import kotlin.system.exitProcess

fun main(args: Array<String>) {
	if (args.size < 1) {
		System.err.println("Please specify the spec (hjson) file.")
		exitProcess(1)
	}
	val specFileName = args[0]

	val now = System.currentTimeMillis()

	val masterSeed = now
	val resultSpec = StringWriter()
	val params = resultSpec.buffered().use { resultSpec_ ->
		FileReader(specFileName).buffered().use { spec ->
			compileSpec(spec, masterSeed, resultSpec_)
		}
	}
	resultSpec.close()

	val filenameBase = makeFilenameBase(params.size, now)
	FileWriter("$filenameBase.hjson").buffered().use {
		it.write(resultSpec.toString())
	}

	val window = MainWindow(params, "$filenameBase.png")
	PApplet.runSketch((listOf(window.javaClass.canonicalName) + args).toTypedArray(), window)
}

private fun makeFilenameBase(size: Vec2d, now: Long): String {
	val width = size.x.toInt()
	val height = size.y.toInt()
	val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(now)
	return "${width}x${height}_${timestamp}"
}
