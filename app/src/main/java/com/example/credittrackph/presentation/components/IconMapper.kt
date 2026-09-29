package com.example.credittrackph.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.credittrackph.data.model.ExpenseCategory

/**
 * Maps each expense category to the most semantically appropriate Material icon.
 * Icons chosen to be instantly recognizable to Filipino users without any text.
 */
fun ExpenseCategory.getIcon(): ImageVector {
    return when (this) {
        ExpenseCategory.FOOD          -> Icons.Default.DinnerDining      // fork+knife plate
        ExpenseCategory.TRANSPORT     -> Icons.Default.DirectionsBus     // public transit PH context
        ExpenseCategory.SHOPPING      -> Icons.Default.ShoppingBag       // retail bag
        ExpenseCategory.UTILITIES     -> Icons.Default.Bolt               // electricity (Meralco)
        ExpenseCategory.HEALTH        -> Icons.Default.LocalHospital      // hospital cross
        ExpenseCategory.ENTERTAINMENT -> Icons.Default.LocalActivity      // ticket / activity
        ExpenseCategory.TRAVEL        -> Icons.Default.Flight             // airplane
        ExpenseCategory.EDUCATION     -> Icons.Default.MenuBook           // open book
        ExpenseCategory.GROCERIES     -> Icons.Default.LocalGroceryStore  // shopping cart
        ExpenseCategory.ONLINE        -> Icons.Default.ShoppingCart       // Shopee / Lazada context
        ExpenseCategory.OTHER         -> Icons.Default.Category           // generic grid
    }
}
