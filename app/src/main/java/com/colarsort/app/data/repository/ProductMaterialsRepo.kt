package com.colarsort.app.data.repository

import com.colarsort.app.data.db.dao.ProductMaterialDao
import com.colarsort.app.data.entities.ProductMaterials
import com.colarsort.app.data.pojo.ProductMaterialDetails


class ProductMaterialsRepo(dao : ProductMaterialDao) : BaseRepo<ProductMaterials, ProductMaterialDao>(dao)
{
    suspend fun getAll() : List<ProductMaterials> = dao.getAllData()

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
}