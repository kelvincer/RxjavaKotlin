package com.home.retrofitrxjavademo

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.nio.channels.SelectableChannel

class FilterDataModel {

    private val TAG = FilterDataModel::class.java.simpleName;
    private val service = RetrofitClient.ayudaApiService
    var messageLiveData: MutableLiveData<String> = MutableLiveData()
    var dataLiveData: MutableLiveData<AyudListObject> = MutableLiveData()

    @SuppressLint("CheckResult")
    fun getData() {

        val request = service?.getAyudaListaData(buildEmpresaBody(Constants.entityEmpresa))
        request?.flatMap {
            manageEmpresaResponse(it)
            service?.getAyudaListaData(buildEstadoInterBody(Constants.entityEstadoInterrup))
        }?.flatMap { response ->
            manageEstadoResponse(response)
            service?.getAyudaListaData(
                buildElemElectBody(
                    Constants.entityElementoElectrico,
                    getElementJsonData(response.datos.get(0).id)
                )
            )
        }?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ response ->
                manageElementoResponse(response)
            }, { error -> error.printStackTrace() }, { Log.d(TAG, "Chain completed") })
    }

    private fun getElementJsonData(id: Int): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.add("IdEmpresa", JsonNull.INSTANCE)
        jsonObject.addProperty("IdEstadoInterrupcion", id)
        return jsonObject
    }

    @SuppressLint("CheckResult")
    fun getTension(entityType: String) {
        val request = service?.getAyudaListaData(buildNivelTensionBody(entityType))
        request?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { response -> manageTensionResponse(response) },
                { error -> error.printStackTrace() },
                { Log.d(TAG, "Tension completed ") })
    }

    @SuppressLint("CheckResult")
    fun uniqueCall() {
        Observable.zip(service?.getAyudaListaData(
            buildNivelTensionBody(Constants.entityNivelTension)
        )?.onErrorReturn {
            Log.d(TAG, "uniquecall Error 1")
            //messageLiveData.postValue("Nivel Tension Error")
            messageLiveData.postValue("Nivel Tension Error 2")
            //messageLiveData.postValue("Nivel Tension Error")
            it.printStackTrace()
            AyudListResponse(idError = 2, datos = emptyList(), mensaje = "Error", tried = 0, idAplicacion = 0)
        }, newgetFlatMap()?.onErrorReturn {
            Log.d(TAG, "uniquecall error 2")
            it.printStackTrace()
        }, BiFunction<AyudListResponse, Unit, Any> { r, a ->
            manageTensionResponse(r)
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                Log.d(TAG, "response any")
            }, { error ->
                Log.d(TAG, "error filter")
                messageLiveData.value = "ERROR"
                error.printStackTrace()
            }, { Log.d(TAG, "Task completed ") })

    }

    @SuppressLint("CheckResult")
    fun newuniqueCall() {
        /*Observable.zip(service?.getAyudaListaData(
            buildNivelTensionBody(Constants.entityNivelTension)
        )?.subscribeOn(Schedulers.newThread()),
            newgetFlatMap()?.subscribeOn(Schedulers.newThread()),
            BiFunction<AyudListResponse, Unit, Any> { r, a ->
                manageTensionResponse(r)
            }).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                Log.d(TAG, "response any")
            }, { error ->
                Log.d(TAG, "error filter")
                messageLiveData.value = "ERROR"
                error.printStackTrace()
            }, { Log.d(TAG, "Task completed ") })*/

        service?.getAyudaListaData(
            buildNivelTensionBody(Constants.entityNivelTension)
        )?.subscribeOn(Schedulers.io())?.zipWith(newgetFlatMap()?.subscribeOn(Schedulers.io()),
            BiFunction<AyudListResponse, Unit, Any> { r, a ->
                manageTensionResponse(r)
            })?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ response ->
            Log.d(TAG, "response any")
        }, { error ->
            Log.d(TAG, "error filter")
            messageLiveData.value = "ERROR"
            error.printStackTrace()
        }, { Log.d(TAG, "Task completed ") })
        /*val single1 = Single.fromCallable { getData1() }
            .subscribeOn(Schedulers.newThread())
        val single2 = Single.fromCallable { getData2() }
            .subscribeOn(Schedulers.newThread())

        single1.zipWith(single2, BiFunction { s1: String, s2: String -> Log.d(TAG, "$s1 $s2") })
            .subscribeBy(
                onSuccess = { println("success") },
                onError = { println("error") })*/
    }

    fun getFlatMap(): Observable<Unit>? {
        return service?.getAyudaListaData(buildEmpresaBody(Constants.entityEmpresa))
            ?.onErrorReturn {
                Log.d(TAG, "getflat error 1")
                messageLiveData.postValue("Empresa Error")
                it.printStackTrace()
                AyudListResponse(idError = 2, datos = emptyList(), mensaje = "Error", tried = 0, idAplicacion = 0)
            }?.flatMap {
                manageEmpresaResponse(it)
                service.getAyudaListaData(buildEstadoInterBody(Constants.entityEstadoInterrup)).onErrorReturn {
                    Log.d(TAG, "getflat error 2")
                    messageLiveData.postValue("Estado Interrupcion Error")
                    it.printStackTrace()
                    AyudListResponse(idError = 2, datos = emptyList(), mensaje = "Error", tried = 0, idAplicacion = 0)
                }
            }?.flatMap {
                manageEstadoResponse(it)
                if (it.datos.isNotEmpty()) {
                    return@flatMap service.getAyudaListaData(
                        buildElemElectBody(
                            Constants.entityElementoElectrico, getElementJsonData(it.datos.get(0).id)
                        )
                    ).onErrorReturn {
                        Log.d(TAG, "Error size 0")
                        AyudListResponse(
                            idError = 2,
                            datos = emptyList(),
                            mensaje = "Error",
                            tried = 0,
                            idAplicacion = 0
                        )
                    }
                }
                return@flatMap Observable.empty<AyudListResponse>().onErrorReturn {
                    messageLiveData.postValue("Elemento Electrico Error")
                    Log.d(TAG, "getflat error 3")
                    it.printStackTrace()
                    AyudListResponse(idError = 2, datos = emptyList(), mensaje = "Error", tried = 0, idAplicacion = 0)
                }
            }?.map {
                manageElementoResponse(it)
            }?.onErrorReturn {
                messageLiveData.postValue("Last error")
                Log.d(TAG, "Last Error")
            }
    }

    fun newgetFlatMap(): Observable<Unit>? {
        return service?.getAyudaListaData(buildEmpresaBody(Constants.entityEmpresa))
            ?.subscribeOn(Schedulers.io())
            ?.flatMap {
                manageEmpresaResponse(it)
                service.getAyudaListaData(buildEstadoInterBody(Constants.entityEstadoInterrup))
                    .subscribeOn(Schedulers.io())
            }?.flatMap {
                manageEstadoResponse(it)
                if (it.datos.isNotEmpty()) {
                    return@flatMap service.getAyudaListaData(
                        buildElemElectBody(
                            Constants.entityElementoElectrico, getElementJsonData(it.datos.get(0).id)
                        )
                    ).subscribeOn(Schedulers.io())
                }
                return@flatMap Observable.empty<AyudListResponse>()
            }?.map {
                manageElementoResponse(it)
            }/*?.observeOn(AndroidSchedulers.mainThread())*/
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

    private fun getData1(): String {
        // some long running operation
        Log.d(TAG, "hello")
        return "hello"
    }

    private fun getData2(): String {
        // some long running operation
        Log.d(TAG, "world")
        return "world"
    }
}