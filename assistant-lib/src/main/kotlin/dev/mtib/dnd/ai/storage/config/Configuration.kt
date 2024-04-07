package dev.mtib.dnd.ai.storage.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import java.io.Closeable
import java.nio.file.Path
import kotlin.io.path.notExists

class Configuration {
    companion object {
        fun loadConfig(configPath: Path): DndConfigRc {
            if (configPath.notExists()) {
                return DndConfigRc.default()
            }
            return configPath.toFile().inputStream().use { inputStream ->
                Yaml.default.decodeFromString(DndConfigRc.serializer(), inputStream.readBytes().decodeToString())
            }
        }

        fun writeConfig(configPath: Path, config: DndConfigRc) {
            configPath.toFile().outputStream().use { outputStream ->
                outputStream.write(Yaml.default.encodeToString(DndConfigRc.serializer(), config).toByteArray())
            }
        }

        inline fun <T> configured(configPath: Path, configured: (config: DndConfigRc) -> T): T = ConfigurationCloser(configPath).use {
            configured(it.config)
        }
    }

    class ConfigurationCloser(private val configPath: Path) : Closeable {
        var config: DndConfigRc
            get() = loadConfig(configPath)
            set(value) = writeConfig(configPath, value)
        override fun close() {
            writeConfig(configPath, config)
        }
    }

    private constructor() {

    }

    @Serializable
    data class DndConfigRc(
        val openAiToken: OpenAiToken,
        val openAiModel: OpenAiModel,
        val projectRoot: String,
    ) {
        companion object {
            fun default() = DndConfigRc(
                openAiToken = OpenAiToken(""),
                openAiModel = OpenAiModel(""),
                projectRoot = ""
            )
        }
        @JvmInline
        @Serializable
        value class OpenAiToken(
            val token: String
        )

        @JvmInline
        @Serializable
        value class OpenAiModel(
            val model: String
        )
    }
}