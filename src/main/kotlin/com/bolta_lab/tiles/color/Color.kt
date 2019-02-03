package com.bolta_lab.tiles.color

abstract class Color {
	abstract val red: Float
	abstract val green: Float
	abstract val blue: Float
}

class Rgb(red: Float, green: Float, blue: Float) : Color() {
	override val red = red
	override val green = green
	override val blue = blue
}
