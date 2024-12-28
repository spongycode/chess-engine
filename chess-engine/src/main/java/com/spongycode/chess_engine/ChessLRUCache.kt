package com.spongycode.chess_engine

class ChessLRUCache(
    private val maxSize: Int
) {
    private val cache: LinkedHashMap<String, List<String>> =
        object : LinkedHashMap<String, List<String>>(maxSize, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<String, List<String>>?): Boolean {
                return size > maxSize
            }
        }

    fun get(key: String, fetchFromFunction: (String) -> List<String>): List<String> {
        val res = cache.getOrPut(key = key,
            defaultValue = { fetchFromFunction(key).also { put(key, it) } }
        )
        return res
    }

    private fun put(key: String, value: List<String>) {
        cache[key] = value
    }

    fun clear() {
        cache.clear()
    }

    private fun printCache() {
        println("Cache content: $cache")
    }
}