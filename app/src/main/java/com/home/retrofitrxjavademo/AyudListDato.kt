package com.home.retrofitrxjavademo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AyudListDato(
        @SerializedName("Id")
        @Expose
        var id: Int,
        @SerializedName("Nombre")
        @Expose
        var nombre: String)




