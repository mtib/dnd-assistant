package dev.mtib.dnd.ai.storage.modules

import dev.mtib.dnd.ai.storage.config.Configuration
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class FileSystemModuleStorageTest {
    @Test
    fun testToAbsolutePath() {
        val config = Configuration.DndConfigRc.default().copy(projectRoot = "/")

        data class TestCase(
            val abs: String,
            val rel: String,
            val expected: String,
        )

        val pairs = listOf(
            TestCase("/tmp", "file.txt", "/tmp/file.txt"),
            TestCase("/tmp", "/file.txt", "/file.txt"),
            TestCase("/tmp", "dir/file.txt", "/tmp/dir/file.txt"),
            TestCase("/tmp", "/dir/file.txt", "/dir/file.txt"),

            TestCase("/tmp/dir1", "file.txt", "/tmp/dir1/file.txt"),
            TestCase("/tmp/dir1", "/file.txt", "/file.txt"),
            TestCase("/tmp/dir1", "dir/file.txt", "/tmp/dir1/dir/file.txt"),
            TestCase("/tmp/dir1", "/dir/file.txt", "/dir/file.txt"),

            TestCase("/tmp/dir1", "dir2/file.txt", "/tmp/dir1/dir2/file.txt"),
            TestCase("/tmp/dir1", "/dir2/file.txt", "/dir2/file.txt"),
            TestCase("/tmp/dir1", "dir2/dir/file.txt", "/tmp/dir1/dir2/dir/file.txt"),

            TestCase("/tmp", "../file.txt", "/file.txt"),
            TestCase("/tmp", "./file.txt", "/tmp/file.txt"),
        )

        pairs.forEach {
            val absolutePath = FileSystemModuleStorage.AbsoluteProjectPath(it.abs)
            val relativePath = FileSystemModuleStorage.RelativeProjectPath(it.rel, absolutePath)

            assertEquals(it.expected, relativePath.toAbsoluteProjectPath().path)

            assertEquals(it.abs, absolutePath.toNioPath(config).toString())
            assertEquals(it.expected, relativePath.toNioPath(config).toString())

            val absolutePath2 = FileSystemModuleStorage.AbsoluteProjectPath(it.abs + "/")
            val relativePath2 = FileSystemModuleStorage.RelativeProjectPath(it.rel, absolutePath2)

            assertEquals(it.expected, relativePath2.toNioPath(config).toString())
            assertEquals(it.abs, absolutePath2.toNioPath(config).toString())
        }
    }

    @Test
    fun testPathInExistingRoot() {
        listOf("/tmp", "/tmp/").forEach {
            val config = Configuration.DndConfigRc.default().copy(projectRoot = it)
            val absolutePath = FileSystemModuleStorage.AbsoluteProjectPath("/data")
            assertEquals("/tmp/data", absolutePath.toNioPath(config).toString())
        }
    }

    @Test
    fun testParentPath() {
        val config = Configuration.DndConfigRc.default().copy(projectRoot = "/tmp")
        listOf("/tmp/data", "/tmp/data/").forEach {
            val absolutePath = FileSystemModuleStorage.AbsoluteProjectPath(it)
            val parentPath = absolutePath.getParent()
            assertEquals("/tmp", parentPath.path)
        }
    }
}