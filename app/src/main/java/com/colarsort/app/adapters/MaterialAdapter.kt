package com.colarsort.app.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colarsort.app.R
import com.colarsort.app.models.Materials
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt


class MaterialAdapter(private val materials: ArrayList<Materials>) : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    var onItemMoreClickListener: ((Materials, View) -> Unit)? = null
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val materialImg: ImageView = itemView.findViewById(R.id.iv_material_image)
        val materialId: TextView = itemView.findViewById(R.id.tv_material_id)
        val materialName: TextView = itemView.findViewById(R.id.tv_material_name)
        val materialUnit: TextView = itemView.findViewById(R.id.tv_material_unit)
        val materialQuantity: TextView = itemView.findViewById(R.id.tv_material_quantity)
        val itemMore: ImageView = itemView.findViewById(R.id.iv_more)

        fun bind(material: Materials) {
            if (material.image != null) {
                val bitmap = BitmapFactory.decodeByteArray(material.image, 0, material.image.size)
                materialImg.setImageBitmap(bitmap)
            } else {
                materialImg.setImageResource(R.drawable.default_img) // fallback image
            }
            materialId.text = material.id.toString()
            materialName.text = material.name
            materialUnit.text = material.unit
            materialQuantity.text = material.quantity.toString()

            val lowStock = material.quantity!! <= material.stockThreshold!!

            if (lowStock) {
                itemView.findViewById<CardView>(R.id.material_card).setCardBackgroundColor("#9a0002".toColorInt())
            } else {
                itemView.findViewById<CardView>(R.id.material_card).setCardBackgroundColor("#241d1d".toColorInt())
            }

            itemMore.setOnClickListener {
                onItemMoreClickListener?.invoke(material, it)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_material, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(materials[position])
    }

    override fun getItemCount(): Int {
        return materials.size
    }

}