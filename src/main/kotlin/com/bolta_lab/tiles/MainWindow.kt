package com.bolta_lab.tiles

import com.bolta_lab.tiles.divider.Divider
import com.bolta_lab.tiles.color.Color
import com.esri.core.geometry.*
import com.esri.core.geometry.Polygon
import processing.core.PApplet
import processing.core.PConstants
import kotlin.coroutines.experimental.buildSequence

class MainWindow(val rect: Rect, val divider: Divider, val colors: Sequence<Color>) : PApplet() {
	override fun settings() {
		// TODO leftTop は (0, 0) を期待している
		this.size(this.rect.right.toInt(), this.rect.bottom.toInt())
	}

	override fun setup() {
		val start = System.currentTimeMillis()

		val figures = this.divider(this.rect)
		this.rectMode(PConstants.CORNER)

//		val mask = this.createGraphics(this.width, this.height)
//		mask.beginDraw()
//		run {
//			mask.triangle(30f, 480f, 256f, 30f, 480f, 480f)
//		}
//		mask.endDraw()

//		val canvas = this.createGraphics(this.width, this.height)

//		canvas.beginDraw()
		figures.zip(this.colors).forEach { (figure_, color) ->
			val figure = clipExper(figure_)
			if (figure !== null && figure.vertices.count() > 0) { // TODO 本当は zip する前に飛ばす

				/*canvas*/this.g.fill(256 * color.red, 256 * color.green, 256 * color.blue)
				figure.paint(/*canvas*/this.g)
				//			this.rect(figure.left.toFloat(), figure.top.toFloat(), figure.right.toFloat(), figure.bottom.toFloat())
			}
		}
//		canvas.mask(mask)
//		canvas.endDraw()

//		this.g.image(canvas, 0f, 0f)

		println((System.currentTimeMillis() - start).toString() + " ms")
	}
}

private fun clipExper(figure: Figure): Figure? {
	fun v2p(v: Vec2d) = Point(v.x.toDouble(), v.y.toDouble())
	fun toEsriPolygon(vertices: List<Vec2d>): Polygon {
		val poly = Polygon()
		// TODO figure の点の数チェックが必要
		poly.startPath(v2p(vertices[0]))
		vertices.drop(1).forEach { v -> poly.lineTo(v2p(v)) }
		return poly
	}
	val figureEsri = toEsriPolygon(figure.vertices)
	val clipEsri = toEsriPolygon(listOf(Vec2d(30.0, 480.0), Vec2d(256.0, 30.0), Vec2d(480.0, 480.0)))

//	val clipped = OperatorIntersection.local().execute(
//			SimpleGeometryCursor(figureEsri), SimpleGeometryCursor(clipEsri),
//			SpatialReference.create(4326), // これはなんだ？？
//			null, -1)
//	val resultVertices = enumVertices(clipped).toList()
	val clipped = OperatorIntersection.local().execute(figureEsri, clipEsri,
			SpatialReference.create(4326), // これはなんだ？？
			null)
//	val resultVertices = enumVertices(clipped).toList()
	return if (clipped.type.value() == Geometry.GeometryType.Polygon) com.bolta_lab.tiles.Polygon(
				(clipped as Polygon).coordinates2D.map { Vec2d(it.x, it.y) })
	else null

//	return com.bolta_lab.tiles.Polygon(resultVertices)
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
