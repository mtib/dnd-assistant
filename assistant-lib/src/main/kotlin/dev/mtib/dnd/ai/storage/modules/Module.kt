package dev.mtib.dnd.ai.storage.modules

import dev.mtib.dnd.ai.storage.markdown.ExtendedMarkdown
import dev.mtib.dnd.ai.storage.markdown.Markdown
import dev.mtib.dnd.ai.storage.modules.ModuleStorage.Key

interface Module {

    fun read(): Markdown = readExtended().toMarkdown(ExtendedMarkdown.MarkdownParsingContext.create(
        currentModule = this,
        moduleStorage = getStorage(),
    ))

    fun readExtended(): ExtendedMarkdown

    fun getKey(): Key

    fun getStorage(): ModuleStorage<*>

    /** Used for include resolution */
    fun resolveOrNull(reference: String): Key?
}