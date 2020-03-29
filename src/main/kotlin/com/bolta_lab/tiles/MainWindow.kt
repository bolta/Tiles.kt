package com.bolta_lab.tiles

import com.bolta_lab.tiles.divider.Divider
import com.bolta_lab.tiles.color.Color
import com.esri.core.geometry.*
import com.esri.core.geometry.Polygon
import processing.core.PApplet
import processing.core.PConstants
import java.util.*
import kotlin.coroutines.experimental.buildSequence

class MainWindow(val params: RenderingParameterSet) : PApplet() {
	override fun settings() {
		this.size(this.params.size.x.toInt(), this.params.size.y.toInt())
	}

	override fun setup() {
		val start = System.currentTimeMillis()
		render(this.g, this.params)
		println((System.currentTimeMillis() - start).toString() + " ms")
		this.save("${this.width}x${this.height}_${System.currentTimeMillis()}.png")
	}
}

//private fun clipExper(figure: Figure): Figure? {
//	if (figure.vertices.isEmpty()) return null // TODO どんな場合？
//
//	fun v2p(v: Vec2d) = Point(v.x.toDouble(), v.y.toDouble())
//	fun toEsriPolygon(vertices: List<Vec2d>): Polygon {
//		val poly = Polygon()
//		// TODO figure の点の数チェックが必要
//		poly.startPath(v2p(vertices[0]))
//		vertices.drop(1).forEach { v -> poly.lineTo(v2p(v)) }
//		return poly
//	}
//
//	val figureEsri = toEsriPolygon(figure.vertices)
//	val clipEsri = toEsriPolygon(listOf(Vec2d(30.0, 480.0), Vec2d(256.0, 30.0), Vec2d(480.0, 480.0)))
//
//	val clipped = OperatorIntersection.local().execute(figureEsri, clipEsri,
//			SpatialReference.create(4326), // これはなんだ？？
//			null)
//
//	return if (clipped.type.value() == Geometry.GeometryType.Polygon) {
//		com.bolta_lab.tiles.Polygon((clipped as Polygon).coordinates2D.map { Vec2d(it.x, it.y) })
//	} else {
//		null
//	}
//}
//
//private fun enumVertices(geom: Geometry): Sequence<Vec2d> = buildSequence {
//	val cur = SimpleGeometryCursor(geom)
//	while (true) {
//		val g = cur.next()
//		println(g.javaClass)
//		if (g === null) break
//		val pt = g as? Point
//		if (pt !== null) {
//			yield(Vec2d(pt.x, pt.y))
//		} else {
//			println("recurring...")
//			yieldAll(enumVertices(g))
//		}
//	}
//}
