package com.example.credittrackph.presentation.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.credittrackph.data.network.GroqMessage
import com.example.credittrackph.presentation.viewmodel.FinancierViewModel
import com.example.credittrackph.theme.*
import com.example.credittrackph.util.AudioRecorderHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

@Composable
fun FinancierChatSheet(
    onDismiss: () -> Unit,
    viewModel: FinancierViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val chatState by viewModel.chatState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showKeyDialog by remember { mutableStateOf(false) }

    // ── Text-to-Speech Engine ──
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var autoSpeakEnabled by remember { mutableStateOf(false) }
    var currentlySpeakingText by remember { mutableStateOf<String?>(null) }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isTtsReady = true
            }
        }
        textToSpeech = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun speakText(text: String) {
        if (!isTtsReady || textToSpeech == null) return
        if (currentlySpeakingText == text) {
            textToSpeech?.stop()
            currentlySpeakingText = null
        } else {
            textToSpeech?.stop()
            val cleanSpeech = text.replace("*", "").replace("🐶", "").replace("₱", "Pesos ").trim()
            textToSpeech?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, "FINANCIER_TTS")
            currentlySpeakingText = text
        }
    }

    // Auto-speak new incoming assistant responses
    LaunchedEffect(chatState.size) {
        if (autoSpeakEnabled) {
            val lastMsg = chatState.lastOrNull()
            if (lastMsg != null && lastMsg.role == "assistant") {
                speakText(lastMsg.content)
            }
        }
    }

    // ── In-App Audio Recorder & Whisper AI Dictation ──
    val recorderHelper = remember { AudioRecorderHelper(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }
    val isTranscribing by viewModel.isTranscribing.collectAsState()
    val haptic = LocalHapticFeedback.current
    var pressStartTime by remember { mutableStateOf(0L) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Microphone permission is required for voice dictation", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isActive && isRecording) {
                delay(1000)
                recordingSeconds++
            }
        } else {
            recordingSeconds = 0
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    fun startAudioRecording() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val success = recorderHelper.startRecording()
        if (success) {
            isRecording = true
        } else {
            Toast.makeText(context, "Could not start microphone recorder", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopAndTranscribeAudio() {
        if (!isRecording) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        isRecording = false
        val audioFile = recorderHelper.stopRecording()
        if (audioFile != null && audioFile.exists() && audioFile.length() > 0) {
            viewModel.transcribeAudio(
                audioFile = audioFile,
                onResult = { transcribedText ->
                    inputText = if (inputText.isBlank()) transcribedText else "$inputText $transcribedText"
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    fun cancelAudioRecording() {
        recorderHelper.cancelRecording()
        isRecording = false
    }

    DisposableEffect(Unit) {
        onDispose {
            recorderHelper.cancelRecording()
        }
    }

    // Trigger initial summary when opened
    LaunchedEffect(Unit) {
        if (chatState.size <= 1) {
            viewModel.generateSummary()
        }
    }

    if (showKeyDialog) {
        var keyInput by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            keyInput = viewModel.getSavedApiKey()
        }
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            containerColor = appCardColor(),
            title = {
                Text("Groq API Key", color = appTextColor(), fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Financier uses free models hosted on Groq (openai/gpt-oss-20b & qwen/qwen3.8-27b). Enter your API key below if you wish to override the default key.",
                        color = appTextSubColor(),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        placeholder = { Text("gsk_...", color = appTextSubColor().copy(alpha = 0.5f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = appTextColor(),
                            unfocusedTextColor = appTextColor(),
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = appTextSubColor().copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveApiKey(keyInput)
                        showKeyDialog = false
                    }
                ) {
                    Text("Save Key", color = Emerald400, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeyDialog = false }) {
                    Text("Cancel", color = appTextSubColor())
                }
            }
        )
    }

    // Full Screen AI Assistant
    Scaffold(
        containerColor = appBackgroundColor(),
        topBar = {
            Surface(
                color = appCardColor(),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Emerald400
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "🐶 FINANCIER",
                            color = Emerald400,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "AI Financial Watchdog",
                            color = appTextSubColor(),
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Auto-TTS Voice Toggle
                        IconButton(
                            onClick = {
                                autoSpeakEnabled = !autoSpeakEnabled
                                if (!autoSpeakEnabled) {
                                    textToSpeech?.stop()
                                    currentlySpeakingText = null
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                if (autoSpeakEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Auto Speak",
                                tint = if (autoSpeakEnabled) Emerald400 else appTextSubColor().copy(alpha = 0.5f)
                            )
                        }

                        IconButton(
                            onClick = { showKeyDialog = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Key,
                                contentDescription = "Groq API Key Settings",
                                tint = Emerald400
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                // ── Creator Watermark Banner ──
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/aydreian"))
                            context.startActivity(browserIntent)
                        },
                    color = appSurfaceColor().copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🐶 Built with ❤️ by aydreian • github.com/aydreian",
                            color = appTextSubColor().copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // ── Chat Input Bar ──
                Surface(
                    color = appCardColor(),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Push-to-Talk / Hold-to-Speak Microphone Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .scale(if (isRecording) pulseScale else 1f)
                                .background(
                                    if (isRecording) RedAlert else Emerald500.copy(alpha = 0.15f),
                                    CircleShape
                                )
                                .pointerInput(isRecording) {
                                    detectTapGestures(
                                        onPress = {
                                            if (isRecording) {
                                                // Tapped to stop active recording
                                                tryAwaitRelease()
                                                stopAndTranscribeAudio()
                                            } else {
                                                pressStartTime = System.currentTimeMillis()
                                                startAudioRecording()
                                                val released = tryAwaitRelease()
                                                val pressDuration = System.currentTimeMillis() - pressStartTime
                                                if (released) {
                                                    if (pressDuration >= 400) {
                                                        // Was held down: stop and transcribe on release
                                                        stopAndTranscribeAudio()
                                                    } else {
                                                        // Was quick tap: keep recording in toggle mode!
                                                    }
                                                } else {
                                                    cancelAudioRecording()
                                                }
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Mic else Icons.Default.MicNone,
                                contentDescription = "Hold or tap to speak",
                                tint = if (isRecording) Color.White else Emerald400,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (isRecording) {
                            // Active Recording Visualizer Bar
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(RedAlert.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .scale(pulseScale)
                                            .background(RedAlert, CircleShape)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "%02d:%02d".format(recordingSeconds / 60, recordingSeconds % 60),
                                        color = RedAlert,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    "Release or tap to transcribe ✨",
                                    color = appTextColor(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                IconButton(
                                    onClick = { cancelAudioRecording() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Cancel recording",
                                        tint = appTextSubColor(),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else if (isTranscribing) {
                            // Whisper AI Processing Indicator Bar
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(Emerald500.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Emerald400,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "✨ Whisper AI transcribing...",
                                    color = Emerald400,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            // Standard Text Input Field
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text("Ask or hold 🎙️ to speak...", color = appTextSubColor().copy(alpha = 0.6f)) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = appSurfaceColor(),
                                    unfocusedContainerColor = appSurfaceColor(),
                                    focusedBorderColor = Emerald500,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = appTextColor(),
                                    unfocusedTextColor = appTextColor()
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = !isRecording && !isTranscribing,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isRecording || isTranscribing) appSurfaceColor() else Emerald500,
                                    RoundedCornerShape(22.dp)
                                )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (isRecording || isTranscribing) appTextSubColor().copy(alpha = 0.4f) else Surface950
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                reverseLayout = true
            ) {
                val visibleMessages = chatState.filter { it.role != "system" }
                items(visibleMessages.reversed()) { msg ->
                    ChatBubble(
                        message = msg,
                        isSpeaking = currentlySpeakingText == msg.content,
                        onSpeakClick = { speakText(msg.content) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            if (isLoading) {
                Text(
                    "*Financier is sniffing around...*",
                    color = Emerald400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: GroqMessage,
    isSpeaking: Boolean = false,
    onSpeakClick: () -> Unit = {}
) {
    val isUser = message.role == "user"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) appCardColor() else Emerald500.copy(alpha = 0.15f)
    val textColor = appTextColor()
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
            Row(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .padding(start = 6.dp, bottom = 2.dp, end = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Financier",
                    color = Emerald400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onSpeakClick,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                        contentDescription = "Read Aloud",
                        tint = if (isSpeaking) Emerald400 else appTextSubColor().copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .background(bgColor, shape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(message.content, color = textColor, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}
