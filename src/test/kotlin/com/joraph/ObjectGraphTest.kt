package com.joraph

import com.joraph.schema.Author
import com.joraph.schema.Book
import com.joraph.schema.Rating
import com.joraph.schema.Schema
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ObjectGraphTest : AbstractJoraphTest() {

    private var _subject: ObjectGraph? = null
    private var _schema: Schema? = null

    private val subject: ObjectGraph
        get() = _subject!!

    private val schema: Schema
        get() = _schema!!

    @BeforeEach
    fun setUp() {
        _schema = Schema()
        setupSchema(schema)
        _subject = ObjectGraph(schema)
    }

    @Test
    fun `Empty and size work as expected`() {
        assertTrue(subject.isEmpty())
        assertFalse(subject.isNotEmpty())
        assertEquals(0, subject.size)

        subject[String::class.java, 0] = "1234 string"
        subject[Int::class.java, 1] = 1234
        assertEquals(2, subject.size)
        assertFalse(subject.isEmpty())
        assertTrue(subject.isNotEmpty())

        assertTrue(subject.remove(String::class.java, 0))
        assertEquals(1, subject.size)
        assertFalse(subject.isEmpty())
        assertTrue(subject.isNotEmpty())

        assertTrue(subject.remove(Int::class.java, 1))
        assertEquals(0, subject.size)
        assertTrue(subject.isEmpty())
        assertFalse(subject.isNotEmpty())

    }

    @Test
    fun `Can copy`() {
        subject[String::class.java, 0] = "1234 string"
        subject[Int::class.java, 1] = 1234
        assertEquals(2, subject.size)
        assertFalse(subject.isEmpty())
        assertTrue(subject.isNotEmpty())

        val clone = ObjectGraph(schema)
        assertEquals(0, clone.size)
        assertTrue(clone.isEmpty())
        assertFalse(clone.isNotEmpty())

        clone.copyGraphFrom(subject)
        assertEquals(2, clone.size)
        assertFalse(clone.isEmpty())
        assertTrue(clone.isNotEmpty())
        assertEquals(subject, clone)
    }

    @Test
    fun `Can get ids`() {
        subject[String::class.java, 0] = "0 string"
        subject[String::class.java, 1] = "1 string"
        subject[String::class.java, 2] = "2 string"
        subject[Int::class.java, 1] = 100
        subject[Int::class.java, 69] = 6900

        assertTrue(subject.getIds<Int>(String::class.java).containsAll(setOf(0, 1, 2)))
        assertEquals(3, subject.getIds<Int>(String::class.java).size)
        assertTrue(subject.getIds<Int>(Int::class.java).containsAll(setOf(1, 69)))
        assertEquals(2, subject.getIds<Int>(Int::class.java).size)
    }

    @Test
    fun `Can add a result that is part of schema`() {
        val author = Author()
        author.id = "penis1234i'm unique"
        author.name = "Penis Man"

        assertNull(subject[Author::class.java, author.id])
        subject.addResult(author)
        assertEquals(author, subject[Author::class.java, author.id])

    }

    @Test
    fun `Get required throws`() {
        val author = Author()
        author.id = "penis1234i'm unique"
        author.name = "Penis Man"

        assertNull(subject[Author::class.java, author.id])
        assertThrows(EntityNotFoundException::class.java) {
            subject.getRequired(Author::class.java, author.id)
        }

        subject.addResult(author)
        assertEquals(author, subject[Author::class.java, author.id])
        assertDoesNotThrow {
            subject.getRequired(Author::class.java, author.id)
        }

    }

    @Test
    fun `Getting a list never returns null`() {
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
