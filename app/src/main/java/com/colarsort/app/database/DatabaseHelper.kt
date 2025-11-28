package com.colarsort.app.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.colarsort.app.utils.UtilityHelper.hashPassword

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{
    companion object
    {
        const val DATABASE_NAME = "CollarSort.db"
        const val DATABASE_VERSION = 1
    }
    override fun onCreate(db: SQLiteDatabase?)
    {
        val createTableQuery = listOf(
            """
                CREATE TABLE IF NOT EXISTS ${UserTable.TABLE_NAME} (
                ${UserTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${UserTable.USERNAME} TEXT NOT NULL,
                ${UserTable.ROLE} TEXT NOT NULL,
                ${UserTable.PASSWORD} TEXT NOT NULL
                );
            """.trimIndent(),
            """
                CREATE TABLE IF NOT EXISTS ${MaterialsTable.TABLE_NAME} (
                ${MaterialsTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${MaterialsTable.NAME} TEXT NOT NULL,
                ${MaterialsTable.QUANTITY} REAL NOT NULL, 
                ${MaterialsTable.UNIT} TEXT NOT NULL,
                ${MaterialsTable.LOW_STOCK_THRESHOLD} REAL NOT NULL,
                ${MaterialsTable.IMAGE} BLOB
                );
            """.trimIndent(),
            """
                CREATE TABLE IF NOT EXISTS ${ProductsTable.TABLE_NAME} (
                ${ProductsTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ProductsTable.NAME} TEXT NOT NULL,
                ${ProductsTable.IMAGE} BLOB
                );
            """.trimIndent(),
            """
                CREATE TABLE IF NOT EXISTS ${ProductMaterialTable.TABLE_NAME} (
                ${ProductMaterialTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ProductMaterialTable.PRODUCT_ID} INTEGER NOT NULL,
                ${ProductMaterialTable.MATERIAL_ID} INTEGER NOT NULL,
                ${ProductMaterialTable.QUANTITY_REQUIRED} REAL NOT NULL,
                FOREIGN KEY(${ProductMaterialTable.PRODUCT_ID}) 
                    REFERENCES ${ProductsTable.TABLE_NAME}(${ProductsTable.ID}) 
                    ON DELETE CASCADE,
                FOREIGN KEY(${ProductMaterialTable.MATERIAL_ID}) 
                    REFERENCES ${MaterialsTable.TABLE_NAME}(${MaterialsTable.ID}) 
                    ON DELETE CASCADE
                );
            """.trimIndent(),
            """
                CREATE TABLE IF NOT EXISTS ${OrdersTable.TABLE_NAME} (
                ${OrdersTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${OrdersTable.CUSTOMER_NAME} TEXT NOT NULL,
                ${OrdersTable.STATUS} TEXT NOT NULL,
                ${OrdersTable.EXPECTED_DELIVERY} TEXT
                );
            """.trimIndent(),
            """
                CREATE TABLE IF NOT EXISTS ${OrderItemsTable.TABLE_NAME} (
                ${OrderItemsTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${OrderItemsTable.ORDER_ID} INTEGER NOT NULL,
                ${OrderItemsTable.PRODUCT_ID} INTEGER NOT NULL,
                ${OrderItemsTable.QUANTITY} INTEGER NOT NULL, 
                FOREIGN KEY(${OrderItemsTable.ORDER_ID})
                    REFERENCES ${OrdersTable.TABLE_NAME}(${OrdersTable.ID})
                    ON DELETE CASCADE,
                FOREIGN KEY(${OrderItemsTable.PRODUCT_ID})
                    REFERENCES ${ProductsTable.TABLE_NAME}(${ProductsTable.ID})
                    ON DELETE CASCADE
                );
            """.trimIndent(),
            """
                CREATE TABLE IF NOT EXISTS ${ProductionStatusTable.TABLE_NAME} (
                ${ProductionStatusTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT, 
                ${ProductionStatusTable.ORDER_ITEM_ID} INTEGER NOT NULL,
                ${ProductionStatusTable.CUTTING_STATUS} INTEGER NOT NULL,
                ${ProductionStatusTable.STITCHING_STATUS} INTEGER NOT NULL,
                ${ProductionStatusTable.EMBROIDERY_STATUS} INTEGER NOT NULL,
                ${ProductionStatusTable.FINISHING_STATUS} INTEGER NOT NULL,
                FOREIGN KEY(${ProductionStatusTable.ORDER_ITEM_ID})
                    REFERENCES ${OrderItemsTable.TABLE_NAME}(${OrderItemsTable.ID})
                    ON DELETE CASCADE
                );
            """.trimIndent(),
            """
                INSERT INTO ${UserTable.TABLE_NAME} (
                     ${UserTable.USERNAME},
                     ${UserTable.ROLE},
                     ${UserTable.PASSWORD}
                 )
                 SELECT 'admin', 'Manager', '${hashPassword("admin")}'
                 WHERE NOT EXISTS (
                    SELECT 1 FROM ${UserTable.TABLE_NAME} 
                    WHERE ${UserTable.USERNAME} = 'admin' 
                 );
            """.trimIndent(),
            """
                INSERT INTO ${UserTable.TABLE_NAME} (
                     ${UserTable.USERNAME},
                     ${UserTable.ROLE},
                     ${UserTable.PASSWORD}
                 )
                 SELECT 'user', 'Worker', '${hashPassword("user")}'
                 WHERE NOT EXISTS (
                    SELECT 1 FROM ${UserTable.TABLE_NAME} 
                    WHERE ${UserTable.USERNAME} = 'user' 
                 );
            """.trimIndent()
        )

        createTableQuery.forEach { query ->
            db?.execSQL(query)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {
        val tables = arrayOf(
            UserTable.TABLE_NAME,
            MaterialsTable.TABLE_NAME,
            ProductsTable.TABLE_NAME,
            ProductMaterialTable.TABLE_NAME,
            OrdersTable.TABLE_NAME,
            OrderItemsTable.TABLE_NAME,
            ProductionStatusTable.TABLE_NAME
        )

        tables.forEach { table ->
            db?.execSQL("DROP TABLE IF EXISTS $table")
        }

        onCreate(db)
    }
}