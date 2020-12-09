package com.joraph

import com.joraph.schema.Book
import com.joraph.schema.Rating
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ObjectGraphTest {

    private var _subject: ObjectGraph? = null

    private val subject: ObjectGraph
        get() = _subject!!

    @BeforeEach
    fun setUp() {
        _subject = ObjectGraph()
    }

    @Test
    fun testGetListNeverReturnsNull() {
        var strings: List<String?> = subject.getList(String::class.java)
        assertNotNull(strings)
        assertTrue(strings.isEmpty())

        subject.addResult(String::class.java, Integer.valueOf(1), "one")
        strings = subject.getList(String::class.java)
        assertNotNull(strings)
        assertFalse(strings.isEmpty())
        assertEquals(1, strings.size)
    }

}
