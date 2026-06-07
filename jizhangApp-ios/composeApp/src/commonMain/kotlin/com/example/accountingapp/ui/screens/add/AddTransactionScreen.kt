package com.example.accountingapp.ui.screens.add

import com.example.accountingapp.platform.showPlatformToast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// LocalContext removed — Toast now uses showPlatformToast()
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// viewModel 由 NavGraph 注入
import com.example.accountingapp.data.model.TransactionType
import com.example.accountingapp.ui.components.TimeWheelDialog
import com.example.accountingapp.ui.theme.BrownDark
import com.example.accountingapp.ui.theme.BrownLight
import com.example.accountingapp.ui.theme.BrownMedium
import com.example.accountingapp.ui.theme.Cream
import com.example.accountingapp.ui.theme.Mint
import com.example.accountingapp.ui.theme.MintLight
import com.example.accountingapp.ui.theme.Pink
import com.example.accountingapp.ui.theme.PinkLight
import com.example.accountingapp.ui.theme.White
import com.example.accountingapp.util.currentHour
import com.example.accountingapp.util.format0
import com.example.accountingapp.util.format2
import com.example.accountingapp.util.startOfDay
import com.example.accountingapp.util.toHHmm
import com.example.accountingapp.util.toHour
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
fun AddTransactionScreen(
    onBack: () -> Unit,
    viewModel: AddTransactionViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    LaunchedEffect(state.saved) {
        if (state.saved) {
            showPlatformToast("保存成功")
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        // 1. White status bar — statusBarsPadding on Spacer makes it exactly status bar height
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(White)
        )

        // 2. Title bar — blends into Cream page background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Cream)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "记一笔",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // 标题栏 ↔ 支出/收入按钮：20dp
            Spacer(modifier = Modifier.height(20.dp))

            TypeToggle(
                selectedType = state.type,
                onTypeSelected = { viewModel.setType(it) }
            )

            // 支出/收入按钮 ↔ "金额"文字：24dp
            Spacer(modifier = Modifier.height(24.dp))

            AmountInput(
                amount = state.amount,
                onAmountChange = { viewModel.setAmount(it) }
            )

            // "点击上方输入金额"提示 ↔ "选择分类"文字：24dp
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "选择分类",
                style = MaterialTheme.typography.titleMedium
            )
            CategoryGrid(
                categories = allCategories.filter { it.type == state.type },
                selectedId = state.selectedCategoryId,
                onSelect = { viewModel.selectCategory(it) }
            )

            // 分类图标 ↔ "日期与时间"文字：24dp
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "日期与时间",
                style = MaterialTheme.typography.titleMedium
            )
            DateTimeSelector(
                date = state.date,
                onDateSelected = { viewModel.setDate(it) },
                onTimeChanged = { hour, minute -> viewModel.setTime(hour, minute) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Note input
            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.setNote(it) },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Pink,
                    cursorColor = Pink,
                    focusedLabelColor = Pink
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save button
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Pink,
                    contentColor = White
                ),
                enabled = state.amount.isNotBlank()
                        && state.selectedCategoryId != null
                        && (state.amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text(
                    text = "保存",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TypeToggle(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Cream)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TypeButton(
            modifier = Modifier.weight(1f),
            emoji = "💸",
            label = "支出",
            selected = selectedType == TransactionType.EXPENSE,
            selectedColor = Pink,
            selectedBg = PinkLight,
            onClick = { onTypeSelected(TransactionType.EXPENSE) }
        )
        TypeButton(
            modifier = Modifier.weight(1f),
            emoji = "💰",
            label = "收入",
            selected = selectedType == TransactionType.INCOME,
            selectedColor = Mint,
            selectedBg = MintLight,
            onClick = { onTypeSelected(TransactionType.INCOME) }
        )
    }
}

@Composable
private fun TypeButton(
    modifier: Modifier,
    emoji: String,
    label: String,
    selected: Boolean,
    selectedColor: androidx.compose.ui.graphics.Color,
    selectedBg: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (selected) selectedBg else White)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) selectedColor else BrownMedium
            )
        }
    }
}

@Composable
private fun AmountInput(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val centeredTextStyle = MaterialTheme.typography.headlineLarge.copy(
        textAlign = TextAlign.Center,
        color = BrownDark
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "金额",
            style = MaterialTheme.typography.labelLarge,
            color = BrownMedium
        )
        // "金额"文字 ↔ 金额输入框：16dp
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = centeredTextStyle,
            placeholder = {
                if (!isFocused && amount.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "¥ 0.00",
                            style = centeredTextStyle,
                            color = BrownLight
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Pink,
                unfocusedBorderColor = BrownLight.copy(alpha = 0.3f),
                cursorColor = Pink
            )
        )
        // 金额输入框 ↔ "点击上方输入金额"提示：8dp
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击上方输入金额",
            fontSize = 12.sp,
            color = BrownLight.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CategoryGrid(
    categories: List<com.example.accountingapp.data.model.Category>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    val rows = categories.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { category ->
                    val selected = category.id == selectedId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) PinkLight else White.copy(alpha = 0.4f))
                            .clickable { onSelect(category.id) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = category.emoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = category.name,
                                fontSize = 12.sp,
                                color = if (selected) Pink else BrownMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                // Fill remaining columns with empty space to keep alignment
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeSelector(
    date: Long,
    onDateSelected: (Long) -> Unit,
    onTimeChanged: (hour: Int, minute: Int) -> Unit
) {
    val dateStr = date.toYYYYMMdd()
    val timeStr = date.toHHmm()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Date picker
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .clickable { showDatePicker = true }
                .border(1.dp, BrownLight.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CalendarMonth, null, tint = Pink, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(dateStr, fontSize = 14.sp, color = BrownDark)
        }

        // Time picker - opens custom scrollable dialog
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable { showTimePicker = true }
                .border(1.dp, BrownLight.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🕐", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(timeStr, fontSize = 14.sp, color = BrownDark, fontWeight = FontWeight.Medium)
        }
    }

    // M3 Date picker dialog — same style as edit transaction dialog
    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { millis ->
                        onDateSelected(millis.withTime(date.toHour(), date.toMinute()))
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

    // Custom styled time picker dialog
    if (showTimePicker) {
        TimeWheelDialog(
            initialHour = date.toHour(),
            initialMinute = date.toMinute(),
            onConfirm = { hour, minute ->
                onTimeChanged(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

