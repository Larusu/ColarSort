package com.colarsort.app.session

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context)
{
    private val session = context.getSharedPreferences("", Context.MODE_PRIVATE)

    fun saveUser(userId: Int, role: String) {
        session.edit {
            putInt("user_id", userId)
                .putString("role", role)
        }
    }

    fun getUserId(): Int = session.getInt("user_id", -1)
    fun getRole(): String? = session.getString("role", null)

    fun clearSession() {
        session.edit { clear() }
    }

    fun isLoggedIn(): Boolean = getUserId() != -1
}