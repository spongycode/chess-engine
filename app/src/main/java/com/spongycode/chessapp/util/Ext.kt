package com.spongycode.chessapp.util

import com.spongycode.chessapp.R

fun String.getResource(): Int {
    return when(this) {
        "BK" -> R.drawable.bk
        "BQ" -> R.drawable.bq
        "BR" -> R.drawable.br
        "BB" -> R.drawable.bb
        "BN" -> R.drawable.bn
        "BP" -> R.drawable.bp
        "WK" -> R.drawable.wk
        "WQ" -> R.drawable.wq
        "WR" -> R.drawable.wr
        "WB" -> R.drawable.wb
        "WN" -> R.drawable.wn
        "WP" -> R.drawable.wp
        else -> R.drawable.transparent
    }
}