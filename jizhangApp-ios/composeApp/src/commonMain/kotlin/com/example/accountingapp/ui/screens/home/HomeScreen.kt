package com.example.accountingapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.roundToInt
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// viewModel 由 NavGraph 注入
import com.example.accountingapp.data.model.Transaction
import com.example.accountingapp.data.model.TransactionType
import com.example.accountingapp.ui.components.TimeWheelDialog
import com.example.accountingapp.ui.components.TransactionItem
import com.example.accountingapp.ui.theme.BrickRose
import com.example.accountingapp.ui.theme.BrownDark
import com.example.accountingapp.ui.theme.BrownLight
import com.example.accountingapp.ui.theme.BrownMedium
import com.example.accountingapp.ui.theme.Cream
import com.example.accountingapp.ui.theme.DustyRose
import com.example.accountingapp.ui.theme.Pink
import com.example.accountingapp.ui.theme.PinkDark
import com.example.accountingapp.ui.theme.SageGreen
import com.example.accountingapp.ui.theme.SoftApricot
import com.example.accountingapp.ui.theme.SquircleShape
import com.example.accountingapp.ui.theme.White
import com.example.accountingapp.util.currentHour
import com.example.accountingapp.util.format0
import com.example.accountingapp.util.format2
import com.example.accountingapp.util.startOfDay
import com.example.accountingapp.util.toHHmm
import com.example.accountingapp.util.toHour
import com.example.accountingapp.util.toMMdd
import com.example.accountingapp.util.toMinute
import com.example.accountingapp.util.toYYYYMMdd
import com.example.accountingapp.util.withTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsState()

    // Edit dialog state
    var txToEdit by remember { mutableStateOf<Transaction?>(null) }
    // Date picker dialog state
    var showDatePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 80.dp
            )
        ) {
            // Greeting header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                GreetingHeader(state.currentMonth)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Date picker bar
            item {
                DateSelectorBar(
                    selectedDate = state.selectedDate,
                    onDatePickerClick = { showDatePicker = true },
                    onGoToToday = { viewModel.goToToday() }
                )
            }

            // Monthly summary cards
            item {
                val displayBalance = when {
                    state.showAlternate -> {
                        when (state.balanceType) {
                            BalanceType.MONTHLY -> state.totalBalance
                            BalanceType.TOTAL -> state.monthBalance
                        }
                    }
                    else -> {
                        when (state.balanceType) {
                            BalanceType.MONTHLY -> state.monthBalance
                            BalanceType.TOTAL -> state.totalBalance
                        }
                    }
                }
                val displayLabel = when {
                    state.showAlternate -> {
                        when (state.balanceType) {
                            BalanceType.MONTHLY -> "总结余"
                            BalanceType.TOTAL -> "月结余"
                        }
                    }
                    else -> {
                        when (state.balanceType) {
                            BalanceType.MONTHLY -> "月结余"
                            BalanceType.TOTAL -> "总结余"
                        }
                    }
                }
                MonthlySummaryCards(
                    income = state.monthIncome,
                    expense = state.monthExpense,
                    balance = displayBalance,
                    balanceLabel = displayLabel,
                    onToggleBalance = { viewModel.toggleBalance() },
                    onSwitchDefault = { viewModel.switchDefaultBalance() }
                )
            }

            // Transactions header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (state.selectedDate != null) "当日账单" else "全部账单",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${state.currentYear}年${state.currentMonth}月",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrownLight
                    )
                }
            }

            // Transaction list
            if (state.recentTransactions.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(
                    items = state.recentTransactions,
                    key = { it.transaction.id }
                ) { tx ->
                    SwipeableTransactionItem(
                        transaction = tx.transaction,
                        categoryEmoji = tx.categoryEmoji,
                        categoryName = tx.categoryName,
                        onEdit = { txToEdit = tx.transaction },
                        onDelete = { viewModel.deleteTransaction(tx.transaction) }
                    )
                }
            }

        }

        // FAB — iOS-style squircle with pink-to-coral gradient
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(60.dp)
                .shadow(12.dp, SquircleShape(20.dp))
                .clip(SquircleShape(20.dp))
                .background(brush = Brush.linearGradient(listOf(DustyRose, SoftApricot)))
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "记一笔",
                tint = White,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Edit dialog
    if (txToEdit != null) {
        EditTransactionDialog(
            transaction = txToEdit!!,
            allCategories = state.allCategories,
            onConfirm = { updated ->
                viewModel.updateTransaction(updated)
                txToEdit = null
            },
            onDismiss = { txToEdit = null }
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.selectDate(millis.startOfDay())
                    }
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DateSelectorBar(
    selectedDate: Long?,
    onDatePickerClick: () -> Unit,
    onGoToToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedDate != null) {
            val dateStr = selectedDate.toMMdd()
                TextButton(onClick = onGoToToday) {
                    Text("← 查看全部", color = Pink, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "查看: $dateStr",
                style = MaterialTheme.typography.labelMedium,
                color = BrownDark
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onDatePickerClick) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "选择日期",
                tint = Pink,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun GreetingHeader(month: Int) {
    val hour = currentHour()
    val greeting = when {
        hour < 6 -> "夜深了"
        hour < 9 -> "早上好"
        hour < 12 -> "上午好"
        hour < 14 -> "中午好"
        hour < 18 -> "下午好"
        else -> "晚上好"
    }

    Column {
        Text(
            text = "$greeting 👋",
            fontSize = 14.sp,
            color = BrownMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "小账簿",
            style = MaterialTheme.typography.headlineLarge,
            color = BrownDark
        )
    }
}

@Composable
private fun MonthlySummaryCards(
    income: Double,
    expense: Double,
    balance: Double,
    balanceLabel: String,
    onToggleBalance: () -> Unit,
    onSwitchDefault: () -> Unit
) {
    val glassBg = White.copy(alpha = 0.5f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            emoji = "💰",
            label = "收入",
            amount = income,
            amountColor = SageGreen,
            bgColor = glassBg
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            emoji = "💸",
            label = "支出",
            amount = expense,
            amountColor = BrickRose,
            bgColor = glassBg
        )
        SummaryCard(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onToggleBalance() },
                        onLongPress = { onSwitchDefault() }
                    )
                },
            emoji = "💎",
            label = balanceLabel,
            amount = balance,
            amountColor = BrownDark,
            bgColor = glassBg
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    emoji: String,
    label: String,
    amount: Double,
    amountColor: androidx.compose.ui.graphics.Color,
    bgColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = BrownMedium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "¥${amount.format0()}",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                color = amountColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📝", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "还没有账单记录",
            style = MaterialTheme.typography.bodyMedium,
            color = BrownLight
        )
        Text(
            text = "点击右下角 + 记一笔吧",
            style = MaterialTheme.typography.labelSmall,
            color = BrownLight
        )
    }
}

// --- Swipeable transaction wrapper ---
@Composable
private fun SwipeableTransactionItem(
    transaction: Transaction,
    categoryEmoji: String,
    categoryName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val maxSwipePx = with(density) { 100.dp.toPx() }
    val dragThresholdPx = with(density) { 40.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        val maxSlidePx = with(density) { 58.dp.toPx() }
        val visualOffset = offsetX.coerceAtLeast(-maxSlidePx)

        // Main card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(visualOffset.roundToInt(), 0) }
                .pointerInput(transaction.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            offsetX = if (offsetX < -dragThresholdPx) -maxSwipePx else 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-maxSwipePx, 0f)
                        }
                    )
                }
        ) {
            TransactionItem(
                transaction = transaction,
                categoryEmoji = categoryEmoji,
                categoryName = categoryName,
                onLongClick = onEdit
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

// --- Edit transaction dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTransactionDialog(
    transaction: Transaction,
    allCategories: List<com.example.accountingapp.data.model.Category>,
    onConfirm: (Transaction) -> Unit,
    onDismiss: () -> Unit
) {
    var editType by remember { mutableStateOf(transaction.type) }
    var editAmount by remember { mutableStateOf(transaction.amount.format2()) }
    var editCategoryId by remember { mutableStateOf(transaction.categoryId) }
    var editNote by remember { mutableStateOf(transaction.note) }
    var editDate by remember { mutableStateOf(transaction.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val filteredCats = allCategories.filter { it.type == editType }
    val dateStr = editDate.toYYYYMMdd()
    val timeStr = editDate.toHHmm()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("编辑账单", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (type in listOf(TransactionType.EXPENSE, TransactionType.INCOME)) {
                        val selected = editType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) Pink else BrownLight.copy(alpha = 0.1f))
                                .clickable {
                                    editType = type
                                    // Reset category when type changes
                                    editCategoryId = filteredCats.firstOrNull()?.id ?: 0
                                }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (type == TransactionType.EXPENSE) "💸 支出" else "💰 收入",
                                color = if (selected) White else BrownMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = editAmount,
                    onValueChange = { v ->
                        if (v.isEmpty() || v.matches(Regex("^\\d*\\.?\\d{0,2}$"))) editAmount = v
                    },
                    label = { Text("金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category selector
                if (filteredCats.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        filteredCats.chunked(4).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { cat ->
                                    val selected = cat.id == editCategoryId
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (selected) Pink.copy(alpha = 0.15f) else BrownLight.copy(alpha = 0.05f))
                                            .border(
                                                if (selected) 1.5.dp else 0.dp,
                                                if (selected) Pink else Color.Transparent,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { editCategoryId = cat.id }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${cat.emoji} ${cat.name}", fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                                repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }

                // Date & time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, BrownLight.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .clickable { showDatePicker = true }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, tint = Pink, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(dateStr, fontSize = 13.sp)
                    }
                    // Time
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, BrownLight.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .clickable { showTimePicker = true }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🕐", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(timeStr, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Note
                OutlinedTextField(
                    value = editNote,
                    onValueChange = { editNote = it },
                    label = { Text("备注") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = editAmount.toDoubleOrNull() ?: return@TextButton
                if (amt <= 0) return@TextButton
                onConfirm(
                    transaction.copy(
                        type = editType,
                        amount = amt,
                        categoryId = editCategoryId,
                        note = editNote,
                        date = editDate
                    )
                )
            }) {
                Text("保存", fontWeight = FontWeight.Bold, color = Pink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // Date picker within edit dialog
    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = editDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { millis ->
                        editDate = millis.withTime(editDate.toHour(), editDate.toMinute())
                    }
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = dpState)
        }
    }

    // Time picker within edit dialog
    if (showTimePicker) {
        TimeWheelDialog(
            initialHour = editDate.toHour(),
            initialMinute = editDate.toMinute(),
            onConfirm = { hour, minute ->
                editDate = editDate.withTime(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
