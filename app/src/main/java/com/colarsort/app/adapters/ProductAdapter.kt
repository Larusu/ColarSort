package com.colarsort.app.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.colarsort.app.R
import com.colarsort.app.models.Products
import android.widget.ImageView
import android.widget.TextView

class ProductAdapter(private val products: ArrayList<Products>,
    private var userRole: String = "Manager"
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    var onItemMoreClickListener: ((Products, View) -> Unit)? = null

    fun setUserRole(role: String) { userRole = role }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.iv_productImage)
        val productName: TextView = itemView.findViewById(R.id.tv_productName)
        val itemMenu: ImageView = itemView.findViewById(R.id.iv_more)

        fun bind(product: Products) {
            if (product.image != null) {
                val bitmap = BitmapFactory.decodeFile(product.image)
                productImage.setImageBitmap(bitmap)
            } else {
                productImage.setImageResource(R.drawable.default_img) // fallback image
            }

            productName.text = product.name

            if (userRole == "Worker") {
                itemMenu.visibility = View.GONE
            } else {
                itemMenu.visibility = View.VISIBLE
                itemMenu.setOnClickListener {
                    onItemMoreClickListener?.invoke(product, it)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int {
        return products.size
    }

}




