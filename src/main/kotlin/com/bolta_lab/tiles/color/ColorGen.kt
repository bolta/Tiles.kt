package com.bolta_lab.tiles.color

import java.lang.Float.max
import java.lang.Float.min
import java.util.*
import kotlin.coroutines.experimental.buildSequence

fun defaultColorGen(maxChangeAbs: Float, rand: Random): Sequence<Color> {
	var c = Rgb(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())

	fun nextComponent(cur: Float) = min(1f, max(0f, cur + rand.nextFloat() * 2 * maxChangeAbs - maxChangeAbs))

	return buildSequence {
		while (true) {
			yield(c)
			c = Rgb(nextComponent(c.red), nextComponent(c.green), nextComponent(c.blue))
		}
	}
}

/**
 * テスト用のランダム性のない色系列。
 * 黒 → 赤 → 黄 → 白 → シアン → 青 → 黒 の変化を繰り返す
 */
fun fixedTest(): Sequence<Color> = buildSequence {
	var r = 0f
	var g = 0f
	var b = 0f
	fun col() = Rgb(r, g, b)

	val resolution = 1000
	val delta = 1f / resolution

	while (true) {
		(0 until resolution).forEach { yield(col()); r += delta }
		(0 until resolution).forEach { yield(col()); g += delta }
		(0 until resolution).forEach { yield(col()); b += delta }
		(0 until resolution).forEach { yield(col()); r -= delta }
		(0 until resolution).forEach { yield(col()); g -= delta }
		(0 until resolution).forEach { yield(col()); b -= delta }
	}
}
