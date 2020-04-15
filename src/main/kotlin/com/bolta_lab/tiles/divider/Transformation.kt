package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.Figure
import com.bolta_lab.tiles.Rect
import com.bolta_lab.tiles.Vec2d
import java.lang.Double.max
import java.lang.Double.min

fun rotate(divider: Divider, angle: Double, center: Vec2d = Vec2d(0.0, 0.0)) = fun (figure: Figure): Sequence<Figure> {
	val expandedOuterRect = figure.rotate(-angle, center).circumscribedRect
	val originalOuterRect = figure.circumscribedRect

	val widerLeft = min(expandedOuterRect.left, originalOuterRect.left)
	val widerTop = min(expandedOuterRect.top, originalOuterRect.top)
	val widerRight = max(expandedOuterRect.right, originalOuterRect.right)
	val widerBottom = max(expandedOuterRect.bottom, originalOuterRect.bottom)

	val leftTop = Vec2d(widerLeft, widerTop)
	val size = Vec2d(widerRight, widerBottom) - leftTop
	val maxRect = Rect(leftTop, size)

	return divider(maxRect).map { it.rotate(angle, center) }
			.let { clip(it, figure) }

//	return divider(figure).map { it.rotate(angle, center) }
}
