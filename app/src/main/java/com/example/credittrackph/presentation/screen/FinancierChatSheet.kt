package com.example.credittrackph.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.credittrackph.data.network.GroqMessage
import com.example.credittrackph.presentation.viewmodel.FinancierViewModel
import com.example.credittrackph.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancierChatSheet(
    onDismiss: () -> Unit,
    viewModel: FinancierViewModel = hiltViewModel()
) {
    val chatState by viewModel.chatState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }

    // Trigger initial summary when opened
    LaunchedEffect(Unit) {
        if (chatState.size <= 1) { // Only system prompt
            viewModel.generateSummary()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface900,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "🐶 FINANCIER",
                    color = Emerald400,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Chat Messages
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                reverseLayout = true
            ) {
                val visibleMessages = chatState.filter { it.role != "system" }
                items(visibleMessages.reversed()) { msg ->
                    ChatBubble(msg)
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (isLoading) {
                Text(
                    "*Financier is sniffing around...*",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
                )
            }

            // Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask your watchdog...", color = Color.White.copy(alpha = 0.4f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface800,
                        unfocusedContainerColor = Surface800,
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Emerald500, RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Surface950)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: GroqMessage) {
    val isUser = message.role == "user"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) Surface800 else Emerald500.copy(alpha = 0.2f)
    val textColor = if (isUser) Color.White else Color.White
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        if (!isUser) {
            Text("Financier", color = Emerald400, fontSize = 10.sp, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
        }
        Box(
            modifier = Modifier
                .background(bgColor, shape)
                .padding(16.dp)
        ) {
            Text(message.content, color = textColor, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}
