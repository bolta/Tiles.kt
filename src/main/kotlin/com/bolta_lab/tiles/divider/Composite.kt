package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.Rect

fun composite(vararg dividers: Divider): Divider = fun(rect: Rect): Sequence<Rect> {
	var result = sequenceOf(rect)
	dividers.forEach { divider ->
		result = result.flatMap(divider)
	}

	return result
}
