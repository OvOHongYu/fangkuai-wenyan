package com.ziyuan.wenyan.util

import com.ziyuan.wenyan.data.model.CardUnionFull
import com.ziyuan.wenyan.data.model.CompareCardFull
import com.ziyuan.wenyan.data.model.ShiCardFull
import com.ziyuan.wenyan.data.model.XuCardFull

data class QuizQuestion(
    val cardId: String?,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val analysis: String,
    val sentence: String = "",
    val source: String = ""
)

// 由卡片库生成选择题：实词考义项、虚词考关系、对比卡考例句
object QuizGenerator {
    fun generate(cards: List<CardUnionFull>, count: Int = 5): List<QuizQuestion> {
        if (cards.isEmpty()) return emptyList()
        val relationPool = cards.filterIsInstance<XuCardFull>()
            .flatMap { it.card.judgeFlow.map { step -> step.relation } }.distinct()
        val yixiangPool = cards.filterIsInstance<ShiCardFull>()
            .flatMap { it.card.examples.map { example -> example.yixiang } }.distinct()
        return cards.shuffled().take(count).mapNotNull { card ->
            when (card) {
                is ShiCardFull -> genShi(card, yixiangPool)
                is XuCardFull -> genXu(card, relationPool)
                is CompareCardFull -> genCompare(card)
            }
        }
    }

    private fun genShi(card: ShiCardFull, pool: List<String>): QuizQuestion? {
        val example = card.card.examples.randomOrNull() ?: return null
        val distract = pool.filter { it != example.yixiang }.shuffled().take(3)
        if (distract.size < 3) return null
        val options = (distract + example.yixiang).shuffled()
        return QuizQuestion(
            cardId = card.id,
            question = "「${example.sentence}」中「${card.card.char}」的意思是？",
            options = options,
            correctIndex = options.indexOf(example.yixiang),
            analysis = "正确义项：${example.yixiang}。译文：${example.translation}",
            sentence = example.sentence,
            source = example.source
        )
    }

    private fun genXu(card: XuCardFull, pool: List<String>): QuizQuestion? {
        val step = card.card.judgeFlow.randomOrNull() ?: return null
        val distract = pool.filter { it != step.relation }.shuffled().take(3)
        if (distract.size < 3) return null
        val options = (distract + step.relation).shuffled()
        return QuizQuestion(
            cardId = card.id,
            question = "「${step.example}」中「${card.card.char}」表示什么关系？",
            options = options,
            correctIndex = options.indexOf(step.relation),
            analysis = "${step.relation}：译为「${step.translation}」。${step.translationFull}",
            sentence = step.example,
            source = step.source
        )
    }

    private fun genCompare(card: CompareCardFull): QuizQuestion? {
        val correct = card.card.table.filter { it.left.startsWith("✅") }.randomOrNull() ?: return null
        val correctSentence = correct.left.removePrefix("✅").trim()
        val wrongPool = card.card.table.filter { !it.left.startsWith("✅") }
            .map { it.left.removePrefix("❌").trim() } +
            card.card.table.map { it.right.removePrefix("✅").removePrefix("❌").trim() }
        val distract = wrongPool.filter { it.isNotBlank() && it != correctSentence }.shuffled().take(3)
        if (distract.size < 3) return null
        val options = (distract + correctSentence).shuffled()
        return QuizQuestion(
            cardId = card.id,
            question = "下列哪句中的「${card.card.charLeft}」表示「${correct.relation}」？",
            options = options,
            correctIndex = options.indexOf(correctSentence),
            analysis = "${correct.relation}关系例句：$correctSentence",
            sentence = correctSentence
        )
    }
}
