package com.bolta_lab.tiles.divider

import com.bolta_lab.js.JsExpr
import com.bolta_lab.tiles.Figure
import com.bolta_lab.tiles.Rect
import java.util.function.Function

typealias Divider = (Figure) -> Sequence<Figure>

//interface Divider {
//
//	// 設定ファイルを読み込む fromScript() 関数（ファクトリメソッド）は、各具象クラスに用意する
//
//	operator fun invoke(rect: Rect): Sequence<Rect>
//
//	fun toScript(): JsExpr
//}