package com.bolta_lab.tiles.spec

import com.bolta_lab.tiles.*
import com.bolta_lab.tiles.color.*
import com.bolta_lab.tiles.divider.*
import org.hjson.JsonArray
import org.hjson.JsonObject
import org.hjson.JsonValue
import org.hjson.Stringify
import java.io.Reader
import java.io.Writer
import java.util.*
import javax.script.Bindings
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings
import kotlin.math.PI

/**
 * 仕様を記述した Hjson 文書を描画可能な値の組に変換する。
 * 副作用として、文書中で乱数の種が省略されている個所に、生成した種を付与する
 *
 * @param masterSeed 種を生成するための種。この「種の種」も文書に記録する
 *
 */
fun compileSpec(script: Reader, defaultMasterSeed: Long, resultScript: Writer): RenderingParameterSet {
	// TODO エラーを親切に出す
	val spec = JsonValue.readHjson(script).evalAsObject()!!

	val masterSeed = spec["masterSeed"]?.evalAsLong() ?: defaultMasterSeed

	spec["masterSeed"] = masterSeed
	val seeds = SeedGenerator(masterSeed)

	val size = compileVec2d(spec["size"])
	val divider = compileDivider(spec["divider"].evalAsObject() !!, seeds)
	val colors = compileColors(spec["colors"].evalAsObject() !!, seeds)
	val border = compileBorder(spec["border"].evalAsObject() !!, seeds)
	val background = compileBackground(spec["background"], seeds)

	spec.writeTo(resultScript, Stringify.HJSON)

	return RenderingParameterSet(size, divider, colors, border, background)
}

private class SeedGenerator(masterSeed: Long) {
	private val random = Random(masterSeed)
	fun next() = this.random.nextLong()
}

private fun compileVec2d(obj: JsonValue) = obj.asArray().let { Vec2d(it[0].evalAsDouble(), it[1].evalAsDouble()) }

private const val PREFIX_EXPR = "="

private val exprEngine = ScriptEngineManager().getEngineByExtension("js") !!

private fun JsonValue.asExpr(): String? {
	if (! this.isString) return null

	val s = this.asString()
	if (! s.startsWith(PREFIX_EXPR)) return null

	return s.substring(PREFIX_EXPR.length)
}

private fun <T> eval(expr: String): T {
	// TODO キャストをちゃんとエラー処理
	return exprEngine.eval("($expr)") as T
}

// 数値は暗黙の型変換を許す（一旦 Number を経由する）
private fun JsonValue.evalAsInt(): Int =
		this.asExpr()?.let { eval<Number>(it).toInt() } ?: this.asInt()
private fun JsonValue.evalAsLong(): Long =
		this.asExpr()?.let { eval<Number>(it).toLong() } ?: this.asLong()
private fun JsonValue.evalAsFloat(): Float =
		this.asExpr()?.let { eval<Number>(it).toFloat() } ?: this.asFloat()
private fun JsonValue.evalAsDouble(): Double =
		this.asExpr()?.let { eval<Number>(it).toDouble() } ?: this.asDouble()

private fun JsonValue.evalAsBoolean(): Boolean =
		this.asExpr()?.let { eval<Boolean>(it) } ?: this.asBoolean()
private fun JsonValue.evalAsString(): String =
		this.asExpr()?.let { eval<String>(it) } ?: this.asString()
private fun JsonValue.evalAsArray(): JsonArray =
		this.asExpr()?.let {
			val bin = eval<Bindings>(it)
			if (bin.isArray) {
				nashornValueToHjsonValue(bin) as JsonArray
			} else throw Exception("not an array")
		} ?: this.asArray()
private fun JsonValue.evalAsObject(): JsonObject =
		this.asExpr()?.let {
			val bin = eval<Bindings>(it)
			if (! bin.isArray) {
				nashornValueToHjsonValue(bin) as JsonObject
			} else throw Exception("not an object")
		} ?: this.asObject()
private val Bindings.isArray get() =
		exprEngine.eval("""Array.isArray(bindings)""",
				SimpleBindings(mutableMapOf<String, Any>("bindings" to this))) as Boolean

private fun nashornValueToHjsonValue(value: Any?): JsonValue {
	return if (value === null) {
		JsonValue.NULL
	} else (value as? Int)?.let { JsonValue.valueOf(it) }
			?: (value as? Long)?.let { JsonValue.valueOf(it) }
			?: (value as? Float)?.let { JsonValue.valueOf(it) }
			?: (value as? Double)?.let { JsonValue.valueOf(it) }
			?: (value as? Boolean)?.let { JsonValue.valueOf(it) }
			?: (value as? String)?.let { JsonValue.valueOf(it) }
			?: (value as? Bindings)?.let { bindings ->
				val result: JsonValue
				if (bindings.isArray) {
					result = JsonArray().apply {
						bindings.values.forEach { this.add(nashornValueToHjsonValue(it)) }
					}
				} else {
					result = JsonObject().apply {
						bindings.entries.forEach { (key, value) ->
							this[key] = nashornValueToHjsonValue(value)
						}
					}
				}
				result
			} ?: throw IllegalArgumentException("cannot convert to JsonValue: $value")
}

private fun compileDivider(obj: JsonObject, seeds: SeedGenerator): Divider {
	val type = obj["type"].evalAsString() !!

	return when (type) {
		// matrix dividers
		"lrtb" -> {
			val tileSize = compileVec2d(obj["tileSize"].evalAsArray() !!)
			val (shape, supplement) = compileMatrixShape(obj)
			matrix(tileSize, ::arrangeLrtb, shape, supplement)
		}
		"diagonal" -> {
			val tileSize = compileVec2d(obj["tileSize"].evalAsArray() !!)
			val (shape, supplement) = compileMatrixShape(obj)
			matrix(tileSize, ::arrangeDiagonal, shape, supplement)
		}
		"random" -> {
			val tileSize = compileVec2d(obj["tileSize"].evalAsArray() !!)
			val seed = obj.getRandomSeedOrSetDefault(seeds)
			val (shape, supplement) = compileMatrixShape(obj)
			matrix(tileSize, arrangeRandom(Random(seed)), shape, supplement)
		}
		"scattering" -> {
			val tileSize = compileVec2d(obj["tileSize"].evalAsArray() !!)
			val seed = obj.getRandomSeedOrSetDefault(seeds)
			val (shape, supplement) = compileMatrixShape(obj)
			matrix(tileSize, arrangeScattering(Random(seed)), shape, supplement)
		}
		"image" -> {
			val tileSize = compileVec2d(obj["tileSize"].evalAsArray() !!)
			val path = obj["path"].evalAsString()
			val (shape, supplement) = compileMatrixShape(obj)
			matrix(tileSize, arrangeImage(path), shape, supplement)
		}

		// non-rect dividers
		"radial" -> {
			val count = obj["count"].evalAsInt()
			radial(count)
		}

		// transformation (geometric higher-order) dividers

		"rotate" -> {
			val divider = compileDivider(obj["divider"].evalAsObject(), seeds)
			val angle = degreeToRadian(obj["angle"].evalAsDouble())
//			val center = compileVec2d(obj["center"].evalAsArray() !!)
			rotate(divider, angle/*, center*/)
		}

		// other higher-order dividers
		"identity" -> {
			val divider = compileDivider(obj["divider"].evalAsObject(), seeds)
			identity(divider) // 呼ばなくてもいいのだが…
		}
		"reverse" -> {
			val divider = compileDivider(obj["divider"].evalAsObject(), seeds)
			reverse(divider)
		}
		"endsToMiddle" -> {
			val divider = compileDivider(obj["divider"].evalAsObject(), seeds)
			endsToMiddle(divider)
		}
		"sometimes" -> {
			val divider = compileDivider(obj["divider"].evalAsObject(), seeds)
			val probability = obj["probability"].evalAsDouble()
			val seed = obj.getRandomSeedOrSetDefault(seeds)
			sometimes(divider, probability, Random(seed))
		}
		"sortByDistance" -> {
			val divider = compileDivider(obj["divider"].evalAsObject(), seeds)
			val origin = compileVec2d(obj["origin"])
			val relative = obj["relative"]?.evalAsBoolean() ?: false
			sortByDistance(divider, origin, relative)
		}
		"sortByArgument" -> {
			val divider = compileDivider(obj["divider"].evalAsObject(), seeds)
			val origin = compileVec2d(obj["origin"])
			val relative = obj["relative"]?.evalAsBoolean() ?: false
			sortByArgument(divider, origin, relative)
		}
		"composite" -> {
			val dividers = obj["dividers"].evalAsArray().map { compileDivider(it.evalAsObject(), seeds) }
			composite(* dividers.toTypedArray())
		}

		else -> throw SpecException("Unknown divider type: $type")
	}
}

private fun degreeToRadian(degree: Double) = degree * 2 * PI / 360

private fun compileMatrixShape(divider: JsonObject) = (divider["shape"]?.evalAsString() ?: "grid").let { type ->
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
	val type = obj["type"].evalAsString() !!

	return when (type) {
		"default" -> {
			val maxChange = obj["maxChange"]?.evalAsFloat() ?: 4 / 256f
			val seed = obj.getRandomSeedOrSetDefault(seeds)
			val rand = Random(seed)
			defaultColorGen(maxChange, rand)
		}
		"fixedTest" -> fixedTest()

		else -> throw SpecException("Unknown color generator type: $type")
	}
}

private fun compileBorder(obj: JsonObject, seeds: SeedGenerator): BorderSetter {
	val type = obj["type"]?.evalAsString() ?: "times"

	return when (type) {
		"none" -> none()
		"times" -> {
			val coeff = obj["coeff"].evalAsFloat()

			// TODO width は none 以外の多くでは共通になるので、今後種類が増えたときは適用のしかたを共通化したい
			val width = obj["width"]?.evalAsFloat() ?: 1f
			times(coeff, width)
		}

		else -> throw SpecException("Unknown border type: $type")
	}
}

private fun compileBackground(obj: JsonValue?, seeds: SeedGenerator): Background {
	if (obj === null) return Background(color = null)

	return Background(compileColor(obj.evalAsArray()))
}

private fun compileColor(color: JsonValue): Color {
	val rgb = color.evalAsArray()
	return Rgb(rgb[0].evalAsFloat(), rgb[1].evalAsFloat(), rgb[2].evalAsFloat())
}

/**
 * オブジェクトのプロパティとして記載された種を取得する。
 * 種が省略されていれば種を生成し、オブジェクトのプロパティとして付与する。
 * その際、「種を生成した」ことを示すプロパティも付与する
 */
private fun JsonObject.getRandomSeedOrSetDefault(seeds: SeedGenerator, name: String = "seed"): Long {
	val specifiedSeed = this[name]
	if (specifiedSeed !== null) return specifiedSeed.evalAsLong()

	val generatedSeed = seeds.next()
	this[name] = generatedSeed
	this["#${name}_generated"] = true

	return generatedSeed
}
