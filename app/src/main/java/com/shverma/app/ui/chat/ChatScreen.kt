package com.shverma.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shverma.app.core.domain.model.ChatMessage
import com.shverma.app.core.domain.model.Role


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var input by remember { mutableStateOf(TextFieldValue("")) }

    var lastFedIndex by remember(state.streamingSessionId) {
        mutableIntStateOf(0)
    }

    // 🔥 One controller per streaming session
    val controller = remember(state.streamingSessionId) {
        StreamingTextController(scope)
    }

    // Collect typed text ONCE (Compose-safe)
    val typedText by controller.displayedText.collectAsState()

    // Feed only NEW deltas to controller
    LaunchedEffect(state.streamingText) {
        if (state.streamingText.length > lastFedIndex) {
            val delta = state.streamingText.substring(lastFedIndex)
            controller.append(delta)
            lastFedIndex = state.streamingText.length
        }
    }


    // Prevent double commit
    var committed by remember(state.streamingSessionId) { mutableStateOf(false) }

    // Finalize ONLY after typing fully catches up
    LaunchedEffect(state.isStreaming, typedText, state.streamingText) {
        if (
            !committed &&
            !state.isStreaming &&
            state.streamingText.isNotBlank() &&
            typedText.length == state.streamingText.length
        ) {
            committed = true
            viewModel.commitAssistantMessage()
            controller.reset()
        }
    }

    // Auto-scroll during typing & new messages
    LaunchedEffect(state.messages.size, typedText) {
        listState.animateScrollToItem(listState.layoutInfo.totalItemsCount)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AI Chat") }) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Display error message if present
            state.error?.let { errorMessage ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(12.dp)
            ) {

                items(state.messages, key = { it.id }) { msg ->
                    MessageItem(message = msg)
                    Spacer(Modifier.height(8.dp))
                }

                // 🔥 Streaming bubble
                if (state.isStreaming || state.streamingText.isNotBlank()) {
                    item(key = state.streamingSessionId) {
                        MessageItem(
                            message = ChatMessage(
                                id = state.streamingSessionId,
                                role = Role.ASSISTANT,
                                content = if (typedText.isBlank()) "…" else typedText,
                                timestamp = System.currentTimeMillis()
                            ),
                            isStreaming = true
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            ChatInputBar(
                text = input,
                isStreaming = state.isStreaming,
                onTextChange = { input = it },
                onSend = {
                    focusManager.clearFocus()
                    controller.reset()
                    viewModel.sendMessage(input.text)
                    input = TextFieldValue("")
                },
                onCancel = {
                    controller.reset()
                    viewModel.cancelStreaming()
                }
            )
        }
    }
}


@Composable
private fun ChatInputBar(
    text: TextFieldValue,
    isStreaming: Boolean,
    onTextChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message") },
            maxLines = 4,
            enabled = !isStreaming
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (isStreaming) {
            TextButton(onClick = onCancel) {
                Text("Stop")
            }
        } else {
            Button(
                onClick = onSend,
                enabled = text.text.isNotBlank()
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageItem(
    message: ChatMessage,
    isStreaming: Boolean = false
) {
    val isUser = message.role == Role.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = MaterialTheme.shapes.medium,
            color = if (isUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = if (isStreaming) 1.dp else 2.dp
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun TypewriterText(
    messageId: String,
    fullText: String,
    modifier: Modifier = Modifier,
    speedMs: Long = 18L
) {
    var displayedText by remember(messageId) { mutableStateOf("") }
    var lastRenderedLength by remember(messageId) { mutableStateOf(0) }

    LaunchedEffect(fullText) {
        if (fullText.length > lastRenderedLength) {
            val newPart = fullText.substring(lastRenderedLength)
            for (char in newPart) {
                displayedText += char
                kotlinx.coroutines.delay(speedMs)
            }
            lastRenderedLength = fullText.length
        }
    }

    Row(modifier = modifier) {
        Text(displayedText)
        TypingCursor()
    }
}


@Composable
fun TypingCursor() {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            visible = !visible
            kotlinx.coroutines.delay(500)
        }
    }

    if (visible) {
        Text("▍")
    }
}
