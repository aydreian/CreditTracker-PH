package com.example.credittrackph.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.credittrackph.data.model.ExpenseCategory

fun ExpenseCategory.getIcon(): ImageVector {
    return when (this) {
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
        ExpenseCategory.SHOPPING -> Icons.Default.ShoppingBag
        ExpenseCategory.UTILITIES -> Icons.Default.Bolt
        ExpenseCategory.HEALTH -> Icons.Default.MedicalServices
        ExpenseCategory.ENTERTAINMENT -> Icons.Default.Movie
        ExpenseCategory.TRAVEL -> Icons.Default.Flight
        ExpenseCategory.EDUCATION -> Icons.Default.School
        ExpenseCategory.GROCERIES -> Icons.Default.LocalGroceryStore
        ExpenseCategory.ONLINE -> Icons.Default.Computer
        ExpenseCategory.OTHER -> Icons.Default.Category
    }
}
