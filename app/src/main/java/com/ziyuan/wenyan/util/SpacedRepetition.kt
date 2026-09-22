package com.ziyuan.wenyan.util

// 艾宾浩斯遗忘曲线：10 分钟、1 天、3 天、7 天、15 天、30 天
object SpacedRepetition {
    val INTERVALS_MS = longArrayOf(
        10 * 60 * 1000L,
        1L * 24 * 3600 * 1000,
        3L * 24 * 3600 * 1000,
        7L * 24 * 3600 * 1000,
        15L * 24 * 3600 * 1000,
        30L * 24 * 3600 * 1000
    )
    const val MAX_STAGE = 5

    fun intervalMs(stage: Int): Long = INTERVALS_MS[stage.coerceIn(0, MAX_STAGE)]
}
