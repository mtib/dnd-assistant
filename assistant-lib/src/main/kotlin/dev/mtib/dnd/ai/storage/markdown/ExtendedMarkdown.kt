package dev.mtib.dnd.ai.storage.markdown

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
    fun toMarkdown(context: MarkdownParsingContext): Markdown {
        return Markdown(toMarkdownSequence(context).joinToString("\n"))
    }

    fun toMarkdownSequence(context: MarkdownParsingContext): Sequence<String> {
        return content.lineSequence().flatMap { context.handleLine(it) }
    }

    private fun MarkdownParsingContext.handleLine(line: String): Sequence<String> {
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
        return sequenceOf(line)
    }

    data class MarkdownParsingContext private constructor(
        val moduleStorage: ModuleStorage<*>,
        val includePath: List<ModuleStorage.Key>,
        val currentModule: Module,
        val environment: Map<String, String> = emptyMap(),
    ) {
        companion object {
            fun create(moduleStorage: ModuleStorage<*>, currentModule: Module): MarkdownParsingContext {
                return MarkdownParsingContext(moduleStorage, listOf(currentModule.getKey()), currentModule)
            }
        }
    }
}
