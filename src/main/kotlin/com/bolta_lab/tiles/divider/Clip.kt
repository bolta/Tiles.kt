package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.Figure
import com.bolta_lab.tiles.Vec2d
import com.esri.core.geometry.*

fun clip(figure: Figure, clip: Figure): Figure? {
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

	if (clipped.type.value() != Geometry.GeometryType.Polygon) return null

	val coords = (clipped as Polygon).coordinates2D
	if (coords.isEmpty()) return null

	return com.bolta_lab.tiles.Polygon(coords.map { Vec2d(it.x, it.y) })
}

fun clip(figures: Sequence<Figure>, clip: Figure): Sequence<Figure> =
		figures.map { clip(it, clip) }
				.filter { it !== null }
				.map { it !! }
