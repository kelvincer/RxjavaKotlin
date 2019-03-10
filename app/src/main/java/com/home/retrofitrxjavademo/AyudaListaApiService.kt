package com.home.retrofitrxjavademo

import com.google.gson.JsonObject
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AyudaListaApiService {

    @POST("AyudaLista")
    fun getAyudaListaData(@Body jsonObject: JsonObject): Observable<AyudListResponse>
}
