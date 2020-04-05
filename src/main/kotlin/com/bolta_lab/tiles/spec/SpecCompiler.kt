package com.bolta_lab.tiles.spec

import com.bolta_lab.tiles.RenderingParameterSet
import com.bolta_lab.tiles.Vec2d
import com.bolta_lab.tiles.color.Color
import com.bolta_lab.tiles.color.defaultColorGen
import com.bolta_lab.tiles.color.fixedTest
import com.bolta_lab.tiles.divider.*
import org.hjson.JsonArray
import org.hjson.JsonObject
import org.hjson.JsonValue
import org.hjson.Stringify
import java.io.Reader
import java.io.Writer
import java.util.*

/**
 * 仕様を記述した Hjson 文書を描画可能な値の組に変換する。
 * 副作用として、文書中で乱数の種が省略されている個所に、生成した種を付与する
 *
 * @param masterSeed 種を生成するための種。この「種の種」も文書に記録する
 *
 */
fun compileSpec(script: Reader, defaultMasterSeed: Long, resultScript: Writer): RenderingParameterSet {
	// TODO エラーを親切に出す
	val spec = JsonValue.readHjson(script).asObject()!!

	val masterSeed = spec["masterSeed"]?.asLong() ?: defaultMasterSeed

	spec["masterSeed"] = masterSeed
	val seeds = SeedGenerator(masterSeed)

	val size = compileSize(spec["size"].asArray() !!)
	val divider = compileDivider(spec["divider"].asObject() !!, seeds)
	val colors = compileColors(spec["colors"].asObject() !!, seeds)
	val border = compileBorder(spec["border"].asObject() !!, seeds)

	spec.writeTo(resultScript, Stringify.HJSON)

	return RenderingParameterSet(size, divider, colors, border)
}

private class SeedGenerator(masterSeed: Long) {
	private val random = Random(masterSeed)
	fun next() = this.random.nextLong()
}

private fun compileSize(array: JsonArray): Vec2d = Vec2d(array[0].asDouble(), array[1].asDouble())

private fun compileDivider(obj: JsonObject, seeds: SeedGenerator): Divider {
	val type = obj["type"].asString() !!

	return when (type) {
		"lrtb" -> {
			val tileSize = compileSize(obj["tileSize"].asArray() !!)
			val shape = compileMatrixShape(obj)
			matrix(tileSize, ::arrangeLrtb, shape)
		}
		"diagonal" -> {
			val tileSize = compileSize(obj["tileSize"].asArray() !!)
			val shape = compileMatrixShape(obj)
			matrix(tileSize, ::arrangeDiagonal, shape)
		}
		"scattering" -> {
			val tileSize = compileSize(obj["tileSize"].asArray() !!)
			val seed = obj.getRandomSeedOrSetDefault(seeds)
			val shape = compileMatrixShape(obj)
			matrix(tileSize, arrangeScattering(Random(seed)), shape)
		}

		"endsToMiddle" -> {
			val divider = compileDivider(obj["divider"].asObject(), seeds)
			endsToMiddle(divider)
		}
		"composite" -> {
			val dividers = obj["dividers"].asArray().map { compileDivider(it.asObject(), seeds) }
			composite(* dividers.toTypedArray())
		}
		else -> throw SpecException("Unknown divider type: $type")
	}
}

private fun compileMatrixShape(divider: JsonObject) = (divider["shape"]?.asString() ?: "grid").let { type ->
	when (type) {
		"grid" -> ::generateGrid
		"slash" -> generateSlash()
		"backslash" -> generateSlash(true)
		else -> throw SpecException("Unknown matrix shape type: $type")
	}
}

private fun compileColors(obj: JsonObject, seeds: SeedGenerator): Sequence<Color> {
	val type = obj["type"].asString() !!

	return when (type) {
		"default" -> {
			val maxChangeAbs = obj["maxChangeAbs"]?.asFloat() ?: 4 / 256f
			val seed = obj.getRandomSeedOrSetDefault(seeds)
			val rand = Random(seed)
			defaultColorGen(maxChangeAbs, rand)
		}
		"fixedTest" -> fixedTest()

		else -> throw SpecException("Unknown color generator type: $type")
	}
}

private fun compileBorder(obj: JsonObject, seeds: SeedGenerator): BorderSetter {
	val type = obj["type"].asString() !!

	return when (type) {
		"none" -> none()
		"times" -> {
			val coeff = obj["coeff"].asFloat()

			// TODO width は none 以外の多くでは共通になるので、今後種類が増えたときは適用のしかたを共通化したい
			val width = obj["width"]?.asFloat() ?: 1f
			times(coeff, width)
		}

		else -> throw SpecException("Unknown border type: $type")
	}
}


/**
 * オブジェクトのプロパティとして記載された種を取得する。
 * 種が省略されていれば種を生成し、オブジェクトのプロパティとして付与する。
 * その際、「種を生成した」ことを示すプロパティも付与する
 */
private fun JsonObject.getRandomSeedOrSetDefault(seeds: SeedGenerator, name: String = "seed"): Long {
	val specifiedSeed = this[name]
	if (specifiedSeed !== null) return specifiedSeed.asLong()

	val generatedSeed = seeds.next()
	this[name] = generatedSeed
	this["#${name}_generated"] = true

	return generatedSeed
}
