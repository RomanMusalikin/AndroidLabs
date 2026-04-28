package com.example.lab10.data
import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("products")
    val productsList: List<Product>
)


