package com.colarsort.app.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.colarsort.app.data.entities.ProductMaterials
import com.colarsort.app.data.pojo.ProductMaterialDetails

@Dao
interface ProductMaterialDao : BaseDao<ProductMaterials>
{
    @Query("DELETE FROM product_material WHERE id = :id")
    suspend fun deleteById(id : Int)

    @Query("SELECT * FROM product_material")
    suspend fun getAllData() : List<ProductMaterials>

    @Query("""
        SELECT
            pm.id AS productMaterialId,
            m.id AS materialId,
            m.name AS materialName,
            m.unit AS materialUnit,
            pm.quantityRequired AS quantityRequired,
            p.name AS productName,
            p.image AS productImage
        FROM product_material AS pm
        JOIN materials AS m ON pm.materialId = m.id
        JOIN products AS p ON pm.productId = p.id
        WHERE p.id = :productId;
    """)
    suspend fun getProductMaterialDetails(productId: Int) : List<ProductMaterialDetails>

    @Query("DELETE FROM product_material WHERE materialId = :id")
    suspend fun deleteByMaterialId(id: Int)

    @Query("DELETE FROM product_material WHERE productId = :id")
    suspend fun deleteByProductId(id: Int)

    @Query("""
        SELECT COUNT(*)
        FROM product_material pm
        INNER JOIN order_items AS oi ON oi.productId = pm.productId
        INNER JOIN orders AS o ON o.id = oi.orderId
        WHERE pm.materialId = :materialId AND o.status != 'Completed';
    """)
    suspend fun countActiveOrdersUsingMaterial(materialId : Int) : Long

    @Query("""
        SELECT COUNT(*)
        FROM product_material pm
        INNER JOIN order_items AS oi ON oi.productId = pm.productId
        INNER JOIN orders AS o ON o.id = oi.orderId
        WHERE pm.productId = :productId AND o.status != 'Completed';
    """)
    suspend fun countActiveOrdersUsingProduct(productId : Int) : Long

    @Query("SELECT EXISTS(SELECT 1 FROM product_material WHERE productId = :productId)")
    suspend fun hasMaterialsForProduct(productId: Int) : Boolean
}