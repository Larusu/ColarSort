package com.colarsort.app.database

object UserTable
{
    const val TABLE_NAME = "users"
    const val ID = "id"
    const val USERNAME = "username"
    const val ROLE = "role"
    const val PASSWORD = "password"
}
object MaterialsTable
{
    const val TABLE_NAME = "materials"
    const val ID = "id"
    const val NAME = "name"
    const val QUANTITY = "quantity"
    const val UNIT = "unit"
    const val LOW_STOCK_THRESHOLD = "low_stock_threshold"
    const val IMAGE = "image"
}
object ProductsTable
{
    const val TABLE_NAME = "products"
    const val ID = "id"
    const val NAME = "name"
    const val IMAGE = "image"
}
object ProductMaterialTable
{
    const val TABLE_NAME = "product_materials"
    const val ID = "id"
    const val PRODUCT_ID = "product_id"
    const val MATERIAL_ID = "material_id"
    const val QUANTITY_REQUIRED = "quantity_required"
}
object OrdersTable
{
    const val TABLE_NAME = "orders"
    const val ID = "id"
    const val CUSTOMER_NAME = "customer_name"
    const val STATUS = "status"
    const val EXPECTED_DELIVERY = "expected_delivery"
}
object OrderItemsTable
{
    const val TABLE_NAME = "order_items"
    const val ID = "id"
    const val ORDER_ID = "order_id"
    const val PRODUCT_ID = "product_id"
    const val QUANTITY = "quantity"
}
object ProductionStatusTable
{
    const val TABLE_NAME = "production_status"
    const val ID = "id"
    const val ORDER_ITEM_ID = "order_item_id"
    const val CUTTING_STATUS = "cutting_status"
    const val STITCHING_STATUS = "stitching_status"
    const val EMBROIDERY_STATUS = "embroidery_status"
    const val FINISHING_STATUS = "finishing_status"
}