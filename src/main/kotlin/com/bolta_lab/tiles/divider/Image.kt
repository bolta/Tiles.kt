package com.bolta_lab.tiles.divider

import java.io.File
import javax.imageio.ImageIO

// TODO 仮称
fun arrangeImage(imagePath: String) = fun (parent: Index2d): Sequence<Index2d> {
	val image = ImageIO.read(File(imagePath)) !!

//	fun valueForTile(index: Index2d) = image.getRGB(image.width * index.x / parent.x, image.height * index.y / parent.y)
	fun valueForTile(index: Index2d): Double {
		val rgb: Int = image.getRGB(image.width * index.x / parent.x, image.height * index.y / parent.y)
		val r = (rgb and 0x00ff0000) shr 16
		val g = (rgb and 0x0000ff00) shr 8
		val b = (rgb and 0x000000ff)
		return (r + g + b) / 3.0
	}

	return arrangeLrtb(parent).toList().sortedBy(::valueForTile).asSequence()
}
