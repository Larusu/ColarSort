package com.colarsort.app.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.colarsort.app.R
import com.colarsort.app.databinding.ItemProductionProductsOrderedBinding
import com.colarsort.app.data.entities.Orders
import com.colarsort.app.data.entities.ProductionStatus
import com.colarsort.app.data.pojo.ProductionStatusDisplay
import com.colarsort.app.data.repository.OrdersRepo
import com.colarsort.app.data.repository.ProductionStatusRepo
import com.colarsort.app.data.repository.RepositoryProvider
import com.colarsort.app.utils.RecyclerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductionStatusAdapter(
    private val items: ArrayList<ProductionStatusDisplay>,
    private val productionStatusRepo: ProductionStatusRepo,
    private val ordersRepo: OrdersRepo = RepositoryProvider.ordersRepo,
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<ProductionStatusAdapter.ViewHolder>() {
    var onDataChanged: (() -> Unit)? = null
    inner class ViewHolder(val binding: ItemProductionProductsOrderedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductionProductsOrderedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        with(holder.binding) {

            tvOrderId.text = item.orderId.toString()
            tvProductName.text = item.productName
            tvProductQuantity.text = item.orderItemsQuantity.toString()

            updateCardColor(item, holder)

            cbCutting.setOnCheckedChangeListener(null)
            cbCutting.isChecked = item.cuttingStatus
            cbCutting.setOnCheckedChangeListener { _, isChecked ->
                item.cuttingStatus = isChecked
                updateStatus(item)
                updateCardColor(item, holder)
                checkOrderCompletion(item.orderId, holder, item, "cutting")
            }

            cbStitching.setOnCheckedChangeListener(null)
            cbStitching.isChecked = item.stitchingStatus
            cbStitching.setOnCheckedChangeListener { _, isChecked ->
                item.stitchingStatus = isChecked
                updateStatus(item)
                updateCardColor(item, holder)
                checkOrderCompletion(item.orderId, holder, item, "stitching")
            }

            cbEmbroidery.setOnCheckedChangeListener(null)
            cbEmbroidery.isChecked = item.embroideryStatus
            cbEmbroidery.setOnCheckedChangeListener { _, isChecked ->
                item.embroideryStatus = isChecked
                updateStatus(item)
                updateCardColor(item, holder)
                checkOrderCompletion(item.orderId, holder, item, "embroidery")
            }

            cbFinishing.setOnCheckedChangeListener(null)
            cbFinishing.isChecked = item.finishingStatus
            cbFinishing.setOnCheckedChangeListener { _, isChecked ->
                item.finishingStatus = isChecked
                updateStatus(item)
                updateCardColor(item, holder)
                checkOrderCompletion(item.orderId, holder, item, "finishing")
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun updateStatus(item: ProductionStatusDisplay) {
        coroutineScope.launch {
            runIO {
                productionStatusRepo.update(
                    ProductionStatus(
                        id = item.productionStatusId,
                        orderItemId = item.orderItemId,
                        cuttingStatus = if (item.cuttingStatus) 1 else 0,
                        stitchingStatus = if (item.stitchingStatus) 1 else 0,
                        embroideryStatus = if (item.embroideryStatus) 1 else 0,
                        finishingStatus = if (item.finishingStatus) 1 else 0
                    )
                )
                val oldOrder = ordersRepo.getDataById(item.orderId)

                ordersRepo.update(
                    Orders(
                        id = item.orderId,
                        customerName = oldOrder.customerName,
                        status = "In Progress",
                        expectedDelivery = oldOrder.expectedDelivery
                    )
                )
            }
        }
    }

    private fun updateCardColor(item: ProductionStatusDisplay, holder: ViewHolder) {
        val allChecked = item.cuttingStatus &&
                item.stitchingStatus &&
                item.embroideryStatus &&
                item.finishingStatus

        val colorRes = if (allChecked) R.color.green else R.color.dark

        holder.binding.cvOrderStatus.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, colorRes)
        )
    }

    /**
     * Checks if ALL production items for the given orderId are fully completed.
     * If yes -> shows confirmation dialog. If confirmed, deletes statuses and updates order status.
     */
    private fun checkOrderCompletion(
        orderId: Int,
        holder: ViewHolder,
        changedItem: ProductionStatusDisplay,
        changedField: String
    ) {
        // Get all items in the adapter with same orderId
        val sameOrderItems = items.filter { it.orderId == orderId }

        if (sameOrderItems.isEmpty()) return

        // Check if all items for that order are fully checked
        val isCompleted = sameOrderItems.all { it.cuttingStatus &&
                it.stitchingStatus &&
                it.embroideryStatus &&
                it.finishingStatus
        }

        if (!isCompleted) {
            sameOrderItems.forEach { it.completionHandled = false }
            return
        }

        if (sameOrderItems.any { it.completionHandled }) return

        sameOrderItems.forEach { it.completionHandled = true }

        val context = holder.binding.root.context

        AlertDialog.Builder(context)
            .setTitle("Order Complete")
            .setMessage("Are you sure this order is already complete?")
            .setPositiveButton("Yes") { _, _ ->
                completeOrder(orderId, sameOrderItems, holder)
            }
            .setNegativeButton("No") { _, _ ->
                // user declined: reset flag so dialog can show again later if needed
                sameOrderItems.forEach { it.completionHandled = false }

                when (changedField) {
                    "cutting" -> changedItem.cuttingStatus = false
                    "stitching" -> changedItem.stitchingStatus = false
                    "embroidery" -> changedItem.embroideryStatus = false
                    "finishing" -> changedItem.finishingStatus = false
                }

                updateStatus(changedItem)

                // Refresh UI
                notifyItemChanged(items.indexOf(changedItem))
            }
            .setCancelable(false)
            .show()
    }

    private fun completeOrder(orderId: Int, itemsSameOrder: List<ProductionStatusDisplay>, holder: ViewHolder) {
        itemsSameOrder.forEach { display ->
            coroutineScope.launch {
                runIO { productionStatusRepo.deleteById(display.productionStatusId) }
            }
        }

        coroutineScope.launch {
            val existingModel = runIO { ordersRepo.getDataById(orderId) }
            val updatedModel = Orders(
                id = existingModel.id,
                customerName = existingModel.customerName,
                status = "Completed",
                expectedDelivery = existingModel.expectedDelivery
            )

            runIO { ordersRepo.update(updatedModel) }
        }

        RecyclerUtils.deleteAll(items, itemsSameOrder, this)
        onDataChanged?.invoke()

        Toast.makeText(holder.binding.root.context, "Order marked as Completed.", Toast.LENGTH_SHORT).show()
    }

    private suspend fun <T> runIO(ioBlock: suspend () -> T): T {
        return withContext(Dispatchers.IO) { ioBlock() }
    }
}
