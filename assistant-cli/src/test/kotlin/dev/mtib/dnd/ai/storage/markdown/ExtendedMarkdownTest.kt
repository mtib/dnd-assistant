package dev.mtib.dnd.ai.storage.markdown

import dev.mtib.dnd.ai.storage.modules.MemoryModuleStorage
import dev.mtib.dnd.ai.utils.asKey
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class ExtendedMarkdownTest {

    private fun MemoryModuleStorage.storeTestData(
        key: String,
        content: String,
    ) {
        store(MemoryModuleStorage.MemoryModule(key.asKey(), ExtendedMarkdown(content), this))
    }
    private fun getTestStorage() = MemoryModuleStorage().apply {
        listOf(
            "test", "foo", "bar", "baz", "foobar"
        ).forEach {
            storeTestData(it, it)
        }
    }

    @Test
    fun testBasicExtendedParsing() {
        val storage = getTestStorage()

        storage.retrieve("test".asKey()).let {
            assertEquals("test", it.readExtended().content)
            assertEquals("test", it.read().content)
        }
    }

    @Test
    fun testBasicInclusion() {
        val storage = getTestStorage()
        storage.storeTestData("test2", "test2\n!include: foo")

        assertEquals("test2\nfoo", storage.retrieve("test2".asKey()).read().content)

        storage.storeTestData("test3", """
            test3
            !include: bar
            !include: baz
        """.trimIndent())

        assertEquals("test3\nbar\nbaz", storage.retrieve("test3".asKey()).read().content)
    }

    @Test
    fun testLongerInclusion() {
        val storage = getTestStorage()
        storage.storeTestData("toplevel", """
            toplevel
            !include: midlevel
        """.trimIndent())
        storage.storeTestData("midlevel", """
            midlevel
            !include: bottomlevel
        """.trimIndent())
        storage.storeTestData("bottomlevel", """
            bottomlevel
        """.trimIndent())
        assertEquals("toplevel\nmidlevel\nbottomlevel", storage.retrieve("toplevel".asKey()).read().content)
    }

    @Test
    fun testDetectCycles() {
        val storage = getTestStorage()
        storage.storeTestData("test2", "test2\n!include: test3")
        storage.storeTestData("test3", "test3\n!include: test2")

        assertThrows(IllegalStateException::class.java) {
            storage.retrieve("test2".asKey()).read()
        }
    }

    @Test
    fun testDetectSelfReference() {
        val storage = getTestStorage()
        storage.storeTestData("test2", "test2\n!include: test2")

        assertThrows(IllegalStateException::class.java) {
            storage.retrieve("test2".asKey()).read()
        }
    }

    @Test
    fun testEnvironment() {
        val storage = getTestStorage()
        storage.storeTestData("test2", "test2\n!include: foo var1=bar")
        storage.storeTestData("foo", "foo\n!include: bar var2=baz")
        storage.storeTestData("bar", "bar")

        assertEquals("test2\nfoo\nbar", storage.retrieve("test2".asKey()).read().content)
    }
}
