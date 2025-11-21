package com.colarsort.app.utils

import androidx.recyclerview.widget.RecyclerView

object RecyclerUtils
{
    /**
     * Replaces the current list content with the items provided by the repository
     * and notifies the adapter of all newly inserted items.
     *
     * This is typically used during the first load of data into a RecyclerView.
     *
     * @param list The mutable list backing the adapter.
     * @param repoItems The items retrieved from the repository.
     * @param adapter The RecyclerView adapter to notify.
     */
    fun <T> initialize(list: MutableList<T>, repoItems: List<T>, adapter: RecyclerView.Adapter<*>)
    {
        list.clear()
        list.addAll(repoItems)
        adapter.notifyItemRangeInserted(0, list.size)
    }

    /**
     * Appends only the newly added items from the repository to the current list
     * and updates the adapter accordingly.
     *
     * This is useful when refreshing data and only new entries need to be displayed,
     * avoiding unnecessary full list updates.
     *
     * @param list The current list backing the adapter.
     * @param repoItems The full updated list from the repository.
     * @param adapter The RecyclerView adapter to notify of inserted items.
     * ```
     * RecyclerUtils.insertedItems(materialList, materialsRepo.getAll(), adapter)
     * ```
     */
    fun <T> insertedItems(list: MutableList<T>, repoItems: List<T>, adapter: RecyclerView.Adapter<*>)
    {
        val oldSize = list.size
        val newSize = repoItems.size

        // Add only newly added items
        if (newSize > oldSize) {
            val newItems = repoItems.subList(oldSize, newSize)
            list.addAll(newItems)
            adapter.notifyItemRangeInserted(oldSize, newItems.size)
        }
    }

    /**
     * Updates a single item in the list by matching its identifier using [idSelector].
     * If the item exists, it is replaced and the adapter is notified of the change.
     *
     * @param list The list containing existing items.
     * @param updated The updated item to replace the old one.
     * @param adapter The RecyclerView adapter to notify of the item update.
     * @param idSelector A lambda used to extract a unique identifier from each item.
     *
     * ```
     * RecyclerUtils.updateItem(materialList, materialData, adapter) {it.id}
     * ```
     */
    fun <T> updateItem(
        list: MutableList<T>,
        updated: T,
        adapter: RecyclerView.Adapter<*>,
        idSelector: (T) -> Any?
    ) {
        val index = list.indexOfFirst { idSelector(it) == idSelector(updated) }
        if (index != -1) {
            list[index] = updated
            adapter.notifyItemChanged(index)
        }
    }

    /**
     * Removes an item from the list at the specified position and notifies the adapter.
     *
     * @param targetList The list from which an item will be removed.
     * @param position The index of the item to delete.
     * @param adapter The adapter to notify of the removal.
     *
     * ### Usage Example:
     * ```
     * val position = materialList.indexOf(material)
     * RecyclerUtils.deleteAt(materialList, position, adapter)
     * ```
     */
    fun <T> deleteAt(targetList: MutableList<T>, position: Int, adapter: RecyclerView.Adapter<*>)
    {
        targetList.removeAt(position)
        adapter.notifyItemRemoved(position)
    }
}