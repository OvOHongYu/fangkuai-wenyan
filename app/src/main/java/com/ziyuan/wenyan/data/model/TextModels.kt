package com.ziyuan.wenyan.data.model

import kotlinx.serialization.Serializable

// 篇目中的重点字词注释
@Serializable
data class Annotation(
    val word: String,                 // 字/词
    val meaning: String,              // 该处释义
    val note: String = "",            // 简短注释
    val positions: List<Int> = emptyList()  // 出现位置（字符下标）；同一字多义时必填
)

// 字词在篇目中的一次出现（由注释推导）
data class Occurrence(
    val char: String,
    val yixiang: String,
    val start: Int
)

// 高中文言诗词篇目
@Serializable
data class TextPiece(
    val id: String,
    val title: String,
    val author: String = "",
    val dynasty: String = "",
    val book: String = "",      // 教材册别：必修上册/必修下册/选择性必修上册/选择性必修中册/选择性必修下册/其他常见篇目
    val genre: String = "文言文", // 文言文 / 诗词
    val content: String,
    val translation: String = "",
    val annotations: List<Annotation> = emptyList()
) {
    // 由注释生成出现位置；positions 为空时取该词在正文中的全部位置
    val occurrences: List<Occurrence>
        get() = annotations.flatMap { annotation ->
            val positions = annotation.positions.ifEmpty { findAll(annotation.word) }
            positions.map { Occurrence(annotation.word, annotation.meaning, it) }
        }

    private fun findAll(word: String): List<Int> {
        if (word.isBlank() || !content.contains(word)) return emptyList()
        val result = mutableListOf<Int>()
        var index = content.indexOf(word)
        while (index >= 0) {
            result.add(index)
            index = content.indexOf(word, index + word.length)
        }
        return result
    }
}
