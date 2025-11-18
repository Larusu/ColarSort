package com.colarsort.app.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.colarsort.app.R
import com.colarsort.app.models.Products
import android.widget.ImageView
import android.widget.TextView

class ProductAdapter(private val products: ArrayList<Products>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.productImage.setImageBitmap(product.image as Bitmap?)
        holder.productName.text = product.name
    }

    override fun getItemCount(): Int {
        return products.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage = itemView.findViewById<ImageView>(R.id.iv_productImage)
        val productName = itemView.findViewById<TextView>(R.id.tv_productName)
    }

}




