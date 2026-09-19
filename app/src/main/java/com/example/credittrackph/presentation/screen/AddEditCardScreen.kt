package com.example.credittrackph.presentation.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.credittrackph.data.model.Bank
import com.example.credittrackph.data.model.CardType
import com.example.credittrackph.presentation.viewmodel.CardViewModel
import com.example.credittrackph.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CardViewModel = hiltViewModel()
) {
    var label by remember { mutableStateOf("") }
    var lastFour by remember { mutableStateOf("") }
    var selectedCardType by remember { mutableStateOf(CardType.VISA) }
    var selectedBank by remember { mutableStateOf(Bank.BDO) }
    var selectedColor by remember { mutableStateOf(CardColorPresets[0]) }
    var creditLimit by remember { mutableStateOf("") }
    var billingCutoffDay by remember { mutableStateOf("25") }
    var dueDay by remember { mutableStateOf("22") }
    var monthlyBudgetCap by remember { mutableStateOf("") }
    var bankExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Card", color = appTextColor(), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = appTextColor())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appCardColor())
            )
        },
        containerColor = appBackgroundColor()
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
            // Card Preview
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(selectedColor)
                        .padding(20.dp)
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(selectedBank.shortCode, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(selectedCardType.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Text(label.ifBlank { "Card Label" }, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(
                            "••••  ••••  ••••  ${lastFour.ifBlank { "0000" }}",
                            color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp, letterSpacing = 2.sp
                        )
                    }
                }
            }

            // Label
            FormSection("Card Label") {
                OutlinedTextField(
                    value = label, onValueChange = { label = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Daily Card, Travel Card", color = Color.White.copy(0.4f)) },
                    colors = outlinedTextFieldColors(),
                    singleLine = true
                )
            }

            // Last 4 digits
            FormSection("Last 4 Digits of Card") {
                OutlinedTextField(
                    value = lastFour,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) lastFour = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("1234", color = Color.White.copy(0.4f)) },
                    colors = outlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Bank selector
            FormSection("Bank") {
                ExposedDropdownMenuBox(
                    expanded = bankExpanded,
                    onExpandedChange = { bankExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedBank.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bankExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = bankExpanded,
                        onDismissRequest = { bankExpanded = false },
                        modifier = Modifier.background(appCardColor())
                    ) {
                        Bank.entries.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank.displayName, color = appTextColor()) },
                                onClick = { selectedBank = bank; bankExpanded = false }
                            )
                        }
                    }
                }
            }

            // Card type
            FormSection("Card Type") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CardType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedCardType == type,
                            onClick = { selectedCardType = type },
                            label = { Text(type.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald500,
                                selectedLabelColor = Surface950
                            )
                        )
                    }
                }
            }

            // Color picker
            FormSection("Card Color") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.height(90.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CardColorPresets) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (selectedColor == color) 2.dp else 0.dp,
                                    Emerald400, CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }

            // Credit limit
            FormSection("Credit Limit (PHP)") {
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("50000", color = appTextSubColor().copy(0.4f)) },
                    colors = outlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            // Billing cutoff and due day
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormSection("Billing Cutoff Day", Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = billingCutoffDay,
                        onValueChange = { if (it.length <= 2) billingCutoffDay = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("25", color = appTextSubColor().copy(0.4f)) },
                        colors = outlinedTextFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                FormSection("Payment Due Day", Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = dueDay,
                        onValueChange = { if (it.length <= 2) dueDay = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("22", color = appTextSubColor().copy(0.4f)) },
                        colors = outlinedTextFieldColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Monthly Budget Cap (optional)
            FormSection("Monthly Spending Cap (optional)") {
                OutlinedTextField(
                    value = monthlyBudgetCap,
                    onValueChange = { monthlyBudgetCap = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 5000 (0 = no cap)", color = appTextSubColor().copy(0.4f)) },
                    colors = outlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("₱", color = Emerald400, fontWeight = FontWeight.Bold) },
                    singleLine = true
                )
                Text(
                    "You'll get a warning when you reach 80% of this cap 🔔",
                    color = appTextSubColor(),
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    if (label.isNotBlank() && lastFour.length == 4) {
                        viewModel.addCard(
                            label = label,
                            lastFourDigits = lastFour,
                            cardType = selectedCardType,
                            bank = selectedBank,
                            colorArgb = selectedColor.toArgb().toLong(),
                            creditLimit = creditLimit.toDoubleOrNull() ?: 0.0,
                            billingCutoffDay = billingCutoffDay.toIntOrNull() ?: 25,
                            dueDay = dueDay.toIntOrNull() ?: 22,
                            monthlyBudgetCap = monthlyBudgetCap.toDoubleOrNull() ?: 0.0
                        )
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                shape = RoundedCornerShape(12.dp),
                enabled = label.isNotBlank() && lastFour.length == 4
            ) {
                Icon(Icons.Default.Check, null, tint = Surface950)
                Spacer(Modifier.width(8.dp))
                Text("Save Card", color = Surface950, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun FormSection(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = appTextSubColor(), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        content()
    }
}

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = appTextColor(),
    unfocusedTextColor = appTextColor(),
    focusedBorderColor = Emerald500,
    unfocusedBorderColor = appSurfaceColor(),
    cursorColor = Emerald500,
    focusedContainerColor = appCardColor(),
    unfocusedContainerColor = appCardColor()
)
