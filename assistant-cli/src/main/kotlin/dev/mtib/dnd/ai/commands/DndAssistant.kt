package dev.mtib.dnd.ai.commands

import dev.mtib.dnd.ai.openai.OpenAiManager
import dev.mtib.dnd.ai.storage.config.Configuration
import dev.mtib.dnd.ai.storage.markdown.ExtendedMarkdown
import dev.mtib.dnd.ai.storage.modules.FileSystemModuleStorage
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

        @Parameters(index = "0", description = ["The file to create"], arity="1", paramLabel = "FILE")
        private lateinit var file: String

        override fun run(): Unit = runBlocking {
            Configuration.configured(config.asPath()) { config ->
                val storage = FileSystemModuleStorage(config)
                val module = storage.retrieve(FileSystemModuleStorage.ProjectPath.of(file))

                val parsingContext = ExtendedMarkdown.MarkdownParsingContext.create(
                    storage,
                    module
                )
                val markdown = module.readExtended().toMarkdown(parsingContext)
                val blanks = parsingContext.blankTracker.asBlanks()

                val response = OpenAiManager.fromConfiguration(config).use { openAi ->
                    openAi.promptWithData(
                        data = includePrompts(markdown.content),
                        prompt = blanks.joinToString("\n") { it.toEndOfDocumentReference() },
                    )
                }

                println(blanks.joinToString("\n") { it.toPreResponseReference() })
                println("\n----\n")
                println(response)
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

    companion object {
        fun includePrompts(data: String): String {
            return listOf(
                """
                You are a helpful creative writing assistant for a Dungeon Master.
                
                You will receive a Markdown except from the document you are working on, which includes blanks with prompts for missing sections.
                
                Your task is to fill in the blanks with creative and engaging content. The campaign is using standard Dungeons and Dragons 5e rules, and is set in the Critical Role Tal'Dorei Reborn campaign setting.
                
                Whenever possible, try to maintain the tone and style of the original document and introduce just enough new information to allow for extending later.
                
                Your response will be structured and short, following the structure of similar sections in the document.
            """.trimIndent(),
                data,
                """
                Please fill the blanks and consider the prompts. Try to follow the existing structure in the document as much as possible and introduce just enough new information to allow for extending later.
                
                Mention which segment of the document you are responding to by naming the blank number and prompt, and then provide a short, structured response.
            """.trimIndent()
            ).joinToString("\n\n")
        }
    }
}