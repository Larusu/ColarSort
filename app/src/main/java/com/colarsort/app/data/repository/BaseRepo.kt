package com.colarsort.app.data.repository

import com.colarsort.app.data.db.dao.BaseDao

open class BaseRepo<T, D : BaseDao<T>>(protected val dao: D)
{
    open suspend fun insert(model: T) = dao.insert(model)

    open suspend fun update(model: T): Boolean
    {
        dao.update(model)
        return true
    }
}