package com.colarsort.app.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object
    {
        const val DATABASE_NAME = "colarsort.db"
        const val DATABASE_VERSION = 1
    }
    object UserTable
    {
        const val TABLE_NAME = "Users"
        const val ID = "user_id"
        const val USERNAME = "username"
        const val ROLE = "role"
        const val PASSWORD = "password"
    }
    object MaterialsTable
    {
        const val TABLE_NAME = "Materials"
        const val ID = "material_id"
        const val NAME = "material_name"
        const val QUANTITY = "quantity"
        const val UNIT = "unit"
        const val LOW_STOCK_THRESHOLD = "low_stock_threshold"
    }
    object ProductsTable
    {
        const val TABLE_NAME = "Products"
        const val ID = "product_id"
        const val NAME = "product_name"
        const val IMAGE = "product_image"
    }
    object ProductMaterialTable
    {
        const val TABLE_NAME = "Product_Materials"
        const val ID = "product_material_id"
        const val PRODUCT_ID = "product_id"
        const val MATERIAL_ID = "material_id"
        const val QUANTITY_REQUIRED = "quantity_required"
    }
    object OrdersTable
    {
        const val TABLE_NAME = "Orders"
        const val ID = "order_id"
        const val CUSTOMER_NAME = "customer_name"
        const val STATUS = "status"
        const val EXPECTED_DELIVERY = "expected_delivery"
    }
    object OrderItemsTable
    {
        const val TABLE_NAME = "Order_Items"
        const val ID = "order_item_id"
        const val ORDER_ID = "order_id"
        const val PRODUCT_ID = "product_id"
        const val QUANTITY = "quantity"
    }
    object ProductionStatusTable
    {
        const val TABLE_NAME = "Production_Status"
        const val ID = "production_id"
        const val ORDER_ITEM_ID = "order_item_id"
        const val CUTTING_STATUS = "cutting_status"
        const val STITCHING_STATUS = "stitching_status"
        const val EMBROIDERY_STATUS = "embroidery_status"
        const val FINISHING_STATUS = "finishing_status"
    }

    override fun onCreate(db: SQLiteDatabase?)
    {
        val createTableQuery =
            """
            CREATE TABLE IF NOT EXISTS ${UserTable.TABLE_NAME} (
            ${UserTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${UserTable.USERNAME} TEXT NOT NULL,
            ${UserTable.ROLE} TEXT NOT NULL,
            ${UserTable.PASSWORD} TEXT NOT NULL
            );
            
            CREATE TABLE IF NOT EXISTS ${MaterialsTable.TABLE_NAME} (
            ${MaterialsTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${MaterialsTable.NAME} TEXT NOT NULL,
            ${MaterialsTable.QUANTITY} REAL NOT NULL, 
            ${MaterialsTable.UNIT} TEXT NOT NULL,
            ${MaterialsTable.LOW_STOCK_THRESHOLD} REAL NOT NULL
            );
            
            CREATE TABLE IF NOT EXISTS ${ProductsTable.TABLE_NAME} (
            ${ProductsTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${ProductsTable.NAME} TEXT NOT NULL,
            ${ProductsTable.IMAGE} BLOB
            );
            
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
            
            CREATE TABLE IF NOT EXISTS ${OrdersTable.TABLE_NAME} (
            ${OrdersTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${OrdersTable.CUSTOMER_NAME} TEXT NOT NULL,
            ${OrdersTable.STATUS} TEXT NOT NULL,
            ${OrdersTable.EXPECTED_DELIVERY} TEXT
            );
            
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
            """
        db?.execSQL(createTableQuery)
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