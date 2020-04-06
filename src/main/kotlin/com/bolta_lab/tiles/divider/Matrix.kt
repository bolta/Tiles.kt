package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.Figure
import com.bolta_lab.tiles.Polygon
import com.bolta_lab.tiles.Rect
import com.bolta_lab.tiles.Vec2d
import java.util.*
import kotlin.coroutines.experimental.buildSequence
import kotlin.math.sqrt

fun matrix(tileSize: Vec2d, arrangeTiles: (Index2d) -> Sequence<Index2d>,
		generateFigures: (Rect, Vec2d, Index2d) -> Figure = ::generateGrid,
		supplementCount: Index2d = Index2d(0, 0)) = fun (figure: Figure): Sequence<Figure> {
	val rect = figure.circumscribedRect
	val tileOrderByIndex = arrangeTiles(
			Index2d((rect.width divByTiles tileSize.x) + supplementCount.x,
					(rect.height divByTiles tileSize.y) + supplementCount.y))

	return tileOrderByIndex.map { index -> generateFigures(rect, tileSize, index) }
}

data class Index2d(val x: Int, val y: Int)

fun arrangeLrtb(parent: Index2d) = buildSequence {
	(0 until parent.y).forEach { y ->
		(0 until parent.x).forEach { x ->
			yield(Index2d(x, y))
		}
	}
}

fun arrangeDiagonal(parent: Index2d) = buildSequence {
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

fun arrangeRandom(rand: Random) = fun (parent: Index2d) = arrangeLrtb(parent).asIterable().shuffled().asSequence()

fun arrangeScattering(rand: Random) = fun (parent: Index2d) = buildSequence {
	// x 座標の出現順
	val allXs = (0 until parent.x).flatMap { x -> List(parent.y) { x } }.shuffled(rand)
	// x 座標の各々に対して次に割り当たる y 座標
	val xToY = MutableList(parent.x) { parent.y - 1 }
	allXs.forEach { x ->
		yield(Index2d(x, xToY[x]))
		-- xToY[x]
	}
}

fun generateGrid(circumscribedRect: Rect, tileSize: Vec2d, tileIndex: Index2d): Rect = Rect(
		Vec2d(circumscribedRect.left + tileSize.x * tileIndex.x,
				circumscribedRect.top + tileSize.y * tileIndex.y),
		tileSize)

fun generateSlash(backslash: Boolean = false) =
		fun (circumscribedRect: Rect, tileSize: Vec2d, tileIndex: Index2d): Polygon {
			val l = circumscribedRect.left + tileSize.x * (tileIndex.x / 2 * 2)
			val t = circumscribedRect.top + tileSize.y * tileIndex.y
			val r = l + tileSize.x * 2
			val b = t + tileSize.y

			return if (! backslash) {
				if (tileIndex.x % 2 == 0) {
					Polygon(listOf(Vec2d(l, t), Vec2d(r, t), Vec2d(l, b)))
				} else {
					Polygon(listOf(Vec2d(r, b), Vec2d(l, b), Vec2d(r, t)))
				}
			} else {
				if (tileIndex.x % 2 == 0) {
					Polygon(listOf(Vec2d(l, t), Vec2d(r, t), Vec2d(r, b)))
				} else {
					Polygon(listOf(Vec2d(r, b), Vec2d(l, b), Vec2d(l, t)))
				}
			}
		}

fun generateHex(circumscribedRect: Rect, tileSize: Vec2d, tileIndex: Index2d): Polygon {
	val origin = Vec2d(circumscribedRect.left + tileSize.x * tileIndex.x /* * 3 / 4*/,
			circumscribedRect.top + tileSize.y * (tileIndex.y - if (tileIndex.x % 2 == 1) 0.5 else 0.0))
	val sideLenX = tileSize.x * 2 / 3

	return Polygon(listOf(
			origin,
			Vec2d(origin.x + sideLenX, origin.y),
			Vec2d(origin.x + sideLenX * 3 / 2, origin.y + tileSize.y / 2),
			Vec2d(origin.x + sideLenX, origin.y + tileSize.y),
			Vec2d(origin.x, origin.y + tileSize.y),
			Vec2d(origin.x - sideLenX / 2, origin.y + tileSize.y / 2)))
}

private infix fun Double.divByTiles(tileLen: Double) = ((this - 1) / tileLen).toInt() + 1
