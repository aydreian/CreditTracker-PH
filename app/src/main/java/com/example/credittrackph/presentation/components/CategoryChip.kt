package com.example.credittrackph.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.credittrackph.data.model.ExpenseCategory
import com.example.credittrackph.theme.appPrimaryColor
import com.example.credittrackph.theme.appSoftSuccessColor

/**
 * Pill-shaped category chip with icon + label.
 * Used in TransactionsScreen rows to replace plain text category labels.
 */
@Composable
fun CategoryChip(
    category: ExpenseCategory,
    modifier: Modifier = Modifier
) {
    Surface(
        color    = appSoftSuccessColor(),
        shape    = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector    = category.getIcon(),
                contentDescription = null,
                modifier       = Modifier.size(11.dp),
                tint           = appPrimaryColor()
            )
            Text(
                text       = category.displayName,
                fontSize   = 11.sp,
                color      = appPrimaryColor(),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
