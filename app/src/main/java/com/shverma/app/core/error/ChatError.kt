package com.shverma.app.core.error

sealed class ChatError(message: String, cause: Throwable? = null) : Throwable(message, cause) {
    class Network(cause: Throwable? = null) : ChatError("Network error", cause)
    class Unauthorized : ChatError("Unauthorized (check API key)")
    class RateLimited : ChatError("Rate limited (too many requests)")
    class Server(message: String) : ChatError(message)
    class InvalidResponse(message: String) : ChatError(message)
    class Unknown(cause: Throwable? = null) : ChatError("Unknown error", cause)
}