package com.bolta_lab.tiles.color

import java.lang.Float.max
import java.lang.Float.min
import java.lang.Math.abs
import java.util.*
import kotlin.coroutines.experimental.buildSequence

fun defaultColorGen(maxChangeAbs: Float, rand: Random, constraint: (Color) -> Boolean = ::permitAll)
		: Sequence<Color> {
	var c: Color
	do {
		c = Rgb(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())
	} while (! constraint(c))

	fun nextComponent(cur: Float) = min(1f, max(0f, cur + rand.nextFloat() * 2 * maxChangeAbs - maxChangeAbs))

	return buildSequence {
		while (true) {
			yield(c)
			var next: Color
			do {
				next = Rgb(nextComponent(c.red), nextComponent(c.green), nextComponent(c.blue))
			} while (! constraint(next))
			c = next
		}
	}
}

fun permitAll(c: Color) = true

fun isReddish(c: Color) = c.red > c.green && c.red > c.blue
fun isGreenish(c: Color) = c.green > c.blue && c.green > c.red
fun isBlueish(c: Color) = c.blue > c.red && c.blue > c.green
fun isYellowish(c: Color) = c.red > c.blue && c.green > c.blue
		&& abs(c.red - c.green) < c.red - c.blue
		&& abs(c.red - c.green) < c.green - c.blue
fun isCyanish(c: Color) = c.green > c.red && c.blue > c.red
		&& abs(c.green - c.blue) < c.green - c.red
		&& abs(c.green - c.blue) < c.blue - c.red
fun isMagentaish(c: Color) = c.blue > c.green && c.red > c.green
		&& abs(c.blue - c.red) < c.blue - c.green
		&& abs(c.blue - c.red) < c.red - c.green

// 明度の計算をちゃんとした方がいいか？
fun isBrighterThan(thres: Float) = { c: Color ->
	((c.red + c.green + c.blue) / 3).let { it > thres }
}
fun isDarkerThan(thres: Float) = { c: Color ->
	((c.red + c.green + c.blue) / 3).let { it < thres }
}

infix fun ((Color) -> Boolean).and(that: (Color) -> Boolean) = { c: Color -> this(c) && that(c) }
infix fun ((Color) -> Boolean).or(that: (Color) -> Boolean) = { c: Color -> this(c) || that(c) }
fun not(rhs: (Color) -> Boolean) = { c: Color -> ! rhs(c) }


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
