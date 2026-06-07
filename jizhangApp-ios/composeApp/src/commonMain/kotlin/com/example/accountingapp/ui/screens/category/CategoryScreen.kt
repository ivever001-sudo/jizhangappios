package com.example.accountingapp.ui.screens.category

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// viewModel 由 NavGraph 注入，不再使用 viewModel() 默认值
import com.example.accountingapp.data.model.Category
import com.example.accountingapp.data.model.TransactionType
import com.example.accountingapp.ui.theme.BrickRose
import com.example.accountingapp.ui.theme.BrownDark
import com.example.accountingapp.ui.theme.BrownLight
import com.example.accountingapp.ui.theme.BrownMedium
import com.example.accountingapp.ui.theme.CardGreen
import com.example.accountingapp.ui.theme.CardPink
import com.example.accountingapp.ui.theme.Cream
import com.example.accountingapp.ui.theme.Mint
import com.example.accountingapp.ui.theme.MintDark
import com.example.accountingapp.ui.theme.Pink
import com.example.accountingapp.ui.theme.PinkDark
import com.example.accountingapp.ui.theme.SquircleShape
import com.example.accountingapp.ui.theme.White
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel
) {
    val state by viewModel.uiState.collectAsState()

    val localExpense = remember { mutableStateListOf<Category>() }
    val localIncome = remember { mutableStateListOf<Category>() }

    LaunchedEffect(state.expenseCategories) {
        if (localExpense.toList() != state.expenseCategories) {
            localExpense.clear()
            localExpense.addAll(state.expenseCategories)
        }
    }
    LaunchedEffect(state.incomeCategories) {
        if (localIncome.toList() != state.incomeCategories) {
            localIncome.clear()
            localIncome.addAll(state.incomeCategories)
        }
    }

    val density = LocalDensity.current
    val itemHeightPx = with(density) { (62.dp + 10.dp).toPx() }

    Box(modifier = Modifier.fillMaxSize().background(Cream)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 20.dp
            )
        ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = "分类管理",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrownDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "左滑删除，长按拖拽排序",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrownLight
                )
            }
        }

        item {
            SectionHeader(
                emoji = "💸",
                title = "支出分类",
                count = localExpense.size,
                bgColor = CardPink,
                textColor = PinkDark
            )
        }

        itemsIndexed(localExpense, key = { _, cat -> cat.id }) { _, category ->
            CategoryRow(
                modifier = Modifier.animateItemPlacement(tween(200)),
                category = category,
                allItems = localExpense,
                itemHeightPx = itemHeightPx,
                onDelete = { viewModel.deleteCategory(category) },
                onDragEnd = { viewModel.updateCategoryOrder(localExpense.toList()) }
            )
        }

        item {
            AddCategoryButton(
                label = "+ 添加支出分类",
                bgColor = Pink,
                onClick = { viewModel.showAddDialog(TransactionType.EXPENSE) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            SectionHeader(
                emoji = "💰",
                title = "收入分类",
                count = localIncome.size,
                bgColor = CardGreen,
                textColor = MintDark
            )
        }

        itemsIndexed(localIncome, key = { _, cat -> cat.id }) { _, category ->
            CategoryRow(
                modifier = Modifier.animateItemPlacement(tween(200)),
                category = category,
                allItems = localIncome,
                itemHeightPx = itemHeightPx,
                onDelete = { viewModel.deleteCategory(category) },
                onDragEnd = { viewModel.updateCategoryOrder(localIncome.toList()) }
            )
        }

        item {
            AddCategoryButton(
                label = "+ 添加收入分类",
                bgColor = Mint,
                onClick = { viewModel.showAddDialog(TransactionType.INCOME) }
            )
        }

        }

    }

    if (state.showAddDialog) {
        AddCategoryDialog(
            type = state.addDialogType,
            onConfirm = { name, emoji ->
                viewModel.addCategory(name, emoji, state.addDialogType)
            },
            onDismiss = { viewModel.dismissAddDialog() }
        )
    }
}

@Composable
private fun AddCategoryButton(
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor)
    ) {
        Text(label, color = White, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AddCategoryDialog(
    type: TransactionType,
    onConfirm: (name: String, emoji: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(if (type == TransactionType.EXPENSE) "💸" else "💰") }

    val title = if (type == TransactionType.EXPENSE) "添加支出分类" else "添加收入分类"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("图标 Emoji") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.ifBlank { "未命名" }, emoji.ifBlank { "📌" }) },
                enabled = name.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun SectionHeader(
    emoji: String,
    title: String,
    count: Int,
    bgColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$count 个",
            style = MaterialTheme.typography.labelSmall,
            color = BrownLight
        )
    }
}

@Composable
private fun CategoryRow(
    modifier: Modifier = Modifier,
    category: Category,
    allItems: MutableList<Category>,
    itemHeightPx: Float,
    onDelete: () -> Unit,
    onDragEnd: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxSwipePx = with(LocalDensity.current) { 100.dp.toPx() }
    val dragThresholdPx = with(LocalDensity.current) { 40.dp.toPx() }
    val maxSlidePx = with(LocalDensity.current) { 58.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        val visualOffset = offsetX.coerceAtLeast(-maxSlidePx)

        // Main card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(visualOffset.roundToInt(), 0) }
                .pointerInput(category.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            offsetX = if (offsetX < -dragThresholdPx) -maxSwipePx else 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-maxSwipePx, 0f)
                        }
                    )
                }
                .clip(SquircleShape(16.dp))
                .background(White.copy(alpha = 0.6f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = category.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            var dragVersion by remember { mutableIntStateOf(0) }
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "长按拖拽排序",
                tint = BrownLight,
                modifier = Modifier
                    .size(36.dp)
                    .pointerInput(category.id, dragVersion) {
                        var totalDrag = 0f
                        val catId = category.id
                        detectDragGesturesAfterLongPress(
                            onDragStart = { totalDrag = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount.y
                                val steps = (totalDrag / itemHeightPx).toInt()
                                if (steps != 0) {
                                    val cur = allItems.indexOfFirst { it.id == catId }
                                    if (cur < 0) return@detectDragGesturesAfterLongPress
                                    val target = (cur + steps).coerceIn(0, allItems.lastIndex)
                                    if (target != cur) {
                                        val cat = allItems[cur]
                                        allItems.removeAt(cur)
                                        allItems.add(target, cat)
                                        totalDrag -= steps * itemHeightPx
                                    }
                                }
                            },
                            onDragEnd = {
                                onDragEnd()
                                dragVersion++
                            },
                            onDragCancel = { }
                        )
                    }
            )
        }

        // Delete button — no padding-end, centered in parent
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(50.dp)
                .graphicsLayer {
                    translationX = (58.dp.toPx() + visualOffset).coerceAtLeast(0f)
                    alpha = if (offsetX > -5f) 0f else 1f
                }
                .clip(SquircleShape(14.dp))
                .background(BrickRose)
                .clickable(enabled = offsetX < -30f) { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
