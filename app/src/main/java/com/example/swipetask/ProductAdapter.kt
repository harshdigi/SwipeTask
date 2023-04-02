package com.example.swipetask

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.swipetask.databinding.AdapterProductBinding
import kotlin.math.roundToInt


class ProductAdapter : RecyclerView.Adapter<ProductViewHolder>() {

    var productList = mutableListOf<ProductModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun setProduct(product: List<ProductModel>) {
        this.productList = product.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterProductBinding.inflate(inflater, parent, false)
        return ProductViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {

        val product = productList[position]
        if (ValidationUtil.validateProduct(product)) {
            holder.binding.productName.text = product.product_name
            holder.binding.productType.text = product.product_type
            if (product.image.isEmpty()){
                holder.binding.productImage.setImageResource(R.drawable.brand_logo)
            }
            else{
                Glide.with(holder.itemView.context).load(product.image).into(holder.binding.productImage)
            }
            holder.binding.productPrice.text = "₹" + String.format("%.2f", product.price).toDouble()
            holder.binding.textPercentage.text = "Tax " + product.tax.roundToInt().toString() + "% "
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
    fun update(modelList: MutableList<ProductModel>) {
        productList = modelList
        notifyDataSetChanged()
    }

}

class ProductViewHolder(val binding: AdapterProductBinding) : RecyclerView.ViewHolder(binding.root) {

}