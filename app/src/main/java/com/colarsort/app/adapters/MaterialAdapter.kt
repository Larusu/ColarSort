package com.colarsort.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colarsort.app.R
import com.colarsort.app.models.Materials

class MaterialAdapter(private val materials: ArrayList<Materials>) : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val materialId: TextView = itemView.findViewById(R.id.tv_material_id)
        val materialName: TextView = itemView.findViewById(R.id.tv_material_name)
        val materialUnit: TextView = itemView.findViewById(R.id.tv_material_unit)
        val materialQuantity: TextView = itemView.findViewById(R.id.tv_material_quantity)

        fun bind(material: Materials) {
            materialId.text = material.id.toString()
            materialName.text = material.name
            materialUnit.text = material.unit
            materialQuantity.text = material.quantity.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_material, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MaterialAdapter.ViewHolder, position: Int) {
        holder.bind(materials[position])
    }

    override fun getItemCount(): Int {
        return materials.size
    }

}