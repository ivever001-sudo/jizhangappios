package com.example.accountingapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accountingapp.data.model.Transaction
import com.example.accountingapp.data.model.TransactionType
import com.example.accountingapp.ui.theme.BrickRose
import com.example.accountingapp.ui.theme.BrownLight
import com.example.accountingapp.ui.theme.BrownMedium
import com.example.accountingapp.ui.theme.SageGreen
import com.example.accountingapp.ui.theme.SquircleShape
import com.example.accountingapp.ui.theme.White
import com.example.accountingapp.util.format2
import com.example.accountingapp.util.toMMddHHmm

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    categoryEmoji: String,
    categoryName: String,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) SageGreen else BrickRose
    val sign = if (isIncome) "+" else "-"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = onLongClick
                    )
                } else Modifier
            ),
        shape = SquircleShape(16.dp),
        border = BorderStroke(1.dp, White.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category emoji
            Text(
                text = categoryEmoji,
                fontSize = 28.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Category name & date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.date.toMMddHHmm(),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrownLight
                )
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$sign¥${transaction.amount.format2()}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrownMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
