package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.Figure
import com.bolta_lab.tiles.Vec2d
import com.esri.core.geometry.*
import kotlin.coroutines.experimental.buildSequence

fun composite(vararg dividers: Divider): Divider = fun(figure: Figure): Sequence<Figure> {
	var result = sequenceOf(figure)
	dividers.forEach { divider ->
		result = result.flatMap { parent ->
			divider(parent).map {
				clipExper(it, parent)
			}.filter {
				// TODO null だけでなく頂点なしもはじくべきでは？
				it !== null
			}.map { it !! }
		}
	}

//	val figures1 = dividers[0](figure).map { it.clipBy(figure) }
//	val figures2 = figures1.map { figure1 ->
//		dividers[1](figure1).map { it.clipBy(figure1) }
//
//	}
//	val figures3 = figures
//
//
//	dividers.forEach { divider ->
//		val divided
//
//	}
//
	return result
}

private fun clipExper(figure: Figure, clip: Figure): Figure? {
	fun v2p(v: Vec2d) = Point(v.x.toDouble(), v.y.toDouble())
	fun toEsriPolygon(vertices: List<Vec2d>): Polygon {
		val poly = Polygon()
		// TODO figure の点の数チェックが必要
		poly.startPath(v2p(vertices[0]))
		vertices.drop(1).forEach { v -> poly.lineTo(v2p(v)) }
		return poly
	}
	val figureEsri = toEsriPolygon(figure.vertices)
	val clipEsri = toEsriPolygon(clip.vertices)

	val clipped = OperatorIntersection.local().execute(figureEsri, clipEsri,
			SpatialReference.create(4326), // これはなんだ？？
			null)
	return if (clipped.type.value() == Geometry.GeometryType.Polygon) com.bolta_lab.tiles.Polygon(
			(clipped as Polygon).coordinates2D.map { Vec2d(it.x, it.y) })
	else null
}

private fun enumVertices(geom: Geometry): Sequence<Vec2d> = buildSequence {
	val cur = SimpleGeometryCursor(geom)
	while (true) {
		val g = cur.next()
		println(g.javaClass)
		if (g === null) break
		val pt = g as? Point
		if (pt !== null) {
			yield(Vec2d(pt.x, pt.y))
		} else {
			println("recurring...")
			yieldAll(enumVertices(g))
		}
	}
}
