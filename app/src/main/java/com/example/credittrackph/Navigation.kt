package com.example.credittrackph

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
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
        containerColor = appBackgroundColor(),
        bottomBar = {
            if (!hasOverlay && !showFinancier) {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onFabClick = { showFabMenu = !showFabMenu },
                    isFabExpanded = showFabMenu
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // ── Tab Content ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (!hasOverlay && !showFinancier) padding else PaddingValues(0.dp))
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
                            onAiClick = { showFinancier = true },
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme
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

            // ── Financier AI Chat Screen (Full-Screen Animated Overlay) ──
            AnimatedVisibility(
                visible = showFinancier,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut()
            ) {
                FinancierChatSheet(
                    onDismiss = { showFinancier = false }
                )
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
}

// ═══════════════════════════════════════════════════════════════
// Floating Bottom Navigation Bar with Center Rotating FAB
// ═══════════════════════════════════════════════════════════════

@Composable
fun BottomNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onFabClick: () -> Unit,
    isFabExpanded: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    var touchX by remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .height(72.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // ── Floating Frosted Glass Background with touch/drag tracking ──
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            touchX = offset.x
                        },
                        onDragEnd = {
                            val finalX = touchX
                            touchX = null
                            if (finalX != null) {
                                val slotWidth = size.width / 5f
                                val targetSlot = (finalX / slotWidth).toInt().coerceIn(0, 4)
                                when (targetSlot) {
                                    0 -> onTabSelected(BottomTab.HOME)
                                    1 -> onTabSelected(BottomTab.TRANSACTIONS)
                                    2 -> onFabClick()
                                    3 -> onTabSelected(BottomTab.STATISTICS)
                                    4 -> onTabSelected(BottomTab.WALLET)
                                }
                            }
                        },
                        onDragCancel = {
                            touchX = null
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            touchX = change.position.x
                        }
                    )
                },
            color = if (isDark) Surface900.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.96f),
            shape = RoundedCornerShape(36.dp),
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, if (isDark) Surface700.copy(alpha = 0.4f) else Color(0xFFE2E8F0))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val itemWidth = maxWidth / 5

                // Map the 4 tabs to their grid slot (0, 1, 3, 4). Slot 2 is the FAB.
                val selectedSlot = when (selectedTab) {
                    BottomTab.HOME -> 0
                    BottomTab.TRANSACTIONS -> 1
                    BottomTab.STATISTICS -> 3
                    BottomTab.WALLET -> 4
                }

                val targetOffset = if (touchX != null) {
                    with(LocalDensity.current) {
                        (touchX!! - (itemWidth.toPx() / 2f)).toDp().coerceIn(0.dp, maxWidth - itemWidth)
                    }
                } else {
                    itemWidth * selectedSlot
                }

                // Smooth glide without rushing or wild bouncing
                val indicatorOffset by animateDpAsState(
                    targetValue = targetOffset,
                    animationSpec = tween(
                        durationMillis = if (touchX != null) 0 else 350,
                        easing = FastOutSlowInEasing
                    ),
                    label = "indicator"
                )

                // Glowing circular indicator behind active icon
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
                            .background(
                                if (isDark) Emerald500.copy(alpha = 0.22f) else Color(0xFF0284C7).copy(alpha = 0.16f),
                                CircleShape
                            )
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

                    // Center FAB Spacer (Slot 2)
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

        // ── Smooth Rotating Center FAB (+ to ×) ──
        val fabRotation by animateFloatAsState(
            targetValue = if (isFabExpanded) 45f else 0f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "fabRotation"
        )

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
                contentDescription = if (isFabExpanded) "Close Menu" else "Add Action",
                tint = Surface950,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer(rotationZ = fabRotation)
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
            tint = if (isSelected) appPrimaryColor() else appTextSubColor().copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// FAB Menu Overlay with Staggered Entrance Animations
// ═══════════════════════════════════════════════════════════════

@Composable
fun FabMenuOverlay(
    onDismiss: () -> Unit,
    onAddCard: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddProfile: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 112.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Item 1: Add Profile
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(250, delayMillis = 100)) +
                        slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(250, delayMillis = 100))
            ) {
                FabMenuItem(
                    icon = Icons.Default.PersonAdd,
                    label = "Add Profile",
                    onClick = onAddProfile
                )
            }

            // Item 2: Add Card
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(250, delayMillis = 50)) +
                        slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(250, delayMillis = 50))
            ) {
                FabMenuItem(
                    icon = Icons.Default.CreditCard,
                    label = "Add Card",
                    onClick = onAddCard
                )
            }

            // Item 3: Add Transaction
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(250)) +
                        slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(250))
            ) {
                FabMenuItem(
                    icon = Icons.Default.Receipt,
                    label = "Add Transaction",
                    onClick = onAddTransaction
                )
            }
        }
    }
}

@Composable
private fun FabMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = appCardColor(),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, appSurfaceColor().copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Emerald400, modifier = Modifier.size(20.dp))
            Text(label, color = appTextColor(), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
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
        title = { Text("Welcome to CreditTrack!", color = appTextColor(), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Who is the main user of this app?", color = appTextSubColor())
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appTextColor(),
                        unfocusedTextColor = appTextColor(),
                        cursorColor = Emerald400,
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = appTextSubColor().copy(alpha = 0.4f),
                        focusedLabelColor = Emerald400
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onNameSubmitted(name.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text("Let's Go!", color = Surface950, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = appCardColor()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileDialog(onDismiss: () -> Unit, onNameSubmitted: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Profile", color = appTextColor(), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter the name of the family member or user.", color = appTextSubColor())
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appTextColor(),
                        unfocusedTextColor = appTextColor(),
                        cursorColor = Emerald400,
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = appTextSubColor().copy(alpha = 0.4f),
                        focusedLabelColor = Emerald400
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) { onNameSubmitted(name.trim()); onDismiss() } },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text("Save", color = Surface950, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = appTextSubColor())
            }
        },
        containerColor = appCardColor()
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
        containerColor = appCardColor()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Select Card",
                color = appTextColor(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Choose which card this transaction belongs to",
                color = appTextSubColor(),
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
                        color = appTextSubColor(),
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
                        colors = CardDefaults.cardColors(containerColor = appSurfaceColor()),
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
                                    color = appTextColor(),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    "${card.bank.displayName} • •••• ${card.lastFourDigits}",
                                    color = appTextSubColor(),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
