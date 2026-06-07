package com.example.accountingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accountingapp.ui.theme.BrownDark
import com.example.accountingapp.ui.theme.BrownLight
import com.example.accountingapp.ui.theme.Cream
import com.example.accountingapp.ui.theme.Pink
import com.example.accountingapp.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeWheelDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    val itemHeightDp = 48.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeightDp.toPx() }

    // Start at a large offset so we have room to scroll in both directions
    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = initialHour + 1000)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = initialMinute + 1000)

    // Continuously track center item during scroll for real-time highlighting
    LaunchedEffect(hourState) {
        snapshotFlow {
            val idx = hourState.firstVisibleItemIndex
            val offset = hourState.firstVisibleItemScrollOffset
            idx to offset
        }.collect { (idx, offset) ->
            val centerItem = idx + (offset / itemHeightPx + 0.5f).toInt()
            val value = centerItem % 24
            val corrected = if (value < 0) value + 24 else value
            if (corrected != selectedHour) {
                selectedHour = corrected
            }
        }
    }

    LaunchedEffect(minuteState) {
        snapshotFlow {
            val idx = minuteState.firstVisibleItemIndex
            val offset = minuteState.firstVisibleItemScrollOffset
            idx to offset
        }.collect { (idx, offset) ->
            val centerItem = idx + (offset / itemHeightPx + 0.5f).toInt()
            val value = centerItem % 60
            val corrected = if (value < 0) value + 60 else value
            if (corrected != selectedMinute) {
                selectedMinute = corrected
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间", fontWeight = FontWeight.Bold, color = BrownDark) },
        text = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.height(180.dp), verticalAlignment = Alignment.CenterVertically) {
                    WheelColumn(
                        items = (0..3000).map { it % 24 }, selected = selectedHour,
                        state = hourState, itemHeight = itemHeightDp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BrownDark,
                        modifier = Modifier.padding(horizontal = 8.dp))
                    WheelColumn(
                        items = (0..3000).map { it % 60 }, selected = selectedMinute,
                        state = minuteState, itemHeight = itemHeightDp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour, selectedMinute) }) {
                Text("确定", fontWeight = FontWeight.Bold, color = Pink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Cream,
        tonalElevation = 0.dp
    )
}

@Composable
private fun WheelColumn(
    items: List<Int>, selected: Int,
    state: androidx.compose.foundation.lazy.LazyListState,
    itemHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(160.dp).clip(RoundedCornerShape(12.dp))
            .background(BrownLight.copy(alpha = 0.05f))
    ) {
        LazyColumn(
            state = state, modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 56.dp, bottom = 56.dp)
        ) {
            items(items.size) { idx ->
                val value = items[idx]
                val isSelected = value == selected
                Box(Modifier.fillMaxWidth().height(itemHeight), contentAlignment = Alignment.Center) {
                    Text(
                        text = value.toString().padStart(2, '0'),  // String.format("%02d") → padStart
                        fontSize = if (isSelected) 28.sp else 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Pink else BrownLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
