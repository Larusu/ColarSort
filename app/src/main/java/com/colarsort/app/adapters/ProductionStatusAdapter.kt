package com.colarsort.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.colarsort.app.R
import com.colarsort.app.databinding.ItemProductionProductsOrderedBinding
import com.colarsort.app.models.ProductionStatus
import com.colarsort.app.repository.ProductionStatusDisplay
import com.colarsort.app.repository.ProductionStatusRepo

class ProductionStatusAdapter(
    private val items: ArrayList<ProductionStatusDisplay>,
    private val productionStatusRepo: ProductionStatusRepo
) : RecyclerView.Adapter<ProductionStatusAdapter.ViewHolder>() {

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

            updateCardColor(holder)

            cbCutting.setOnCheckedChangeListener(null)
            cbCutting.isChecked = item.cuttingStatus
            cbCutting.setOnCheckedChangeListener { _, isChecked ->
                item.cuttingStatus = isChecked
                updateStatus(item)
                updateCardColor(holder)
            }

            cbStitching.setOnCheckedChangeListener(null)
            cbStitching.isChecked = item.stitchingStatus
            cbStitching.setOnCheckedChangeListener { _, isChecked ->
                item.stitchingStatus = isChecked
                updateStatus(item)
                updateCardColor(holder)
            }

            cbEmbroidery.setOnCheckedChangeListener(null)
            cbEmbroidery.isChecked = item.embroideryStatus
            cbEmbroidery.setOnCheckedChangeListener { _, isChecked ->
                item.embroideryStatus = isChecked
                updateStatus(item)
                updateCardColor(holder)
            }

            cbFinishing.setOnCheckedChangeListener(null)
            cbFinishing.isChecked = item.finishingStatus
            cbFinishing.setOnCheckedChangeListener { _, isChecked ->
                item.finishingStatus = isChecked
                updateStatus(item)
                updateCardColor(holder)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun updateStatus(item: ProductionStatusDisplay) {
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
    }

    private fun updateCardColor(holder: ViewHolder) {
        val b = holder.binding

        val allChecked = b.cbCutting.isChecked &&
                b.cbStitching.isChecked &&
                b.cbEmbroidery.isChecked &&
                b.cbFinishing.isChecked

        val colorRes =
            if (allChecked) R.color.green
            else R.color.dark

        b.cvOrderStatus.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, colorRes)
        )
    }
}
