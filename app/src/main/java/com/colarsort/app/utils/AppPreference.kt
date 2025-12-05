package com.colarsort.app.utils

import android.content.Context
import androidx.core.content.edit

object AppPreference
{
    private const val PREF_NAME = "app_prefs"
    private const val KEY_FIRST_RUN = "first_run_done"

    fun isFirstRun(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return !prefs.getBoolean(KEY_FIRST_RUN, false)
    }

    fun setFirstRunDone(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_FIRST_RUN, true) }
    }
}