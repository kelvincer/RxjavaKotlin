package com.home.retrofitrxjavademo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import android.view.View

import com.google.gson.JsonObject

class FilterViewModel : ViewModel() {

    val filterDataModel: FilterDataModelTwo
    val messageLiveData: MutableLiveData<String>
    val ayudaListObjLiveData: MutableLiveData<AyudListObject>
    val fmlOverlay: MutableLiveData<Int>
    val readyLiveData: MutableLiveData<Boolean>
    val enableViewLiveData: MutableLiveData<Boolean>

    init {
        filterDataModel = FilterDataModelTwo()
        messageLiveData = MutableLiveData()
        ayudaListObjLiveData = MutableLiveData()
        fmlOverlay = MutableLiveData()
        readyLiveData = MutableLiveData()
        enableViewLiveData = MutableLiveData()
        setupObservers()
    }

    fun getAllData() {
        filterDataModel.newuniqueCall()
    }

    private fun setupObservers() {
        filterDataModel.messageLiveData.observeForever { s -> messageLiveData.setValue(s) }
        filterDataModel.dataLiveData.observeForever {
            ayudaListObjLiveData.value = it
        }
    }

    /*internal fun getAyudaListaData(dataType: String, jsonObject: JsonObject?) {
        ValidateAndGetAyudaListTask(this, jsonObject, dataType).execute()
    }

    internal fun getElementoElctData(jsonObject: JsonObject) {
        ValidateAndGetInterrupTask(this, jsonObject).execute()
    }

    internal fun getEnableViewLiveData(): LiveData<Boolean> {
        return enableViewLiveData
    }

    internal fun getMessageLiveData(): LiveData<String> {
        return messageLiveData
    }

    internal fun getAyudaListObjLiveData(): LiveData<AyudaListaObject> {
        return ayudaListObjLiveData
    }

    internal fun getFmlOverlay(): LiveData<Int> {
        return fmlOverlay
    }

    internal fun getReadyLiveData(): LiveData<Boolean> {
        return readyLiveData
    }

    internal fun getElemElectLiveData(): LiveData<AyudaListaObject> {
        return elemElectLiveData
    }

    private class ValidateAndGetAyudaListTask internal constructor(
            internal var viewModel: FilterViewModel,
            internal var jsonObject: JsonObject?,
            internal var dataType: String
    ) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg voids: Void): Boolean? {
            return Helper.isOnline()
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            super.onPostExecute(aBoolean)
            if (aBoolean!!)
                viewModel.filterDataModel.getAyudaListaData(dataType, jsonObject)
            else {
                viewModel.enableViewLiveData.setValue(false)
                viewModel.messageLiveData.setValue("No tienes acceso a internet")
                viewModel.fmlOverlay.setValue(View.GONE)
                RetrofitClient.client!!.dispatcher().cancelAll()
            }
        }
    }

    private class ValidateAndGetInterrupTask internal constructor(
            internal var viewModel: FilterViewModel,
            internal var jsonObject: JsonObject
    ) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg voids: Void): Boolean? {
            return Helper.isOnline()
        }

        override fun onPostExecute(connected: Boolean?) {
            super.onPostExecute(connected)
            if (connected!!) {
                viewModel.filterDataModel.getElementoElectricoData(jsonObject)
            } else {
                viewModel.enableViewLiveData.setValue(false)
                viewModel.messageLiveData.setValue("No tienes acceso a internet")
                RetrofitClient.client!!.dispatcher().cancelAll()
            }
        }
    }*/
}
