package com.example.swipetask

data class ProductModel(
    var image: String,
    var product_name: String,
    var product_type: String,
    var tax: Double,
    var price: Double,

    // post response

)

data class ProductResponse(
    var message : String,
    var product_details: ProductModel,
    var product_id: Int,
    var success : Boolean
)
