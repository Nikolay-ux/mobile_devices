package com.example.myapplication1.model

data class MetalRate(
    val code: String,
    val name: String,
    val date: String,
    val buyPrice: Double,
    val sellPrice: Double
) {
    fun getFormattedBuyPrice(): String {
        return String.format("%.2f ₽/г", buyPrice)
    }

    fun getFormattedSellPrice(): String {
        return String.format("%.2f ₽/г", sellPrice)
    }
}