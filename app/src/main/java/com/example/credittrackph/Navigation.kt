package com.example.credittrackph

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.credittrackph.data.db.entity.CardEntity
import com.example.credittrackph.domain.calculator.InstallmentCalculator
import com.example.credittrackph.presentation.screen.*
import com.example.credittrackph.presentation.viewmodel.CardViewModel
import com.example.credittrackph.presentation.viewmodel.ProfileViewModel
import com.example.credittrackph.theme.*

// ── Bottom Tab Definitions ──

enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    TRANSACTIONS("Transactions", Icons.Default.Receipt),
    STATISTICS("Statistics", Icons.Default.PieChart),
    WALLET("Wallet", Icons.Default.AccountBalanceWallet)
}

// ── Overlay Screen Definitions ──

sealed class Screen {
    object AddCard : Screen()
    data class CardDetail(val card: CardEntity) : Screen()
    data class AddExpense(val card: CardEntity) : Screen()
}

// ── Main Navigation Host ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    val overlayStack = remember { mutableStateListOf<Screen>() }
    val installmentCalculator = remember { InstallmentCalculator() }
    var showFabMenu by remember { mutableStateOf(false) }
    var showCardPicker by remember { mutableStateOf(false) }
    var showFinancier by remember { mutableStateOf(false) }
    var showAddProfile by remember { mutableStateOf(false) }

    val isMainUserSetupRequired by profileViewModel.isMainUserSetupRequired.collectAsState()

    fun pushScreen(screen: Screen) { overlayStack.add(screen) }
    fun popScreen() { if (overlayStack.isNotEmpty()) overlayStack.removeLastOrNull() }

    val hasOverlay = overlayStack.isNotEmpty()

    if (isMainUserSetupRequired) {
        MainUserSetupDialog(
            onNameSubmitted = { name ->
                profileViewModel.setMainUser(name)
            }
        )
    }

    if (showAddProfile) {
        AddProfileDialog(
            onDismiss = { showAddProfile = false },
            onNameSubmitted = { name ->
                profileViewModel.addFamilyMember(name)
            }
        )
    }

    Scaffold(
        containerColor = Surface950,
        bottomBar = {
            if (!hasOverlay) {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onFabClick = { showFabMenu = true }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // ── Tab Content ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (!hasOverlay) padding else PaddingValues(0.dp))
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "Tab Transition"
                ) { targetTab ->
                    when (targetTab) {
                        BottomTab.HOME -> HomeScreen(
                            onViewAllTransactions = { selectedTab = BottomTab.TRANSACTIONS },
                            onViewWallet = { selectedTab = BottomTab.WALLET },
                            onCardClick = { card -> pushScreen(Screen.CardDetail(card)) },
                            onAiClick = { showFinancier = true }
                        )
                        BottomTab.TRANSACTIONS -> TransactionsScreen()
                        BottomTab.STATISTICS -> AnalyticsScreen()
                        BottomTab.WALLET -> WalletScreen(
                            onCardClick = { card -> pushScreen(Screen.CardDetail(card)) },
                            onAddCard = { pushScreen(Screen.AddCard) }
                        )
                    }
                }
            }

            // ── Overlay Screens (full-screen, on top of tabs) ──
            AnimatedContent(
                targetState = overlayStack.lastOrNull(),
                transitionSpec = {
                    if (targetState != null) {
                        slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn() togetherWith fadeOut()
                    } else {
                        fadeIn() togetherWith slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut()
                    }
                },
                label = "Overlay Transition"
            ) { screen ->
                if (screen != null) {
                    when (screen) {
                        is Screen.AddCard -> AddEditCardScreen(
                            onBack = { popScreen() },
                            onSaved = { popScreen() }
                        )
                        is Screen.CardDetail -> CardDetailScreen(
                            card = screen.card,
                            onBack = { popScreen() },
                            onAddExpense = { pushScreen(Screen.AddExpense(screen.card)) }
                        )
                        is Screen.AddExpense -> AddEditExpenseScreen(
                            card = screen.card,
                            onBack = { popScreen() },
                            onSaved = { overlayStack.clear() },
                            installmentCalculator = installmentCalculator
                        )
                    }
                }
            }
        }
    }

    // ── FAB Menu Overlay ──
    if (showFabMenu) {
        FabMenuOverlay(
            onDismiss = { showFabMenu = false },
            onAddCard = {
                showFabMenu = false
                pushScreen(Screen.AddCard)
            },
            onAddTransaction = {
                showFabMenu = false
                showCardPicker = true
            },
            onAddProfile = {
                showFabMenu = false
                showAddProfile = true
            }
        )
    }

    // ── Card Picker Bottom Sheet ──
    if (showCardPicker) {
        CardPickerSheet(
            onDismiss = { showCardPicker = false },
            onCardSelected = { card ->
                showCardPicker = false
                pushScreen(Screen.AddExpense(card))
            }
        )
    }

    // ── Financier AI Chat Sheet ──
    if (showFinancier) {
        FinancierChatSheet(
            onDismiss = { showFinancier = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Floating Bottom Navigation Bar with Center FAB
// ═══════════════════════════════════════════════════════════════

@Composable
fun BottomNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onFabClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .height(72.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // ── Floating Frosted Glass Background ──
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Surface900.copy(alpha = 0.85f),
            shape = RoundedCornerShape(36.dp),
            shadowElevation = 8.dp
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val itemWidth = maxWidth / 5
                
                // ── Animated Selection Circle ──
                // Map the 4 tabs to their grid slot (0, 1, 3, 4). Slot 2 is the FAB.
                val selectedSlot = when (selectedTab) {
                    BottomTab.HOME -> 0
                    BottomTab.TRANSACTIONS -> 1
                    BottomTab.STATISTICS -> 3
                    BottomTab.WALLET -> 4
                }
                
                val indicatorOffset by androidx.compose.animation.core.animateDpAsState(
                    targetValue = itemWidth * selectedSlot,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    ), label = "indicator"
                )

                // The glowing circle behind the active icon
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(itemWidth)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Emerald500.copy(alpha = 0.2f), CircleShape)
                    )
                }

                // ── Tab Items ──
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(itemWidth), contentAlignment = Alignment.Center) {
                        BottomNavItem(
                            tab = BottomTab.HOME,
                            isSelected = selectedTab == BottomTab.HOME,
                            onClick = { onTabSelected(BottomTab.HOME) }
                        )
                    }
                    Box(modifier = Modifier.width(itemWidth), contentAlignment = Alignment.Center) {
                        BottomNavItem(
                            tab = BottomTab.TRANSACTIONS,
                            isSelected = selectedTab == BottomTab.TRANSACTIONS,
                            onClick = { onTabSelected(BottomTab.TRANSACTIONS) }
                        )
                    }
                    
                    // Center FAB Spacer
                    Spacer(Modifier.width(itemWidth))
                    
                    Box(modifier = Modifier.width(itemWidth), contentAlignment = Alignment.Center) {
                        BottomNavItem(
                            tab = BottomTab.STATISTICS,
                            isSelected = selectedTab == BottomTab.STATISTICS,
                            onClick = { onTabSelected(BottomTab.STATISTICS) }
                        )
                    }
                    Box(modifier = Modifier.width(itemWidth), contentAlignment = Alignment.Center) {
                        BottomNavItem(
                            tab = BottomTab.WALLET,
                            isSelected = selectedTab == BottomTab.WALLET,
                            onClick = { onTabSelected(BottomTab.WALLET) }
                        )
                    }
                }
            }
        }

        // ── Center FAB (Overlapping the nav bar) ──
        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-16).dp)
                .size(64.dp)
                .shadow(12.dp, CircleShape),
            containerColor = Emerald500,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = Surface950,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(tab: BottomTab, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            tab.icon,
            contentDescription = tab.label,
            tint = if (isSelected) Emerald400 else BottomNavUnselected,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// FAB Menu Overlay (Add Card / Add Transaction / Add Profile)
// ═══════════════════════════════════════════════════════════════

@Composable
fun FabMenuOverlay(
    onDismiss: () -> Unit,
    onAddCard: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FabMenuItem(
                icon = Icons.Default.PersonAdd,
                label = "Add Profile",
                onClick = onAddProfile
            )
            FabMenuItem(
                icon = Icons.Default.CreditCard,
                label = "Add Card",
                onClick = onAddCard
            )
            FabMenuItem(
                icon = Icons.Default.Receipt,
                label = "Add Transaction",
                onClick = onAddTransaction
            )
        }
    }
}

@Composable
private fun FabMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() },
        color = Surface800,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Emerald400, modifier = Modifier.size(20.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Profile Dialogs
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUserSetupDialog(onNameSubmitted: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { /* Block dismiss */ },
        title = { Text("Welcome to CreditTrack!", color = Color.White) },
        text = {
            Column {
                Text("Who is the main user of this app?", color = Color.White.copy(0.7f))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Emerald400,
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = Color.White.copy(0.3f),
                        focusedLabelColor = Emerald400
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onNameSubmitted(name.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text("Let's Go!", color = Surface950)
            }
        },
        containerColor = Surface800
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileDialog(onDismiss: () -> Unit, onNameSubmitted: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Profile", color = Color.White) },
        text = {
            Column {
                Text("Enter the name of the family member.", color = Color.White.copy(0.7f))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Emerald400,
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = Color.White.copy(0.3f),
                        focusedLabelColor = Emerald400
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) { onNameSubmitted(name.trim()); onDismiss() } },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text("Save", color = Surface950)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(0.7f))
            }
        },
        containerColor = Surface800
    )
}

// ═══════════════════════════════════════════════════════════════
// Card Picker Bottom Sheet
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardPickerSheet(
    onDismiss: () -> Unit,
    onCardSelected: (CardEntity) -> Unit,
    cardViewModel: CardViewModel = hiltViewModel()
) {
    val cards by cardViewModel.allCards.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface900
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Select Card",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Choose which card this transaction belongs to",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))

            if (cards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No cards added yet.\nAdd a card first.",
                        color = Color.White.copy(0.5f),
                        fontSize = 14.sp
                    )
                }
            } else {
                cards.forEach { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onCardSelected(card) },
                        colors = CardDefaults.cardColors(containerColor = Surface800),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(card.cardColorArgb), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    card.bank.shortCode.take(3),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    card.label,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "${card.bank.displayName} • •••• ${card.lastFourDigits}",
                                    color = Color.White.copy(0.5f),
                                    fontSize = 12.sp
                                )
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Emerald400)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
