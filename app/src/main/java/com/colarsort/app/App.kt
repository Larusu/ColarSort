package com.colarsort.app

import android.app.Application
import androidx.room.Room
import com.colarsort.app.data.db.AppDatabase
import com.colarsort.app.data.repository.MaterialsRepo
import com.colarsort.app.data.repository.ProductMaterialsRepo
import com.colarsort.app.data.repository.ProductsRepo
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
                val userRepo = UsersRepo(database.userDao())
                val materialRepo = MaterialsRepo(database.materialDao())
                val productsRepo = ProductsRepo(database.productDao())
                val productMaterialsRepo = ProductMaterialsRepo(database.productMaterialDao())

                userRepo.initializeUsers()
                materialRepo.insertInitialMaterials(this@App)
                productsRepo.insertInitialProducts(this@App)
                productMaterialsRepo.insertInitialProductMaterials(this@App)
                AppPreference.setFirstRunDone(this@App)
            }
        }
    }
}