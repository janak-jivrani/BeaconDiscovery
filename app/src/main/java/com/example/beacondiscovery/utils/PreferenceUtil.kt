package com.example.beacondiscovery.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtil {

    lateinit var m_sharedPreference: SharedPreferences
    lateinit var m_sharedPrefEditor: SharedPreferences.Editor
    var SHARED_PREF_NAME = "PREF_Beacon"

    private fun setSharedPreference(p_context: Context?) {
        if (!::m_sharedPreference.isInitialized && p_context != null) m_sharedPreference =
            p_context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun setEditor(p_context: Context?) {
        setSharedPreference(p_context)
        if (!::m_sharedPrefEditor.isInitialized) m_sharedPrefEditor = m_sharedPreference.edit()
    }

    fun setPref(context: Context?, p_spKey: String?, p_value: Any) {
        setEditor(context)
        when (p_value) {
            String -> m_sharedPrefEditor.putString(p_spKey, p_value as String)
            Int -> m_sharedPrefEditor.putInt(p_spKey, p_value as Int)
            Boolean -> m_sharedPrefEditor.putBoolean(p_spKey, p_value as Boolean)
            Long -> m_sharedPrefEditor.putLong(p_spKey, p_value as Long)
            Float -> m_sharedPrefEditor.putFloat(p_spKey, p_value as Float)
        }
        m_sharedPrefEditor.commit()
    }

    fun setIntSharedPref(p_context: Context?, p_spKey: String?, p_value: Int?) {
        setEditor(p_context)
        m_sharedPrefEditor.putInt(p_spKey, p_value!!)
        m_sharedPrefEditor.commit()
    }

    fun setStringSharedPref(p_context: Context?, p_spKey: String?, p_value: String?) {
        setEditor(p_context)
        m_sharedPrefEditor.putString(p_spKey, p_value)
        m_sharedPrefEditor.commit()
    }

    fun setBooleanPref(p_context: Context?, p_spKey: String?, p_value: Boolean) {
        setEditor(p_context)
        m_sharedPrefEditor.putBoolean(p_spKey, p_value)
        m_sharedPrefEditor.commit()
    }

    fun getIntSharedPref(p_context: Context?, p_spKey: String?, p_value: Int): Int {
        setSharedPreference(p_context)
        return m_sharedPreference.getInt(p_spKey, p_value)
    }

    fun getStringSharedPref(p_context: Context?, p_spKey: String?, p_value: String?): String? {
        setSharedPreference(p_context)
        return m_sharedPreference.getString(p_spKey, p_value)
    }

    fun getBooleanSharedPref(p_context: Context?, p_spKey: String?, p_value: Boolean): Boolean {
        setSharedPreference(p_context)
        return m_sharedPreference.getBoolean(p_spKey, p_value)
    }

    fun clearPref(p_context: Context?) {
        setEditor(p_context)
        m_sharedPrefEditor.clear().commit()
    }

}