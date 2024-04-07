package dev.mtib.dnd.ai.storage.modules

import dev.mtib.dnd.ai.storage.config.Configuration.DndConfigRc
import dev.mtib.dnd.ai.storage.markdown.ExtendedMarkdown
import dev.mtib.dnd.ai.storage.modules.FileSystemModuleStorage.FileSystemModule
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText

class FileSystemModuleStorage(private val config: DndConfigRc): ModuleStorage<FileSystemModule> {

    class FileSystemModule(private val path: ProjectPath, private val config: DndConfigRc): Module {
        override fun readExtended(): ExtendedMarkdown {
            return path.toExtendedMarkdown(config)
        }

        override fun getKey(): ModuleStorage.Key {
            return path
        }

        override fun getStorage(): ModuleStorage<*> {
            return FileSystemModuleStorage(config)
        }

        override fun resolveOrNull(reference: String): ModuleStorage.Key? {
            return ProjectPath.of(reference, this.path.getParent())
        }
    }

    abstract class ProjectPath() : ModuleStorage.Key {
        fun toNioPath(config: DndConfigRc): Path = Path.of(config.projectRoot).resolve(this.toAbsoluteProjectPath().path.substring(1))

        abstract fun toAbsoluteProjectPath(): AbsoluteProjectPath

        fun getParent(): AbsoluteProjectPath {
            return AbsoluteProjectPath(Path(this.toAbsoluteProjectPath().path).parent.normalize().toString())
        }

        override fun toIdentifier(): String {
            return this.toAbsoluteProjectPath().path
        }

        fun toExtendedMarkdown(config: DndConfigRc): ExtendedMarkdown {
            return ExtendedMarkdown(this.toNioPath(config).readText())
        }

        companion object {
            fun of(path: String, cwd: AbsoluteProjectPath): ProjectPath {
                return if (path.startsWith("/")) {
                    AbsoluteProjectPath(path)
                } else {
                    RelativeProjectPath(path, cwd)
                }
            }
        }
    }
    class AbsoluteProjectPath(val path: String): ProjectPath() {
        override fun toAbsoluteProjectPath(): AbsoluteProjectPath = this
    }

    class RelativeProjectPath(val path: String, val cwd: AbsoluteProjectPath): ProjectPath() {
        override fun toAbsoluteProjectPath(): AbsoluteProjectPath {
            return AbsoluteProjectPath(Path(cwd.path).resolve(path).normalize().toString())
        }
    }

    override fun store(key: ModuleStorage.Key, data: FileSystemModule) {

    }

    override fun retrieveOrNull(key: ModuleStorage.Key): FileSystemModule? {
        TODO("Not yet implemented")
    }
}