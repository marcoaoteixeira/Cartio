package com.minicore.cartio.features.monetization.data

import android.content.SharedPreferences

class InMemorySharedPreferences : SharedPreferences {

    private val store = mutableMapOf<String, Any?>()
    private val listeners = mutableListOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun getAll(): Map<String, *> = store.toMap()
    override fun getString(key: String, defValue: String?) = store[key] as? String ?: defValue
    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        val raw = store[key] ?: return defValues
        return (raw as? Set<*>)?.filterIsInstance<String>()?.toSet() ?: defValues
    }
    override fun getInt(key: String, defValue: Int) = store[key] as? Int ?: defValue
    override fun getLong(key: String, defValue: Long) = store[key] as? Long ?: defValue
    override fun getFloat(key: String, defValue: Float) = store[key] as? Float ?: defValue
    override fun getBoolean(key: String, defValue: Boolean) = store[key] as? Boolean ?: defValue
    override fun contains(key: String) = key in store
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) { listeners.add(listener) }
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) { listeners.remove(listener) }

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clearAll = false

        override fun putString(key: String, value: String?) = apply { pending[key] = value }
        override fun putStringSet(key: String, values: Set<String>?) = apply { pending[key] = values }
        override fun putInt(key: String, value: Int) = apply { pending[key] = value }
        override fun putLong(key: String, value: Long) = apply { pending[key] = value }
        override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
        override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
        override fun remove(key: String) = apply { pending[key] = null }
        override fun clear() = apply { clearAll = true }

        override fun commit(): Boolean { flush(); return true }
        override fun apply() { flush() }

        private fun flush() {
            if (clearAll) store.clear()
            pending.forEach { (k, v) -> if (v == null) store.remove(k) else store[k] = v }
        }
    }
}
