package com.home.retrofitrxjavademo

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.RuntimeException

import java.util.concurrent.TimeUnit

object RetrofitClient {
    val AYUDA_LISTA_BASE_URL = "http://ensa.com.pe:50002/SISAC/Maestro/Api/Control/"
    private var ayudaListaApiService: AyudaListaApiService? = null
    var client: OkHttpClient? = null

    val ayudaApiService: AyudaListaApiService?
        get() {
            if (ayudaListaApiService == null) {

                val retrofit = Retrofit.Builder()
                        .baseUrl(AYUDA_LISTA_BASE_URL)
                        .client(buildOkHttpClient() ?: throw RuntimeException("Invalid client"))
                        .addConverterFactory(GsonConverterFactory.create(buildGson()))
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build()

                ayudaListaApiService = retrofit.create(AyudaListaApiService::class.java)
            }
            return ayudaListaApiService
        }

    private fun buildGson(): Gson {
        return GsonBuilder()
                .serializeNulls()
                .create()
    }

    private fun buildOkHttpClient(): OkHttpClient? {
        if (client == null) {
            client = OkHttpClient().newBuilder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()
        }
        return client
    }
}
