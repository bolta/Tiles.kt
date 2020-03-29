package com.bolta_lab.tiles.spec

import com.bolta_lab.tiles.RenderingParameterSet
import com.bolta_lab.tiles.Vec2d
import com.bolta_lab.tiles.color.Color
import com.bolta_lab.tiles.color.defaultColorGen
import com.bolta_lab.tiles.divider.Divider
import com.bolta_lab.tiles.divider.lrtb
import org.hjson.JsonArray
import org.hjson.JsonObject
import org.hjson.JsonValue
import java.io.Reader
import java.util.*

fun compileSpec(script: Reader): RenderingParameterSet {
	// TODO エラーを親切に出す
	val specRaw = JsonValue.readHjson(script).asObject()!!
	val spec = preprocessSpec(specRaw)

	val size = parseSize(spec["size"].asArray()!!)
	val divider = parseDivider(spec["divider"].asObject()!!)
	val colors = parseColors(spec["colors"].asObject()!!)

	return RenderingParameterSet(size, divider, colors)
}

private fun parseSize(array: JsonArray): Vec2d = Vec2d(array[0].asDouble(), array[1].asDouble())

private fun parseDivider(obj: JsonObject): Divider {
	val type = obj["type"].asString() !!

	return when (type) {
		"lrtb" -> {
			val tileSize = parseSize(obj["tileSize"].asArray() !!)
			lrtb(tileSize)
		}
		else -> throw SpecException("Unknown divider type: $type")
	}
}

private fun parseColors(obj: JsonObject): Sequence<Color> {
	val type = obj["type"].asString() !!

	return when (type) {
		"default" -> {
			val seed = obj["seed"].asLong()
			val rand = Random(seed)
			defaultColorGen(rand)
		}

		else -> throw SpecException("Unknown color generator type: $type")
	}
}
