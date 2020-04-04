package com.bolta_lab.tiles

import com.bolta_lab.tiles.Vec2d
import com.bolta_lab.tiles.color.Color
import com.bolta_lab.tiles.divider.BorderSetter
import com.bolta_lab.tiles.divider.BorderSettings
import com.bolta_lab.tiles.divider.Divider

data class RenderingParameterSet(
		val size: Vec2d,
		val divider: Divider,
		val colors: Sequence<Color>,
		val border: BorderSetter)
