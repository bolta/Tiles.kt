package com.bolta_lab.tiles

import com.bolta_lab.tiles.divider.Divider
import com.bolta_lab.tiles.color.Color
import com.esri.core.geometry.*
import com.esri.core.geometry.Polygon
import processing.core.PApplet
import processing.core.PConstants
import java.util.*
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

		val rand = Random()
		figures.zip(this.colors).forEach { (figure_, color) ->
			if (true || rand.nextDouble() < 0.25) { // 実験：clip がちゃんとかかっている確認のため描画を間引く

				val figure = clipExper(figure_)
//				val figure = figure_
				if (figure !== null && figure.vertices.count() > 0) { // TODO 本当は zip する前に飛ばす

					this.g.fill(256 * color.red, 256 * color.green, 256 * color.blue)
//					this.g.stroke(0f, 0f,0f)//255f, 255f, 255f)
//					this.g.strokeWeight(3f)
					figure.paint(this.g)
				}
			}
		}

		println((System.currentTimeMillis() - start).toString() + " ms")
	}
}

private fun clipExper(figure: Figure): Figure? {
	if (figure.vertices.isEmpty()) return null // TODO どんな場合？

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

	val clipped = OperatorIntersection.local().execute(figureEsri, clipEsri,
			SpatialReference.create(4326), // これはなんだ？？
			null)

	return if (clipped.type.value() == Geometry.GeometryType.Polygon) {
		com.bolta_lab.tiles.Polygon((clipped as Polygon).coordinates2D.map { Vec2d(it.x, it.y) })
	} else {
		null
	}
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
