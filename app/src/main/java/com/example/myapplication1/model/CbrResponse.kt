package com.example.myapplication1.model

import org.simpleframework.xml.*

@Root(name = "Metall", strict = false)
data class CbrResponse @JvmOverloads constructor(
    @field:ElementList(inline = true, required = false)
    var records: List<Record> = ArrayList()
)

@Root(name = "Record", strict = false)
data class Record @JvmOverloads constructor(
    @field:Attribute(name = "Date", required = false)
    var date: String = "",

    @field:Attribute(name = "Code", required = false)
    var code: String = "",

    @field:Element(name = "Buy", required = false)
    var buy: String = "",

    @field:Element(name = "Sell", required = false)
    var sell: String = ""
) {
    fun buyToDouble(): Double? {
        return buy.replace(",", ".").toDoubleOrNull()
    }

    fun sellToDouble(): Double? {
        return sell.replace(",", ".").toDoubleOrNull()
    }
}