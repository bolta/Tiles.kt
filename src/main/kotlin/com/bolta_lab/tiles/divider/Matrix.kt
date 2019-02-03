package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.Rect
import com.bolta_lab.tiles.Vec2d
import kotlin.coroutines.experimental.buildSequence

fun matrix(arrangeTiles: (Vec2d) -> Sequence<Vec2d>, tileSize: Vec2d): Divider = fun(rect: Rect): Sequence<Rect> {
	val tileOrderByIndex = arrangeTiles(Vec2d(rect.width divByTiles tileSize.x, rect.height divByTiles  tileSize.y))

	fun tileIndexToRect(tileIndex: Vec2d): Rect = Rect(
			Vec2d(rect.left + tileSize.x * tileIndex.x, rect.top + tileSize.y * tileIndex.y),
			tileSize)

	return tileOrderByIndex.map(::tileIndexToRect)
}

fun lrtb(tileSize: Vec2d): Divider =
		matrix({ parent -> buildSequence {
			(0 until parent.y).forEach { y ->
				(0 until parent.x).forEach { x ->
					yield(Vec2d(x, y))
				}
			}
		} }, tileSize)

private infix fun Int.divByTiles(tileLen: Int) = (this - 1) / tileLen + 1
