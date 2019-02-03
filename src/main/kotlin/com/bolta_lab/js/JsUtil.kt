package com.bolta_lab.js

import java.io.Reader
import javax.script.Bindings
import javax.script.ScriptEngine
import javax.script.ScriptEngineFactory
import javax.script.ScriptEngineManager

fun engine() = ScriptEngineManager().getEngineByName("nashorn") as ScriptEngine

inline fun <reified T> ScriptEngine.evalAs(script: Reader) = requiredValue<T>(this.eval(script))

inline fun <reified T> ScriptEngine.evalAs(script: String) = requiredValue<T>(this.eval(script))


inline fun <reified T> requiredValue(value: Any?) =
		optionalValue<T>(value) ?: throw JsException(/* TODO message */)

inline fun <reified T> optionalValue(value: Any?) =
		// TODO Int と Double の差異を吸収
		value as T?

inline fun <reified T> Bindings.required(key: String) = requiredValue<T>(this[key])

inline fun <reified T> Bindings.optional(key: String) = optionalValue<T>(this[key])

