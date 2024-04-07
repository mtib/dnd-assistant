package dev.mtib.dnd.ai.storage.markdown

@JvmInline
value class Markdown(val content: String) {
    fun asExtendedMarkdown(): ExtendedMarkdown = ExtendedMarkdown(content)
}