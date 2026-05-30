package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.OptimiserViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    viewModel: OptimiserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val isHighContrast by viewModel.isHighContrast.collectAsState()

    var textInputState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll chat list down on new message payload
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val activeBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.background
    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onBackground
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
    val activeBorderColor = if (isHighContrast) Color.White else Color.Transparent
    val cardBg = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(activeBgColor)
    ) {
        // --- 1. GMail Secure OAuth panel (Top Sticky) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, activeBorderColor, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (userProfile == null) {
                    // Signed out view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Red.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Google GMail API Sync",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeTextColor
                                )
                                Text(
                                    "Unlock multi-device snapshot storage",
                                    fontSize = 10.sp,
                                    color = activeSubTextColor
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.signInWithGmailOAuth() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHighContrast) Color.White else Color.Red,
                                contentColor = if (isHighContrast) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("gmail_sign_in_button")
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = if (isHighContrast) Color.Black else Color.White
                                )
                            } else {
                                Icon(Icons.Default.Login, contentDescription = "", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GMail OAuth", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    // Signed in authenticated user view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Face profile icon placeholder", tint = Color.LightGray)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    userProfile!!.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeTextColor
                                )
                                Text(
                                    userProfile!!.email,
                                    fontSize = 11.sp,
                                    color = activeSubTextColor
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.signOutGmail() },
                            modifier = Modifier.testTag("gmail_sign_out_button")
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Log out from Google",
                                tint = if (isHighContrast) Color.White else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "",
                            tint = if (isHighContrast) Color.White else Color(0xFF4CAF50),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Authenticated with Google ID Token: Multi-device cloud backup sync functional",
                            fontSize = 9.sp,
                            color = activeSubTextColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- 2. Scrollable chat messages pane ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                val isAI = message.sender == "assistant"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAI) Arrangement.Start else Arrangement.End
                ) {
                    if (isAI) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                )
                                .border(1.dp, activeBorderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "AI symbol",
                                tint = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Chat Bubble shape & color
                    val bubbleBg = when {
                        isAI && isHighContrast -> Color.Black
                        isAI -> MaterialTheme.colorScheme.surfaceVariant
                        isHighContrast -> Color.White
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val bubbleBorder = if (isHighContrast) Color.White else Color.Transparent
                    val bubbleText = when {
                        isAI && isHighContrast -> Color.White
                        isAI -> MaterialTheme.colorScheme.onSurfaceVariant
                        isHighContrast -> Color.Black
                        else -> MaterialTheme.colorScheme.onPrimary
                    }

                    Card(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .border(1.dp, bubbleBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = bubbleBg),
                        shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isAI) 2.dp else 14.dp,
                            bottomEnd = if (isAI) 14.dp else 2.dp
                        )
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(12.dp),
                            color = bubbleText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    if (!isAI) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = CircleShape
                                )
                                .border(1.dp, activeBorderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "U",
                                fontWeight = FontWeight.Bold,
                                color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Spinner loader when Gemini API is resolving reply
            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = activeTextColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Agent resolving diagnostic commands...",
                            style = MaterialTheme.typography.bodySmall,
                            color = activeSubTextColor
                        )
                    }
                }
            }
        }

        // --- 3. Quick Touch Prompt suggestions ---
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Clean up residual cache junk [CLEAN_JUNK]" to "Clean System Cache",
                "Reduce system drain [BATTERY_SAVER]" to "Regulate Battery Saving",
                "Force hibernation on idling leakers" to "Freeze idling apps",
                "Run E2E backup snapshot of local logs" to "Save encrypted snapshot"
            ).forEach { (triggerText, chipsTitle) ->
                Box(
                    modifier = Modifier
                        .padding(vertical = 3.dp)
                        .background(
                            color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { textInputState = triggerText }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        chipsTitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeTextColor
                    )
                }
            }
        }

        // --- 4. Inputs Text Field actions layout (Bottom Bar sticky) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInputState,
                onValueChange = { textInputState = it },
                placeholder = { Text("Ask Gemini Agent to optimize phone...", fontSize = 12.sp, color = activeSubTextColor) },
                modifier = Modifier
                    .weight(1.0f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = activeTextColor,
                    unfocusedTextColor = activeTextColor,
                    focusedBorderColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textInputState.trim().isNotEmpty()) {
                        viewModel.sendChatMessage(textInputState)
                        textInputState = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
