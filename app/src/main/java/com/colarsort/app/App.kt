package com.colarsort.app

import android.app.Application
import androidx.room.Room
import com.colarsort.app.data.db.AppDatabase
import com.colarsort.app.data.repository.UsersRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.colarsort.app.utils.AppPreference

class App : Application()
{
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "CollarSort"
        ).build()

        CoroutineScope(Dispatchers.IO).launch {
            if (AppPreference.isFirstRun(this@App)) {
                val repo = UsersRepo(database.userDao())
                repo.initializeUsers()
                AppPreference.setFirstRunDone(this@App)
            }
        }
    }
}