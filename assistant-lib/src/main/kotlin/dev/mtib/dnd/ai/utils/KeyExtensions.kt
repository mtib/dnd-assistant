package dev.mtib.dnd.ai.utils

import dev.mtib.dnd.ai.storage.modules.ModuleStorage

fun String.asKey(): ModuleStorage.Key = object : ModuleStorage.Key {
    override fun toIdentifier(): String = this@asKey
}