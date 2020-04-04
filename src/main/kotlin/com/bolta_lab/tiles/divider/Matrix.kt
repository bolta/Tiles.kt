package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.Figure
import com.bolta_lab.tiles.Rect
import com.bolta_lab.tiles.Vec2d
import java.util.*
import kotlin.coroutines.experimental.buildSequence

data class Index2d(val x: Int, val y: Int)

fun matrix(tileSize: Vec2d, arrangeTiles: (Index2d) -> Sequence<Index2d>): Divider = fun (figure: Figure): Sequence<Figure> {
	val rect = figure.circumscribedRect
	val tileOrderByIndex = arrangeTiles(Index2d(rect.width divByTiles tileSize.x, rect.height divByTiles tileSize.y))

	fun tileIndexToRect(tileIndex: Index2d): Rect = Rect(
			Vec2d(rect.left + tileSize.x * tileIndex.x, rect.top + tileSize.y * tileIndex.y),
			tileSize)
// 非矩形タイル実験
//fun tileIndexToRect(tileIndex: Vec2d): Figure {
//	val l = rect.left + tileSize.x * tileIndex.x
//	val t = rect.top + tileSize.y * tileIndex.y
//	val r = l + tileSize.x
//	val b = t + tileSize.y
//
//	return Polygon(listOf(Vec2d(l, t), Vec2d(r, t), Vec2d(l, b)))
//}

	return tileOrderByIndex.map(::tileIndexToRect)
}

fun lrtb(tileSize: Vec2d): Divider = matrix(tileSize) { parent ->
	buildSequence {
		(0 until parent.y).forEach { y ->
			(0 until parent.x).forEach { x ->
				yield(Index2d(x, y))
			}
		}
	}
}

fun diagonal(tileSize: Vec2d): Divider = matrix(tileSize) { parent ->
	buildSequence {
		val from0ToInf = generateSequence(0) { it + 1 }
		from0ToInf.forEach { yStart ->
			val ys = (yStart downTo 0).asSequence()
			val xs = from0ToInf
			xs.zip(ys).forEach { (x, y) ->
				yield(Index2d(x, y))
			}
		}
	}.filter { (x, y) -> x < parent.x && y < parent.y }
			.take(parent.x * parent.y)
}

fun scattering(tileSize: Vec2d, rand: Random): Divider = matrix(tileSize) { parent ->
	buildSequence {
		// x 座標の出現順
		val allXs = (0 until parent.x).flatMap { x -> List(parent.y) { x } }.shuffled(rand)
		// x 座標の各々に対して次に割り当たる y 座標
		val xToY = MutableList(parent.x) { parent.y - 1 }
		allXs.forEach { x ->
			yield(Index2d(x, xToY[x]))
			-- xToY[x]
		}
	}
}

private infix fun Double.divByTiles(tileLen: Double) = ((this - 1) / tileLen).toInt() + 1
