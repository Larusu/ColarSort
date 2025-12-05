package com.colarsort.app.data.repository

import android.content.Context
import com.colarsort.app.data.db.dao.ProductMaterialDao
import com.colarsort.app.data.entities.ProductMaterials
import com.colarsort.app.data.pojo.ProductMaterialDetails
import com.colarsort.app.utils.UtilityHelper.loadFromJson
import kotlin.collections.forEach


class ProductMaterialsRepo(dao : ProductMaterialDao) : BaseRepo<ProductMaterials, ProductMaterialDao>(dao)
{
    suspend fun getAll() : List<ProductMaterials> = dao.getAllData()

    suspend fun deleteColumn(id: Int) = dao.deleteById(id)

    suspend fun getMaterialsPerProduct(productId: Int) : List<ProductMaterialDetails> =
        dao.getProductMaterialDetails(productId)

    suspend fun deleteByProductOrMaterialId(id: Int?, isProduct: Boolean = true)
    {
        if (id == null) return

        if (isProduct) {
            dao.deleteByProductId(id)
        } else {
            dao.deleteByMaterialId(id)
        }
    }

    suspend fun isMaterialUsedInAnyOrder(materialId: Int): Boolean =
        dao.countActiveOrdersUsingMaterial(materialId) > 0L

    suspend fun isProductUsedInAnyOrder(productId: Int): Boolean =
        dao.countActiveOrdersUsingProduct(productId) > 0L

    suspend fun checkProductIfExists(productId: Int) : Boolean =
        dao.hasMaterialsForProduct(productId)

    suspend fun insertInitialProductMaterials(context: Context) {
        val fileName = "product_materials/productMaterials.json"
        val items : List<ProductMaterials> = loadFromJson(context, fileName)

        items.forEach { json ->
            val entity = ProductMaterials(
                id = 0,
                productId = json.productId,
                materialId = json.materialId,
                quantityRequired = json.quantityRequired
            )
            dao.insert(entity)
        }
    }
}