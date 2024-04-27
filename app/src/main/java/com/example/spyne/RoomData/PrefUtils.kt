package com.example.spyne.RoomData

import android.content.Context
import android.content.SharedPreferences

object PrefUtils {
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("SPYNE_DATA", Context.MODE_PRIVATE)
    }

    @JvmStatic
    fun storeVersionCode(context: Context, apiKey: String?, name: String?) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(name, apiKey)
        editor.commit()
    }

    @JvmStatic
    fun getVersionCode(context: Context, name: String?): String? {
        return getSharedPreferences(context).getString(name, "")
    }

    @JvmStatic
    fun removeVersionCode(context: Context, name: String?): Boolean {
        val editor = getSharedPreferences(context).edit()
        return editor.remove(name).commit()
    }
}