package com.shverma.app.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * A sealed class for handling text resources in a type-safe way
 */
sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    /**
     * Get the string value from the UiText
     */
    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }

    /**
     * Get the string value from the UiText in a Composable
     */
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(id = resId, *args)
        }
    }

    companion object {
        fun dynamicString(value: String): UiText = DynamicString(value)
        fun stringResource(@StringRes resId: Int, vararg args: Any): UiText = StringResource(resId, *args)
    }
}