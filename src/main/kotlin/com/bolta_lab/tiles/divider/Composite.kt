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
//				it
				clip(it, parent)
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
