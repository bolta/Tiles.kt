package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.Figure
import com.bolta_lab.tiles.Polygon
import com.bolta_lab.tiles.Rect
import com.bolta_lab.tiles.Vec2d
import kotlin.coroutines.experimental.buildSequence

data class Index2d(val x: Int, val y: Int)

fun matrix(arrangeTiles: (Index2d) -> Sequence<Index2d>, tileSize: Vec2d): Divider = fun(figure: Figure): Sequence<Figure> {
	val rect = figure.circumscribedRect
	val tileOrderByIndex = arrangeTiles(Index2d(rect.width divByTiles tileSize.x, rect.height divByTiles  tileSize.y))

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

fun lrtb(tileSize: Vec2d): Divider =
		matrix({ parent -> buildSequence {
			(0 until parent.y).forEach { y ->
				(0 until parent.x).forEach { x ->
					yield(Index2d(x, y))
				}
			}
		} }, tileSize)

private infix fun Double.divByTiles(tileLen: Double) = ((this - 1) / tileLen).toInt() + 1
