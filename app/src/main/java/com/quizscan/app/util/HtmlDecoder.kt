package com.quizscan.app.util

import androidx.core.text.HtmlCompat

object HtmlDecoder {
    fun decode(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        if (!input.contains('&') && !input.contains('<')) {
            return input
        }
        return try {
            HtmlCompat.fromHtml(input, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        } catch (_: Exception) {
            input
        }
    }
}
