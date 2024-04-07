package dev.mtib.dnd.ai.storage.modules

interface ModuleStorage<M: Module> {
    fun store(key: Key, data: M)
    fun retrieve(key: Key): M = retrieveOrNull(key) ?: throw IllegalArgumentException("No module found for key $key with identifier ${key.toIdentifier()}")
    fun retrieveOrNull(key: Key): M?

    interface Key {
        fun toIdentifier(): String
    }

}