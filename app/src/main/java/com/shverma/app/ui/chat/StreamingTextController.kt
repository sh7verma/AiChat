package com.shverma.app.ui.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class StreamingTextController(
    private val scope: CoroutineScope,
    private val baseDelayMs: Long = 18L
) {

    private val buffer = StringBuilder()
    private val _displayedText = MutableStateFlow("")
    val displayedText: StateFlow<String> = _displayedText

    private val isRunning = AtomicBoolean(false)
    private var typingJob: Job? = null

    /** Called by ViewModel / UI whenever new text arrives */
    fun append(textDelta: String) {
        synchronized(buffer) {
            buffer.append(textDelta)
        }
        startIfNeeded()
    }

    /** Start typing loop once, never restart */
    private fun startIfNeeded() {
        if (isRunning.compareAndSet(false, true)) {
            typingJob = scope.launch {
                while (isActive) {
                    val nextChar: Char? = synchronized(buffer) {
                        if (buffer.isNotEmpty()) {
                            val c = buffer[0]
                            buffer.deleteCharAt(0)
                            c
                        } else {
                            null
                        }
                    }

                    if (nextChar != null) {
                        _displayedText.value += nextChar
                        delay(adaptiveDelay(nextChar))
                    } else {
                        delay(4)
                    }
                }
            }
        }
    }


    /** Adaptive typing speed */
    private fun adaptiveDelay(char: Char): Long {
        return when {
            char == '.' || char == '\n' -> baseDelayMs + 40
            buffer.length > 200 -> 5
            buffer.length > 100 -> 10
            else -> baseDelayMs
        }
    }

    /** Stop typing but keep text */
    fun stop() {
        typingJob?.cancel()
        isRunning.set(false)
    }

    /** Clear everything for next message */
    fun reset() {
        stop()
        synchronized(buffer) { buffer.clear() }
        _displayedText.value = ""
    }
}
