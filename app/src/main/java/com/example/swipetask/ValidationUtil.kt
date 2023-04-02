package com.example.swipetask

object ValidationUtil {
    fun validateProduct(product: ProductModel) : Boolean {
        if (product.product_type != "" && product.product_name != "") {
            return true
        }
        return false
    }
}