package com.colarsort.app.data.repository

import com.colarsort.app.data.db.dao.MaterialDao
import com.colarsort.app.data.entities.Materials
import com.colarsort.app.data.pojo.ProductIdAndQuantity

class MaterialsRepo(dao : MaterialDao) : BaseRepo<Materials, MaterialDao>(dao)
{
    suspend fun getAll() : List<Materials> = dao.getAllData()

    suspend fun searchMaterialBaseOnName(searchName : String) : List<Materials> =
        dao.searchMaterialsByName("%$searchName%")

    suspend fun deleteById(id : Int) : Boolean
    {
        val material = dao.getDataById(id)
        val rowsDeleted = dao.delete(material)
        return rowsDeleted > 0
    }
    suspend fun setQuantity(quantity : Int, productId : Int)
    {
        val materialWithQuantity = dao.getMaterialsWithQuantityRequired(productId)

        materialWithQuantity.forEach { it ->
            val newQuantity = it.quantity - (quantity * it.quantityRequired)
            dao.updateQuantity(it.materialId, newQuantity)
        }
    }

    suspend fun checkMaterialQuantity(items : List<ProductIdAndQuantity>) : List<String>
    {
        val requiredMap = mutableMapOf<Int, Double>()

        items.forEach { item ->
            dao.getMaterialsWithQuantityRequired(item.productId).forEach { m->
                val totalRequired = m.quantityRequired * item.quantity
                requiredMap[m.materialId] =
                    (requiredMap[m.materialId] ?: 0.0) + totalRequired
            }
        }

        val materialIds = requiredMap.keys.toList()
        val materials = dao.getMaterialsByIds(materialIds).associateBy { it.id }

        val insufficient = mutableListOf<String>()

        requiredMap.forEach { (materialId, totalRequired) ->
            val material = materials[materialId] ?: return@forEach

            if (material.quantity < totalRequired) insufficient.add(material.name)
        }

        return insufficient
    }
}