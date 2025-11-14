package com.colarsort.app.database

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