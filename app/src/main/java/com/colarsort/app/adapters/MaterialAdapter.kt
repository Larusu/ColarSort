package com.colarsort.app.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colarsort.app.R
import com.colarsort.app.data.entities.Materials
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt


class MaterialAdapter(private val materials: ArrayList<Materials>,
                      private var userRole: String = "Worker"
) : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    var onItemMoreClickListener: ((Materials, View) -> Unit)? = null

    fun setUserRole(role: String) { userRole = role }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val materialImg: ImageView = itemView.findViewById(R.id.iv_material_image)
        val materialId: TextView = itemView.findViewById(R.id.tv_material_id)
        val materialName: TextView = itemView.findViewById(R.id.tv_material_name)
        val materialUnit: TextView = itemView.findViewById(R.id.tv_material_unit)
        val materialQuantity: TextView = itemView.findViewById(R.id.tv_material_quantity)
        val itemMore: ImageView = itemView.findViewById(R.id.iv_more)

        fun bind(material: Materials) {
            if (!material.image.isNullOrEmpty()) {
                val bitmap = BitmapFactory.decodeFile(material.image)
                materialImg.setImageBitmap(bitmap)
            } else {
                materialImg.setImageResource(R.drawable.default_img)
            }
            materialId.text = material.id.toString()
            materialName.text = material.name
            materialUnit.text = material.unit
            materialQuantity.text = material.quantity.toString()

            val lowStock = material.quantity <= material.lowStockThreshold

            if (lowStock) {
                itemView.findViewById<CardView>(R.id.material_card).setCardBackgroundColor("#9a0002".toColorInt())
            } else {
                itemView.findViewById<CardView>(R.id.material_card).setCardBackgroundColor("#241d1d".toColorInt())
            }

            if (userRole == "Worker") {
                itemMore.visibility = View.GONE
            } else {
                itemMore.visibility = View.VISIBLE
                itemMore.setOnClickListener {
                    onItemMoreClickListener?.invoke(material, it)
                }
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