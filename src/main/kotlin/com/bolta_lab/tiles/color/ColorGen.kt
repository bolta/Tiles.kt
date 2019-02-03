package com.bolta_lab.tiles.color

import java.lang.Float.max
import java.lang.Float.min
import java.util.*
import kotlin.coroutines.experimental.buildSequence

fun defaultColorGen(rand: Random): Sequence<Color> {
	// TODO 状態を中に持たず、外から与えるようにする
	// TODO ステップの大きさも外から与える
//	val rand = Random()
	var c = Rgb(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())

	fun nextComponent(cur: Float) = min(1f, max(0f, cur + rand.nextFloat() * 8 / 256f - 4 / 256f))

	return buildSequence {
		while (true) {
			yield(c)
			c = Rgb(nextComponent(c.red), nextComponent(c.green), nextComponent(c.blue))
		}
	}
}
