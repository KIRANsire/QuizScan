package com.quizscan.app

import com.quizscan.app.util.HtmlDecoder
import org.junit.Assert.assertEquals
import org.junit.Test

class HtmlDecoderTest {

    @Test
    fun decode_nullOrEmpty_returnsEmpty() {
        assertEquals("", HtmlDecoder.decode(null))
        assertEquals("", HtmlDecoder.decode(""))
    }

    @Test
    fun decode_plainText_returnsUnchanged() {
        val input = "What is the capital of France?"
        assertEquals(input, HtmlDecoder.decode(input))
    }
}
