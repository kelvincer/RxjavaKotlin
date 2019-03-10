package com.home.retrofitrxjavademo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AyudListResponse(@SerializedName("IdError")
                            @Expose
                            var idError: Int,
                            @SerializedName("Mensaje")
                            @Expose
                            var mensaje: String,
                            @SerializedName("Datos")
                            @Expose
                            var datos: List<AyudListDato>,
                            @SerializedName("Tried")
                            @Expose
                            var tried: Int,
                            @SerializedName("IdAplicacion")
                            @Expose
                            var idAplicacion: Int)






