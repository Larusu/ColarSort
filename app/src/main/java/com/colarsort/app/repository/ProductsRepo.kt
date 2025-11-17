package com.colarsort.app.repository

import android.content.Context
import com.colarsort.app.database.DatabaseHelper
import com.colarsort.app.database.ProductsTable
import com.colarsort.app.models.Products

class ProductsRepo(dbHelper: DatabaseHelper) : CRUDRepo<Products>(dbHelper)
{
    override val tableName: String = ProductsTable.TABLE_NAME
    override val tableRows: Array<String> = arrayOf(ProductsTable.NAME, ProductsTable.IMAGE)

    /**
     * Reads an image file from the 'assets' directory and returns as ByteArray
     * It is useful to store image in the Products data class that expects ByteArray
     *
     * @param context - the context use to access the assets folder
     * @param image - the filename of the image in assets directory
     *
     * Example:
     * val bytes = inputStreamToByteArray(this, "Sample.png")
     */
    fun inputStreamToByteArray(context: Context, image: String) : ByteArray
    {
        val inputStream = context.assets.open(image)
        val imageBytes = inputStream.readBytes()
        inputStream.close()

        return imageBytes
    }
}