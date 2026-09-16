package com.example.credittrackph.presentation.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.credittrackph.data.db.entity.CardEntity
import com.example.credittrackph.data.model.ExpenseCategory
import com.example.credittrackph.data.model.InterestType
import com.example.credittrackph.domain.calculator.InstallmentCalculator
import com.example.credittrackph.presentation.viewmodel.ExpenseViewModel
import com.example.credittrackph.theme.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseScreen(
    card: CardEntity,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    installmentCalculator: InstallmentCalculator,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var purchaseDateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var isInstallment by remember { mutableStateOf(false) }
    var installmentMonths by remember { mutableStateOf(3) }
    var interestType by remember { mutableStateOf(InterestType.ZERO_PERCENT) }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val profileViewModel: com.example.credittrackph.presentation.viewmodel.ProfileViewModel = hiltViewModel()
    val allProfiles by profileViewModel.allProfiles.collectAsState()
    var selectedProfileId by remember { mutableStateOf<Int?>(null) }
    var profileDropdownExpanded by remember { mutableStateOf(false) }

    // Set default to main user if not selected
    LaunchedEffect(allProfiles) {
        if (selectedProfileId == null) {
            selectedProfileId = allProfiles.find { it.isMainUser }?.id
        }
    }

    val amountDouble = amount.toDoubleOrNull() ?: 0.0
    val monthlyPreview0 = if (amountDouble > 0 && installmentMonths > 0) amountDouble / installmentMonths else 0.0
    val monthlyPreviewWithInterest = if (amountDouble > 0 && installmentMonths > 0) {
        try { installmentCalculator.calculateWithInterest(amountDouble, installmentMonths, card.bank).monthlyAmortization } catch (e: Exception) { 0.0 }
    } else 0.0

    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = purchaseDateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { purchaseDateMs = it }
                    showDatePicker = false
                }) { Text("OK", color = Emerald500) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.White.copy(0.5f)) } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface900)
            )
        },
        containerColor = Surface950
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card indicator
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(card.cardColorArgb).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CreditCard, null, tint = Emerald400)
                    Spacer(Modifier.width(8.dp))
                    Text("${card.label} •••• ${card.lastFourDigits}", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }

            // Merchant
            FormSection("Merchant / Description") {
                OutlinedTextField(
                    value = merchant, onValueChange = { merchant = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Jollibee, Lazada, SM Dept Store", color = Color.White.copy(0.4f)) },
                    colors = outlinedTextFieldColors(), singleLine = true
                )
            }

            // Amount
            FormSection("Amount (PHP)") {
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00", color = Color.White.copy(0.4f)) },
                    colors = outlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("₱", color = Emerald400, fontWeight = FontWeight.Bold) },
                    singleLine = true
                )
            }

            // Who Swiped
            if (allProfiles.isNotEmpty()) {
                FormSection("Who Swiped?") {
                    ExposedDropdownMenuBox(
                        expanded = profileDropdownExpanded,
                        onExpandedChange = { profileDropdownExpanded = !profileDropdownExpanded }
                    ) {
                        val selectedName = allProfiles.find { it.id == selectedProfileId }?.name ?: "Select Profile"
                        OutlinedTextField(
                            value = selectedName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = profileDropdownExpanded) },
                            colors = outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = profileDropdownExpanded,
                            onDismissRequest = { profileDropdownExpanded = false }
                        ) {
                            allProfiles.forEach { profile ->
                                DropdownMenuItem(
                                    text = { Text(profile.name) },
                                    onClick = {
                                        selectedProfileId = profile.id
                                        profileDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Category
            FormSection("Category") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpenseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.emoji} ${cat.displayName}", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald500,
                                selectedLabelColor = Surface950
                            )
                        )
                    }
                }
            }

            // Purchase date
            FormSection("Purchase Date") {
                OutlinedTextField(
                    value = sdf.format(Date(purchaseDateMs)),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = Emerald400) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledBorderColor = Surface700,
                        disabledContainerColor = Surface800
                    )
                )
            }

            // Installment toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Installment", color = Color.White, fontWeight = FontWeight.Medium)
                    Text("Split into monthly payments", color = Color.White.copy(0.5f), fontSize = 12.sp)
                }
                Switch(
                    checked = isInstallment,
                    onCheckedChange = { isInstallment = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Surface950, checkedTrackColor = Emerald500)
                )
            }

            // Installment options
            if (isInstallment) {
                // Months selector
                FormSection("Number of Months") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 6, 9, 12, 24, 36).forEach { months ->
                            FilterChip(
                                selected = installmentMonths == months,
                                onClick = { installmentMonths = months },
                                label = { Text("${months}x", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Emerald500,
                                    selectedLabelColor = Surface950
                                )
                            )
                        }
                    }
                }

                // Interest type
                FormSection("Interest Type") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        InterestType.entries.filter { it != InterestType.UNCERTAIN }.forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { interestType = type }
                            ) {
                                RadioButton(
                                    selected = interestType == type,
                                    onClick = { interestType = type },
                                    colors = RadioButtonDefaults.colors(selectedColor = Emerald500)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        when(type) {
                                            InterestType.ZERO_PERCENT -> "🎉 0% Interest (Promo / Partner Merchant)"
                                            InterestType.WITH_INTEREST -> "🏦 With Interest (${card.bank.shortCode} ${String.format("%.1f", card.bank.monthlyInterestRate * 100)}%/month)"
                                            else -> ""
                                        },
                                        color = Color.White, fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Preview
                if (amountDouble > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Surface800),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Payment Preview", color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            HorizontalDivider(color = Surface700)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("🎉 0% option:", color = Color.White.copy(0.8f), fontSize = 13.sp)
                                Text(
                                    "₱${String.format("%,.2f", monthlyPreview0)}/mo  →  Total: ₱${String.format("%,.2f", amountDouble)}",
                                    color = GreenSuccess, fontSize = 13.sp, fontWeight = FontWeight.Medium
                                )
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("🏦 With interest:", color = Color.White.copy(0.8f), fontSize = 13.sp)
                                Text(
                                    "₱${String.format("%,.2f", monthlyPreviewWithInterest)}/mo  →  Total: ₱${String.format("%,.2f", monthlyPreviewWithInterest * installmentMonths)}",
                                    color = YellowWarn, fontSize = 13.sp, fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Notes
            FormSection("Notes (optional)") {
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add any notes here...", color = Color.White.copy(0.4f)) },
                    colors = outlinedTextFieldColors(),
                    maxLines = 3
                )
            }

            Spacer(Modifier.height(8.dp))

            // Save
            Button(
                onClick = {
                    if (merchant.isNotBlank() && amountDouble > 0) {
                        viewModel.addManualExpense(
                            card = card,
                            merchantName = merchant,
                            category = selectedCategory,
                            amount = amountDouble,
                            purchaseDateMs = purchaseDateMs,
                            isInstallment = isInstallment,
                            installmentMonths = installmentMonths,
                            interestType = interestType,
                            note = note,
                            profileId = selectedProfileId
                        )
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                shape = RoundedCornerShape(12.dp),
                enabled = merchant.isNotBlank() && amountDouble > 0
            ) {
                Icon(Icons.Default.Check, null, tint = Surface950)
                Spacer(Modifier.width(8.dp))
                Text("Save Expense", color = Surface950, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
