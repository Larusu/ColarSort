package com.colarsort.app.models

/**
 * Converts a model into an ordered array of values.
 *
 * The order of values must exactly match the corresponding table columns
 * that is defined in repository using this model.
 *
 * Returns `null` for fields that are allowed to be stored as null.
 */
interface RowConversion {
    fun toRow(): Array<Any?>
}