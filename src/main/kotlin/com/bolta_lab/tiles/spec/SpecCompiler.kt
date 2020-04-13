package com.bolta_lab.tiles.spec

import com.bolta_lab.tiles.*
import com.bolta_lab.tiles.color.*
import com.bolta_lab.tiles.divider.*
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

	val size = compileVec2d(spec["size"])
	val divider = compileDivider(spec["divider"].asObject() !!, seeds)
	val colors = compileColors(spec["colors"].asObject() !!, seeds)
	val border = compileBorder(spec["border"].asObject() !!, seeds)
	val background = compileBackground(spec["background"], seeds)

	spec.writeTo(resultScript, Stringify.HJSON)

	return RenderingParameterSet(size, divider, colors, border, background)
}

private class SeedGenerator(masterSeed: Long) {
	private val random = Random(masterSeed)
	fun next() = this.random.nextLong()
}

private fun compileVec2d(obj: JsonValue): Vec2d = obj.asArray().let { Vec2d(it[0].asDouble(), it[1].asDouble()) }

private fun compileDivider(obj: JsonObject, seeds: SeedGenerator): Divider {
	val type = obj["type"].asString() !!

	return when (type) {
		// matrix dividers
		"lrtb" -> {
			val tileSize = compileVec2d(obj["tileSize"].asArray() !!)
			val (shape, supplement) = compileMatrixShape(obj)
			matrix(tileSize, ::arrangeLrtb, shape, supplement)
		}
		"diagonal" -> {
			val tileSize = compileVec2d(obj["tileSize"].asArray() !!)
			val (shape, supplement) = compileMatrixShape(obj)
			matrix(tileSize, ::arrangeDiagonal, shape, supplement)
		}
		"random" -> {
			val tileSize = compileVec2d(obj["tileSize"].asArray() !!)
			val seed = obj.getRandomSeedOrSetDefault(seeds)
			val (shape, supplement) = compileMatrixShape(obj)
			matrix(tileSize, arrangeRandom(Random(seed)), shape, supplement)
		}
		"scattering" -> {
			val tileSize = compileVec2d(obj["tileSize"].asArray() !!)
			val seed = obj.getRandomSeedOrSetDefault(seeds)
			val (shape, supplement) = compileMatrixShape(obj)
			matrix(tileSize, arrangeScattering(Random(seed)), shape, supplement)
		}
		"image" -> {
			val tileSize = compileVec2d(obj["tileSize"].asArray() !!)
			val path = obj["path"].asString()
			val (shape, supplement) = compileMatrixShape(obj)
			matrix(tileSize, arrangeImage(path), shape, supplement)
		}

		// higher-order dividers
		"identity" -> {
			val divider = compileDivider(obj["divider"].asObject(), seeds)
			identity(divider) // 呼ばなくてもいいのだが…
		}
		"reverse" -> {
			val divider = compileDivider(obj["divider"].asObject(), seeds)
			reverse(divider)
		}
		"endsToMiddle" -> {
			val divider = compileDivider(obj["divider"].asObject(), seeds)
			endsToMiddle(divider)
		}
		"sometimes" -> {
			val divider = compileDivider(obj["divider"].asObject(), seeds)
			val probability = obj["probability"].asDouble()
			val seed = obj.getRandomSeedOrSetDefault(seeds)
			sometimes(divider, probability, Random(seed))
		}
		"sortByDistance" -> {
			val divider = compileDivider(obj["divider"].asObject(), seeds)
			val origin = compileVec2d(obj["origin"])
			val relative = obj["relative"]?.asBoolean() ?: false
			sortByDistance(divider, origin, relative)
		}
		"composite" -> {
			val dividers = obj["dividers"].asArray().map { compileDivider(it.asObject(), seeds) }
			composite(* dividers.toTypedArray())
		}

		else -> throw SpecException("Unknown divider type: $type")
	}
}

private fun compileMatrixShape(divider: JsonObject) = (divider["shape"]?.asString() ?: "grid").let { type ->
	fun shapeAndSupplement(generateFigures: (Rect, Vec2d, Index2d) -> Figure,
			supplementCount: Index2d = Index2d(0, 0)) = Pair(generateFigures, supplementCount)
	when (type) {
		"grid" -> shapeAndSupplement(::generateGrid)
		"slash" -> shapeAndSupplement(generateSlash())
		"backslash" -> shapeAndSupplement(generateSlash(true))
		// TODO hex には (1, 1) がつきものなので、ここではなく generateHex() 側で記述したい
		"hex" -> shapeAndSupplement(::generateHex, Index2d(1, 1))
		else -> throw SpecException("Unknown matrix shape type: $type")
	}
}

private fun compileColors(obj: JsonObject, seeds: SeedGenerator): Sequence<Color> {
	val type = obj["type"].asString() !!

	return when (type) {
		"default" -> {
			val maxChange = obj["maxChange"]?.asFloat() ?: 4 / 256f
			val seed = obj.getRandomSeedOrSetDefault(seeds)
			val rand = Random(seed)
			defaultColorGen(maxChange, rand)
		}
		"fixedTest" -> fixedTest()

		else -> throw SpecException("Unknown color generator type: $type")
	}
}

private fun compileBorder(obj: JsonObject, seeds: SeedGenerator): BorderSetter {
	val type = obj["type"]?.asString() ?: "times"

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

private fun compileBackground(obj: JsonValue?, seeds: SeedGenerator): Background {
	if (obj === null) return Background(color = null)

	if (obj.isArray) {
		val color = compileColor(obj)
		return Background(color)

	} else throw SpecException("invalid background spec")
}

private fun compileColor(color: JsonValue) =
		if (color.isArray) {
			val rgb = color.asArray()
			Rgb(rgb[0].asFloat(), rgb[1].asFloat(), rgb[2].asFloat())

		} else throw SpecException("unknown color spec")

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
