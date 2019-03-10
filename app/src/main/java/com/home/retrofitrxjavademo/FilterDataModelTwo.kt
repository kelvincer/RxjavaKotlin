package com.home.retrofitrxjavademo

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.lang.RuntimeException


class FilterDataModelTwo {
    private val TAG = FilterDataModel::class.java.simpleName;
    private val service = RetrofitClient.ayudaApiService
    var messageLiveData: MutableLiveData<String> = MutableLiveData()
    var dataLiveData: MutableLiveData<AyudListObject> = MutableLiveData()
    fun newuniqueCall() {

        val compositeDisposable = CompositeDisposable()
        // Tension request
        val disposable = service?.getAyudaListaData(buildNivelTensionBody(Constants.entityNivelTension))
            ?.subscribeOn(Schedulers.newThread())
           /* ?.observeOn(AndroidSchedulers.mainThread())*/
            ?.subscribe({ response ->
                manageTensionResponse(response)
            }, { error ->
                Log.d(TAG, "error filter")
                messageLiveData.value = "ERROR"
                error.printStackTrace()
            }, { Log.d(TAG, "Task completed 1 ") })

        val dispos = Observable.zip(service?.getAyudaListaData(buildEmpresaBody(Constants.entityEmpresa))
            ?.subscribeOn(Schedulers.newThread()),
            service?.getAyudaListaData(buildEstadoInterBody(Constants.entityEstadoInterrup))
                ?.subscribeOn(Schedulers.newThread()),
            BiFunction<AyudListResponse, AyudListResponse, Unit> { r, a ->
                manageEmpresaResponse(r)
                manageEstadoResponse(a)
                val dis = service?.getAyudaListaData(
                    buildElemElectBody(
                        Constants.entityElementoElectrico, getElementJsonData(a.datos.get(0).id)
                    )
                )?.subscribeOn(Schedulers.io())
                  /*  ?.observeOn(AndroidSchedulers.mainThread())*/
                    ?.subscribe({ response ->
                        manageElementoResponse(response)
                    }, { error ->
                        Log.d(TAG, "error filter")
                        messageLiveData.value = "ERROR"
                        error.printStackTrace()
                    }, { Log.d(TAG, "Task completed 2") })

            })/*.observeOn(AndroidSchedulers.mainThread())*/
            /*.subscribeOn(Schedulers.io())*/
            .subscribe({ response ->
                Log.d(TAG, "response any")
            }, { error ->
                Log.d(TAG, "error filter")
                messageLiveData.value = "ERROR"
                error.printStackTrace()
            }, { Log.d(TAG, "Task completed 3") })
    }

    private fun getElementJsonData(id: Int): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.add("IdEmpresa", JsonNull.INSTANCE)
        jsonObject.addProperty("IdEstadoInterrupcion", id)
        return jsonObject
    }

    private fun manageElementoResponse(response: AyudListResponse) {
        val elemObject = AyudListObject(response, Constants.entityElementoElectrico)
        dataLiveData.postValue(elemObject)
        Log.d(TAG, "Elemento response: ${Gson().toJson(response)}")
    }

    private fun manageEstadoResponse(response: AyudListResponse) {
        val estadoObj = AyudListObject(response, Constants.entityEstadoInterrup)
        dataLiveData.postValue(estadoObj)
        Log.d(TAG, "Estado response: ${Gson().toJson(response)}")
    }

    private fun manageTensionResponse(response: AyudListResponse) {
        val tensionObj = AyudListObject(response, Constants.entityNivelTension)
        dataLiveData.postValue(tensionObj)
        Log.d(TAG, "Tension response: ${Gson().toJson(response)}")
    }

    private fun manageEmpresaResponse(response: AyudListResponse) {
        val empresaObj = AyudListObject(response, Constants.entityEmpresa)
        dataLiveData.postValue(empresaObj)
        Log.d(TAG, "Empresa response: ${Gson().toJson(response)}")
    }

    private fun buildNivelTensionBody(dataType: String): JsonObject {
        val innerObj = JsonObject()
        innerObj.addProperty("Entity", dataType)
        innerObj.addProperty("IdSite", 2)
        val outerObj = JsonObject()
        outerObj.add("Parametros", innerObj)

        return outerObj
    }

    private fun buildEmpresaBody(entityType: String): JsonObject {
        val jsonData = JsonObject()
        jsonData.addProperty("Entity", entityType)
        jsonData.addProperty("IdSite", 2)
        jsonData.addProperty("IdOrganizacion", 1)
        jsonData.addProperty("IdUsuario", 60112)
        val outerObject = JsonObject()
        outerObject.add("Parametros", jsonData)
        return outerObject
    }

    private fun buildEstadoInterBody(dataType: String): JsonObject {
        val innerObj = JsonObject()
        innerObj.addProperty("Entity", dataType)
        innerObj.addProperty("IdSite", 2)
        val outerObj = JsonObject()
        outerObj.add("Parametros", innerObj)

        return outerObj
    }

    private fun buildElemElectBody(dataType: String, jsonData: JsonObject): JsonObject {
        jsonData.addProperty("Entity", dataType)
        jsonData.addProperty("IdSite", 2)
        val outerObj = JsonObject()
        outerObj.add("Parametros", jsonData)
        return outerObj
    }
}