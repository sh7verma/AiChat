package com.shverma.app.core.domain.model


/**
 * Represents a single streamed chunk of an assistant response.
 *
 * - `delta` contains incremental text (may be empty)
 * - `isFinished` signals the end of the stream
 */
data class ChatChunk(
    val delta: String="",
    val isFinished: Boolean = false
)