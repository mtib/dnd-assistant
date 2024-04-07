package dev.mtib.dnd.ai.commands

import dev.mtib.dnd.ai.openai.OpenAiManager
import dev.mtib.dnd.ai.storage.config.Configuration
import kotlinx.coroutines.runBlocking
import picocli.CommandLine
import picocli.CommandLine.*
import kotlin.io.path.Path

fun main(args: Array<String>) {
    CommandLine(DndAssistant()).execute(*args)
}

@Command(
    name = "dnd-assistant",
    mixinStandardHelpOptions = true,
    version = ["0.1.0"],
    description = ["A CLI assistant for Dungeons and Dragons"],
    subcommands = [DndAssistant.PromptCompletion::class, DndAssistant.CreateFile::class],

)
class DndAssistant : Runnable {
    @Command(name = "complete", mixinStandardHelpOptions = true)
    class PromptCompletion : Runnable {
        @Mixin
        private lateinit var config: RequiresConfigMixin

        @Parameters(index = "0", description = ["The prompt to complete"], arity="1", paramLabel = "PROMPT", defaultValue = "What is the meaning of life?")
        private lateinit var prompt: String

        override fun run() = runBlocking {
            Configuration.configured(config.asPath()) { config ->
                OpenAiManager.fromConfiguration(config).use {openAi ->
                    println(openAi.simpleResponse(prompt))
                }
            }
        }
    }

    @Command(name = "create", mixinStandardHelpOptions = true)
    class CreateFile : Runnable {
        @Mixin
        private lateinit var config: RequiresConfigMixin

        @Parameters(index = "0", description = ["The file to create"], arity="1", paramLabel = "FILE", defaultValue = "assisted-file.md")
        private var file: String? = null

        override fun run() = runBlocking {
            Configuration.configured(config.asPath()) { config ->
                println("Config: $config")
            }
        }
    }

    class RequiresConfigMixin {
        @Option(names = ["-c", "--config"], description = ["Path to the configuration file"], defaultValue = "dndrc.yaml")
        private lateinit var configPath: String

        fun asPath() = Path(configPath)
    }

    @Spec
    private lateinit var spec: Model.CommandSpec

    override fun run() {
        throw ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}