package com.bolta_lab.tiles.divider

import com.bolta_lab.tiles.Figure
import kotlin.coroutines.experimental.buildSequence

/**
 * 元となる divider の出力を 2 等分し、「先頭から中間まで」と「末尾から中間まで」を交互に並べる（両端から出発して中間で終わる）
 * タイルの配置は変わらないが、色の並びが先頭と末尾で対称になる
 */
fun endsToMiddle(divider: Divider) = fun (figure: Figure): Sequence<Figure> {
	val origResult = divider(figure).toList()
	val count = origResult.count()
	val formerHalf = origResult.subList(0, count / 2)
	val latterHalf = origResult.subList(count / 2, count)

	return buildSequence {
		formerHalf.zip(latterHalf.asReversed()).forEach { (f, l) ->
			yield(f)
			yield(l)
		}
		// 奇数個の場合は 1 個だけ余っている
		if (latterHalf.count() > formerHalf.count()) yield(latterHalf.last())
	}
}
