package com.ziyuan.wenyan.util

// 拼音索引工具：把卡片标题的拼音转成 A-Z 分组首字母
// 规则：
//  1. 多音字取数据中标注的第一个读音（如 "bèi, pī" 取 "bèi"）
//  2. 带声调的元音统一还原为无音标字母（如 "ài" → "ai"），便于排序与取首字母
//  3. 无法得到 A-Z 首字母的内容（纯符号 / 无拼音）统一归入 "#"
object PinyinIndex {

    // 全部索引字母：A-Z 以及兜底的 #
    val ALL_LETTERS: List<Char> = ('A'..'Z').toList() + '#'

    // 兜底分组
    const val OTHER: Char = '#'

    private val TONE_MAP: Map<Char, Char> = buildMap {
        mapOf(
            'a' to "āáǎà",
            'e' to "ēéěè",
            'i' to "īíǐì",
            'o' to "ōóǒò",
            'u' to "ūúǔù",
            'v' to "ǖǘǚǜü",
            'n' to "ńňǹ"
        ).forEach { (plain, toned) -> toned.forEach { put(it, plain) } }
    }

    // 去掉声调符号：ài → ai
    fun stripTone(text: String): String =
        text.map { TONE_MAP[it] ?: it }.joinToString("")

    // 取第一个读音："bèi, pī" → "bèi"；"chéng" → "chéng"
    fun firstReading(pinyin: String): String =
        pinyin.split(',', '，', '/', '；', ';').firstOrNull()?.trim().orEmpty()

    // 首字母分组：优先用拼音，拼音缺失或非字母时用兜底文本，仍失败则归 #
    fun initialOf(pinyin: String, fallback: String = ""): Char {
        val fromPinyin = stripTone(firstReading(pinyin)).firstOrNull()?.uppercaseChar()
        if (fromPinyin != null && fromPinyin in 'A'..'Z') return fromPinyin
        val fromFallback = stripTone(fallback).trim().firstOrNull()?.uppercaseChar()
        return if (fromFallback != null && fromFallback in 'A'..'Z') fromFallback else OTHER
    }

    // 组内排序键：拼音优先，无拼音时退回原文
    fun sortKeyOf(pinyin: String, fallback: String = ""): String {
        val fromPinyin = stripTone(firstReading(pinyin)).trim()
        return fromPinyin.ifEmpty { stripTone(fallback).trim() }
    }
}
