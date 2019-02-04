package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.Figure

fun composite(vararg dividers: Divider): Divider = fun(figure: Figure): Sequence<Figure> {
	var result = sequenceOf(figure)
	dividers.forEach { divider ->
		result = result.flatMap(divider)
	}

	return result
}
