package dev.mtib.dnd.ai.storage.markdown

import dev.mtib.dnd.ai.storage.modules.FileSystemModuleStorage
import dev.mtib.dnd.ai.storage.modules.Module
import dev.mtib.dnd.ai.storage.modules.ModuleStorage

data class ExtendedMarkdown(val content: String) {
    /** Matches:
     * ```
     * include: myFile.md // Some comments
     * include: myFile.md var1=name,val2=test // Some comments
     * ```
     */
    private val includeRegex = Regex("""^\s*!include:\s*(\S+)(\s+(\S*))?\s*(//.*)?$""")
    private val blankRegex = Regex("""^\s*!prompt:\s+(.+)$""")
    fun toMarkdown(context: MarkdownParsingContext): Markdown {
        return Markdown(toMarkdownSequence(context).joinToString("\n"))
    }

    fun toMarkdownSequence(context: MarkdownParsingContext): Sequence<String> {
        return content.lineSequence().flatMapIndexed { index, line -> context.handleLine(
            line = line,
            lineNumberHint = index + 1
        )}
    }

    private fun MarkdownParsingContext.handleLine(line: String, lineNumberHint: Int? = null): Sequence<String> {
        val includeMatch = includeRegex.matchEntire(line)
        if (includeMatch != null) {
            val referenceString = includeMatch.groupValues[1]
            val referencedModuleKey = currentModule.resolveOrNull(referenceString)
                ?: throw IllegalStateException("Could not resolve include: $referenceString")
            if (includePath.any { it.toIdentifier() == referencedModuleKey.toIdentifier() }) {
                throw IllegalStateException("Circular include detected: ${this.includePath.joinToString(" -> ")} -> $referencedModuleKey")
            }
            val includeContent = this.moduleStorage.retrieveOrNull(referencedModuleKey)
                ?: throw IllegalStateException("Could not find module for include: $referenceString (path: ${includePath.joinToString(" -> ") { it.toIdentifier() }} -/-> ${referencedModuleKey.toIdentifier()})")
            val addedEnvironment =
                includeMatch.groupValues.getOrNull(3)?.let {
                    it.split(',').map { it.split('=') }.filter { it.size == 2 } .associate { it[0] to it[1] }
                } ?: emptyMap()
            return includeContent.readExtended().toMarkdownSequence(this.copy(
                currentModule = includeContent,
                includePath = includePath + referencedModuleKey,
                environment = environment + addedEnvironment,
            ))
        }
        val blankMatch = blankRegex.matchEntire(line)
        if (blankMatch != null) {
            return blankTracker.addBlank(blankMatch.groupValues[1], currentModule.getKey(), lineNumberHint)
                .toInDocumentReference().lineSequence()
        }
        return sequenceOf(line)
    }

    data class MarkdownParsingContext private constructor(
        val moduleStorage: ModuleStorage<*>,
        val includePath: List<ModuleStorage.Key>,
        val currentModule: Module,
        val environment: Map<String, String> = emptyMap(),
        val blankTracker: BlankTracker = BlankTracker(),
    ) {
        companion object {
            fun create(moduleStorage: ModuleStorage<*>, currentModule: Module): MarkdownParsingContext {
                return MarkdownParsingContext(moduleStorage, listOf(currentModule.getKey()), currentModule)
            }
        }

        data class BlankTracker(
            private val blanks: MutableList<Blank> = mutableListOf(),
        ) {
            fun addBlank(prompt: String, origin: ModuleStorage.Key, lineNumberHint: Int?): Blank {
                return Blank(blanks.size, prompt, origin, lineNumberHint).also {
                    blanks.add(it)
                }
            }

            fun asBlanks(): List<Blank> {
                return blanks.toList()
            }

            data class Blank(
                /** 0-based index */
                val number: Int,
                val prompt: String,
                val origin: ModuleStorage.Key,
                val lineNumberHint: Int?
            ) {
                private val locationHint: String
                    get() {
                        return listOfNotNull(
                            when (origin) {
                                is FileSystemModuleStorage.ProjectPath -> "in <data-dir>${origin.toAbsoluteProjectPath().path}"
                                else -> "from key=${origin.toIdentifier()}"
                            },
                            when (lineNumberHint) {
                                null -> null
                                else -> "line $lineNumberHint"
                            }
                        ).joinToString(" ")
                    }

                fun toInDocumentReference(): String {
                    return """
                        >>>> Start blank ${number+1}
                        TODO: $prompt
                        <<<< End blank ${number+1}
                    """.trimIndent()
                }

                fun toEndOfDocumentReference(): String {
                    return """Blank ${number + 1}: $prompt"""
                }

                fun toPreResponseReference(): String {
                    return """Blank ${number + 1} (${locationHint}): $prompt"""
                }
            }
        }
    }
}
