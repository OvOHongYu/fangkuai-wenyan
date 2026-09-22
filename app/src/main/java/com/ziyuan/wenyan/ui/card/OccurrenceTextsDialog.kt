package com.ziyuan.wenyan.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.data.model.TextPiece
import com.ziyuan.wenyan.data.repository.TextRepository
import com.ziyuan.wenyan.ui.components.OreChip
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.theme.PixelFontFamily
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// "出现篇目"弹窗 ViewModel：按 字 + 义项 查询收录篇目
@HiltViewModel
class OccurrenceDialogViewModel @Inject constructor(
    private val textRepository: TextRepository
) : ViewModel() {

    // 某字某义项出现过的全部篇目（Flow 由 UI 层 collect）
    fun observeTexts(char: String, yixiang: String) =
        textRepository.observeTextsForOccurrence(char, yixiang)
}

// "「字」= 义项 出现的篇目"选择弹窗：点击篇目进入定位阅读窗口
@Composable
fun OccurrenceTextsDialog(
    char: String,
    yixiang: String,
    onDismiss: () -> Unit,
    onOpenText: (String) -> Unit
) {
    val viewModel: OccurrenceDialogViewModel = hiltViewModel()
    val texts by remember(char, yixiang) { viewModel.observeTexts(char, yixiang) }
        .collectAsStateWithLifecycle(initialValue = null)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.panel,
        title = {
            Text(
                text = "「$char」= $yixiang 出现的篇目",
                color = OreTextPrimary,
                fontFamily = PixelFontFamily,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        },
        text = {
            val list = texts
            if (list == null) {
                Text("加载中…", color = OreTextSecondary, fontSize = 14.sp)
            } else if (list.isEmpty()) {
                Text("该义项在现有教材篇目中暂未出现", color = OreTextSecondary, fontSize = 14.sp)
            } else {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 4.dp)
                ) {
                    list.forEach { piece ->
                        TextPieceRow(piece = piece, onClick = { onOpenText(piece.id) })
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭", color = OreGreen) }
        }
    )
}

// 单条篇目行：篇名 + 作者·朝代 + 册别 Chip
@Composable
private fun TextPieceRow(piece: TextPiece, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.chipBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = piece.title,
            color = OreTextPrimary,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val meta = listOf(piece.author, piece.dynasty).filter { it.isNotBlank() }
                .joinToString("·")
            Text(
                text = meta.ifBlank { "佚名" },
                color = OreTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(8.dp))
            if (piece.book.isNotBlank()) OreChip(text = piece.book)
        }
    }
}
