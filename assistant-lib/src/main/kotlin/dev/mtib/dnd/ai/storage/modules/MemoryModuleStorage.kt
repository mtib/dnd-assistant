package dev.mtib.dnd.ai.storage.modules

import dev.mtib.dnd.ai.storage.markdown.ExtendedMarkdown
import dev.mtib.dnd.ai.storage.modules.MemoryModuleStorage.MemoryModule
import dev.mtib.dnd.ai.utils.asKey

class MemoryModuleStorage: ModuleStorage<MemoryModule> {
    private val storage = mutableMapOf<String, MemoryModule>()
    override fun store(key: ModuleStorage.Key, data: MemoryModule) {
        storage[key.toIdentifier()] = data
    }

    fun store(data: MemoryModule) {
        storage[data.index.toIdentifier()] = data
    }

    override fun retrieveOrNull(key: ModuleStorage.Key): MemoryModule? = storage[key.toIdentifier()]

    data class MemoryModule(val index: ModuleStorage.Key, val data: ExtendedMarkdown, val parentStorage: MemoryModuleStorage): Module {
        override fun readExtended(): ExtendedMarkdown = data

        override fun getKey(): ModuleStorage.Key = index
        override fun getStorage(): ModuleStorage<*> = parentStorage

        /** only supports absolute references */
        override fun resolveOrNull(reference: String): ModuleStorage.Key? {
            return reference.asKey()
        }
    }
}
