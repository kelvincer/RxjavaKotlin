package com.home.retrofitrxjavademo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSpinner
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_filter_two.*
import java.lang.RuntimeException

import java.util.ArrayList
import java.util.Calendar

class FilterActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    internal var TAG = FilterActivity::class.java.simpleName
    internal var empresaIdList: MutableList<Int?>? = null
    internal var estadoIdList: MutableList<Int?>? = null
    internal var tensionIdList: MutableList<Int?>? = null
    internal var elementoIdList: MutableList<Int?>? = null
    internal lateinit var jsonFilterStorageId: JsonObject
    internal lateinit var jsonSpinnerPosit: JsonObject
    internal lateinit var jsonValues: JsonObject
    internal var selectedEmpresaId: Int? = null
    internal var selectedEstadoId: Int? = null
    internal lateinit var prefManager: PreferenceManager
    private var userIsInteracting: Boolean = false
    private lateinit var viewModel: FilterViewModel

    private val currentSelectedEstado: Int?
        get() {
            if (selectedEstadoId == null) {
                val position = spn_estado.selectedItemPosition
                selectedEstadoId = estadoIdList?.get(position)
            }
            return selectedEstadoId
        }

    private val jsonObjectDatosUser: JsonObject
        get() {
            val prefJsonObject = JsonParser().parse(prefManager.getString(Constants.JSON_USER_DATA)).asJsonObject

            val jsonObject = JsonObject()
            jsonObject.addProperty("IdOrganizacion", prefJsonObject.get("IdOrganizacion").asInt)
            jsonObject.addProperty("IdUsuario", prefJsonObject.get("idUsuario").asInt)

            return jsonObject
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_two)

        viewModel = ViewModelProviders.of(this).get(FilterViewModel::class.java)

        /*initObjects()
        getServiceData()
        setupViews()
        setupObservers()*/

        setupObservers()

        viewModel.getAllData()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        /*if (userIsInteracting) {
            when (parent.id) {
                R.id.spn_empresa -> {
                    saveSpinnerPosition("IdEmpresa", position)
                    if (position == 0) {
                        jsonFilterStorageId?.add("IdEmpresa", JsonNull.INSTANCE)
                    } else {
                        jsonFilterStorageId?.addProperty("IdEmpresa", empresaIdList?.get(position))
                    }
                    selectedEmpresaId = empresaIdList?.get(position)
                    viewModel.getElementoElctData(buildElemJsonObject(currentSelectedEstado, selectedEmpresaId))
                    Log.d(TAG, "spn_empresa")
                }
                R.id.spn_estado -> {
                    saveSpinnerPosition("IdEstadoInterrupcion", position)
                    jsonFilterStorageId?.addProperty("IdEstadoInterrupcion", estadoIdList?.get(position))
                    selectedEstadoId = estadoIdList?.get(position)
                    viewModel.getElementoElctData(buildElemJsonObject(currentSelectedEstado, selectedEmpresaId))
                    Log.d(TAG, "spn_estado")
                }
                R.id.spn_tension -> {
                    saveSpinnerPosition("IdNivelTension", position)
                    jsonFilterStorageId?.addProperty("IdNivelTension", tensionIdList?.get(position))
                }
                R.id.spn_elem_elect -> {
                    saveSpinnerPosition("IdElementoElectrico", position)
                    if (position == 0) {
                        jsonFilterStorageId?.add("IdElementoElectrico", JsonNull.INSTANCE)
                    } else {
                        jsonFilterStorageId?.addProperty("IdElementoElectrico", elementoIdList?.get(position))
                    }
                }
                else -> throw IllegalArgumentException("Invalid spinner id")
            }
            userIsInteracting = false
        }*/
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                saveAndSetResult()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userIsInteracting = true
    }

    override fun onBackPressed() {
        // saveAndSetResult()
        super.onBackPressed()
    }

    private fun saveAndSetResult() {
        prefManager.saveString(Constants.SELECTED_POSITION, jsonSpinnerPosit.toString())
        prefManager.saveString(Constants.FIELDS_VALUES, jsonValues.toString())
        val returnIntent = Intent()
        returnIntent.putExtra("filterIdValues", jsonFilterStorageId.toString())
        setResult(Activity.RESULT_OK, returnIntent)
    }

    private fun initObjects() {
        empresaIdList = mutableListOf()
        estadoIdList = mutableListOf()
        tensionIdList = mutableListOf()
        elementoIdList = mutableListOf()
        prefManager = PreferenceManager(this)

        val filterValues = intent.getStringExtra("filterIdValues")
        if (filterValues != null) {
            jsonFilterStorageId = JsonParser().parse(filterValues).asJsonObject
            val jel = jsonFilterStorageId?.get("IdEstadoInterrupcion")
            if (jel != null && !jel.isJsonNull) {
                selectedEstadoId = jel.asInt
                val jelem = jsonFilterStorageId?.get("IdEmpresa")
                if (jelem != null && !jelem.isJsonNull) {
                    selectedEmpresaId = jelem.asInt
                } else {
                    selectedEmpresaId = null
                }
                jsonSpinnerPosit = JsonParser().parse(prefManager.getString(Constants.SELECTED_POSITION)).asJsonObject
                jsonValues = JsonParser().parse(prefManager.getString(Constants.FIELDS_VALUES)).asJsonObject
            } else {
                jsonFilterStorageId = JsonObject()
                jsonSpinnerPosit = JsonObject()
                jsonValues = JsonObject()
            }
        } else {
            jsonFilterStorageId = JsonObject()
            jsonSpinnerPosit = JsonObject()
            jsonValues = JsonObject()
        }
    }

    private fun saveSpinnerPosition(key: String, pos: Int) {
        jsonSpinnerPosit?.addProperty(key, pos)
    }

    /*private fun getServiceData() {
        viewModel.getAyudaListaData(Constants.entityNivelTension, null)
        viewModel.getAyudaListaData(Constants.entityEstadoInterrup, null)
        viewModel.getAyudaListaData(Constants.entityEmpresa, jsonObjectDatosUser)
    }*/

    private fun setupViews() {

        etx_cantid_inicio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    jsonValues?.addProperty("CantSuminInicio", "")
                    jsonFilterStorageId?.add("CantSuminInicio", JsonNull.INSTANCE)
                } else {
                    jsonValues.addProperty("CantSuminInicio", s.toString())
                    jsonFilterStorageId?.addProperty("CantSuminInicio", s.toString())
                }
            }
        })

        etx_cantid_fin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    jsonValues?.addProperty("CantSuminFin", "")
                    jsonFilterStorageId?.add("CantSuminFin", JsonNull.INSTANCE)
                } else {
                    jsonValues?.addProperty("CantSuminFin", s.toString())
                    jsonFilterStorageId?.addProperty("CantSuminFin", s.toString())
                }
            }
        })

        etx_durac_inicio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    jsonFilterStorageId?.add("DuracInicio", JsonNull.INSTANCE)
                    jsonValues?.addProperty("DuracInicio", "")
                } else {
                    jsonFilterStorageId?.addProperty("DuracInicio", Integer.valueOf(s.toString()) * 3600)
                    jsonValues?.addProperty("DuracInicio", s.toString())
                }
            }
        })

        etx_durac_fin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    jsonFilterStorageId?.add("DuracFin", JsonNull.INSTANCE)
                    jsonValues?.addProperty("DuracFin", "")
                } else {
                    jsonFilterStorageId?.addProperty("DuracFin", Integer.valueOf(s.toString()) * 3600)
                    jsonValues?.addProperty("DuracFin", s.toString())
                }
            }
        })

        etx_fecha_inicio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    jsonFilterStorageId?.add("FechaInicio", JsonNull.INSTANCE)
                    jsonValues?.addProperty("FechaInicio", "")
                }
            }
        })
        etx_fecha_inicio.setOnTouchListener(object : View.OnTouchListener {

            internal val DRAWABLE_RIGHT = 2

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= etx_fecha_inicio.right - etx_fecha_inicio.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        launcDatepickerDialog(1)
                        return true
                    }
                }
                return false
            }
        })

        etx_fecha_fin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    jsonFilterStorageId?.add("FechaFin", JsonNull.INSTANCE)
                    jsonValues?.addProperty("FechaFin", "")
                }
            }
        })
        etx_fecha_fin.setOnTouchListener(object : View.OnTouchListener {
            internal val DRAWABLE_RIGHT = 2

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= etx_fecha_fin.right - etx_fecha_fin.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        launcDatepickerDialog(2)
                        return true
                    }
                }
                return false
            }
        })
        supportActionBar!!.setTitle("Filtro")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun saveInitialFilterValues() {
        jsonFilterStorageId?.add("FechaInicio", JsonNull.INSTANCE)
        jsonFilterStorageId?.add("FechaFin", JsonNull.INSTANCE)
        jsonFilterStorageId?.add("CantSuminInicio", JsonNull.INSTANCE)
        jsonFilterStorageId?.add("CantSuminFin", JsonNull.INSTANCE)
        jsonFilterStorageId?.add("DuracInicio", JsonNull.INSTANCE)
        jsonFilterStorageId?.add("DuracFin", JsonNull.INSTANCE)
        jsonFilterStorageId?.add("IdEmpresa", JsonNull.INSTANCE)

        if (estadoIdList?.isNotEmpty() == true)
            jsonFilterStorageId.addProperty("IdEstadoInterrupcion", estadoIdList?.get(0))
        else
            jsonFilterStorageId.add("IdEstadoInterrupcion", JsonNull.INSTANCE)
        if (tensionIdList?.isNotEmpty() == true)
            jsonFilterStorageId.addProperty("IdNivelTension", tensionIdList?.get(0))
        else
            jsonFilterStorageId.add("IdNivelTension", JsonNull.INSTANCE)
        if (elementoIdList?.isNotEmpty() == true)
            jsonFilterStorageId.addProperty("IdElementoElectrico", elementoIdList?.get(0))
        else
            jsonFilterStorageId.add("IdElementoElectrico", JsonNull.INSTANCE)
        jsonFilterStorageId.add("IdInterrupcion", JsonNull.INSTANCE)

        jsonSpinnerPosit.addProperty("IdEmpresa", 0)
        jsonSpinnerPosit.addProperty("IdEstadoInterrupcion", 0)
        jsonSpinnerPosit.addProperty("IdNivelTension", 0)
        jsonSpinnerPosit.addProperty("IdElementoElectrico", 0)

        jsonValues.addProperty("CantSuminInicio", "")
        jsonValues.addProperty("CantSuminFin", "")
        jsonValues.addProperty("DuracInicio", "")
        jsonValues.addProperty("DuracFin", "")
        jsonValues.addProperty("FechaInicio", "")
        jsonValues.addProperty("FechaFin", "")
    }

    private fun showMessage(s: String) {
        Snackbar.make(spn_tension, s, Snackbar.LENGTH_LONG).show()
    }

    private fun setupObservers() {
        viewModel.messageLiveData.observe(this, Observer { s -> showMessage(s!!) })

        viewModel.ayudaListObjLiveData.observe(this, Observer {
            loadSpinner(it)
        })
    }

    /* private fun getElementosElectricos(estadoIntId: Int?, empresaId: Int?) {
         viewModel.getAyudaListaData(Constants.entityElementoElectrico, buildElemJsonObject(estadoIntId, empresaId))
     }*/

    private fun buildElemJsonObject(estadoIntId: Int?, empresaId: Int?): JsonObject {
        val jsonObject = JsonObject()
        if (empresaId == null)
            jsonObject.add("IdEmpresa", JsonNull.INSTANCE)
        else
            jsonObject.addProperty("IdEmpresa", empresaId)
        jsonObject.addProperty("IdEstadoInterrupcion", estadoIntId)
        return jsonObject
    }

    private fun resetElementoElectSpinner(response: AyudListResponse) {

        val datos = response.datos
        if (datos == null || datos.size == 0) {
            spn_elem_elect.adapter = null
            spn_elem_elect.isEnabled = false
            return
        } else {
            spn_elem_elect.isEnabled = true
        }
        elementoIdList?.clear()
        val categories = ArrayList<String>()
        for (dato in datos) {
            categories.add(dato.nombre)
            elementoIdList?.add(dato.id)
        }
        addAdditionalItems(spn_elem_elect, categories)
        val dataAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = super.getView(position, convertView, parent) as TextView
                tv.textSize = 16f
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorNormalText))
                return tv
            }
        }
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spn_elem_elect.adapter = dataAdapter
        spn_elem_elect.setSelection(0, false)
        spn_elem_elect.onItemSelectedListener = this
        jsonFilterStorageId.addProperty("IdElementoElectrico", datos[0].id)
        jsonSpinnerPosit.addProperty("IdElementoElectrico", 0)
    }

    private fun loadSpinner(ayudListObject: AyudListObject?) {
        when (ayudListObject?.type) {
            Constants.entityEmpresa -> {
                loadSpinnerEmpresa(ayudListObject.ayuda)
            }
            Constants.entityNivelTension -> {
                loadSpinnerTension(ayudListObject.ayuda)
            }
            Constants.entityEstadoInterrup -> {
                loadSpinnerEstado(ayudListObject.ayuda)
            }
            Constants.entityElementoElectrico ->{
                loadSpinnerElemento(ayudListObject.ayuda)
            }
        }

    }

    private fun loadSpinnerElemento(ayuda: AyudListResponse) {
        val categories = ArrayList<String>()
        for (dato in ayuda.datos) {
            categories.add(dato.nombre)
            elementoIdList?.add(dato.id)
        }
        val dataAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = super.getView(position, convertView, parent) as TextView
                tv.textSize = 16f
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorNormalText))
                return tv
            }
        }
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spn_elem_elect.adapter = dataAdapter
    }

    private fun loadSpinnerEstado(ayuda: AyudListResponse) {
        val categories = ArrayList<String>()
        for (dato in ayuda.datos) {
            categories.add(dato.nombre)
            elementoIdList?.add(dato.id)
        }
        val dataAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = super.getView(position, convertView, parent) as TextView
                tv.textSize = 16f
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorNormalText))
                return tv
            }
        }
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spn_estado.adapter = dataAdapter
    }

    private fun loadSpinnerTension(ayuda: AyudListResponse) {
        val categories = ArrayList<String>()
        for (dato in ayuda.datos) {
            categories.add(dato.nombre)
            elementoIdList?.add(dato.id)
        }
        val dataAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = super.getView(position, convertView, parent) as TextView
                tv.textSize = 16f
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorNormalText))
                return tv
            }
        }
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spn_tension.adapter = dataAdapter
    }

    private fun loadSpinnerEmpresa(ayuda: AyudListResponse) {
        val categories = ArrayList<String>()
        for (dato in ayuda.datos) {
            categories.add(dato.nombre)
            elementoIdList?.add(dato.id)
        }
        val dataAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = super.getView(position, convertView, parent) as TextView
                tv.textSize = 16f
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorNormalText))
                return tv
            }
        }
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spn_empresa.adapter = dataAdapter
    }

    private fun checkEmpresaSpinnerAmbito() {
        val jsonString = prefManager.getString(Constants.JSON_USER_DATA)
        val jsonObject = JsonParser().parse(jsonString).asJsonObject
        val jel = jsonObject.get("IdEmpresa")
        if (jel != null) {
            if (!jel.isJsonNull) {
                val ambito = jsonObject.get("AmbitoTotal").asInt
                if (ambito == 0) {
                    val idEmpresa = jel.asInt

                    empresaIdList?.filter { it == idEmpresa }?.forEach {
                        spn_empresa.setSelection(empresaIdList?.indexOf(it) ?: 0)
                        spn_empresa.isEnabled = false
                    }
                }
            }
        }
    }

    private fun updateWithSavedValues() {
        etx_cantid_inicio.setText(jsonValues.get("CantSuminInicio")?.asString)
        etx_cantid_fin.setText(jsonValues.get("CantSuminFin")?.asString)
        etx_durac_inicio.setText(jsonValues.get("DuracInicio")?.asString)
        etx_durac_fin.setText(jsonValues.get("DuracFin")?.asString)
        etx_fecha_inicio.setText(jsonValues.get("FechaInicio")?.asString)
        etx_fecha_fin.setText(jsonValues.get("FechaFin")?.asString)

        spn_estado.setSelection(jsonSpinnerPosit.get("IdEstadoInterrupcion").asInt)
        spn_tension.setSelection(jsonSpinnerPosit.get("IdNivelTension").asInt)
        spn_elem_elect.setSelection(jsonSpinnerPosit.get("IdElementoElectrico").asInt)
        spn_empresa.setSelection(jsonSpinnerPosit.get("IdEmpresa").asInt)
    }

    private fun addAdditionalItems(spinner: AppCompatSpinner, categories: MutableList<String>) {
        when (spinner.id) {
            R.id.spn_empresa -> {
                categories.add(0, "Todas")
                empresaIdList?.add(0, null)
            }
            R.id.spn_elem_elect -> {
                categories.add(0, "Todos")
                elementoIdList?.add(0, null)
            }
        }
    }

    private fun enableViews(enable: Boolean) {
        etx_cantid_inicio.isEnabled = enable
        etx_cantid_fin.isEnabled = enable
        etx_durac_inicio.isEnabled = enable
        etx_durac_fin.isEnabled = enable
        etx_fecha_inicio.isEnabled = enable
        etx_fecha_fin.isEnabled = enable
        spn_estado.isEnabled = enable
        spn_tension.isEnabled = enable
        spn_elem_elect.isEnabled = enable
        spn_empresa.isEnabled = enable
    }

    private fun launcDatepickerDialog(type: Int) {
        val calendar = Calendar.getInstance()
        val datePickerDialog =
                DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    //Esta variable lo que realiza es aumentar en uno el mes ya que comienza desde 0 = enero
                    val mesActual = month + 1
                    //Formateo el día obtenido: antepone el 0 si son menores de 10
                    val diaFormateado = dayOfMonth.toString()
                    //Formateo el mes obtenido: antepone el 0 si son menores de 10
                    val mesFormateado = mesActual.toString()
                    //Muestro la fecha con el formato deseado
                    launchTimePickerDialog(String.format("%s/%s/%d", diaFormateado, mesFormateado, year), type)
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun launchTimePickerDialog(date: String, type: Int) {
        val rightNow = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
                this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            //Formateo el hora obtenido: antepone el 0 si son menores de 10
            val horaFormateada = hourOfDay.toString()
            //Formateo el minuto obtenido: antepone el 0 si son menores de 10
            val minutoFormateado = minute.toString()
            //Muestro la hora con el formato deseado
            val time = String.format("%s:%s", horaFormateada, minutoFormateado)
            val dateAndTime = String.format("%s %s", date, time)
            if (type == 1) {
                etx_fecha_inicio.text.clear()
                etx_fecha_inicio.setText(dateAndTime)
                jsonValues?.addProperty("FechaInicio", dateAndTime)
                jsonFilterStorageId?.addProperty("FechaInicio", dateAndTime)
                etx_fecha_inicio.setSelection(dateAndTime.length)
            } else {
                etx_fecha_fin.text.clear()
                etx_fecha_fin.setText(dateAndTime)
                jsonValues?.addProperty("FechaFin", dateAndTime)
                jsonFilterStorageId?.addProperty("FechaFin", dateAndTime)
                etx_fecha_fin.setSelection(dateAndTime.length)
            }
        },          //Estos valores deben ir en ese orden
                //Al colocar en false se muestra en formato 12 horas y true en formato 24 horas
                //Pero el sistema devuelve la hora en formato 24 horas
                rightNow.get(Calendar.HOUR_OF_DAY), rightNow.get(Calendar.MINUTE), false
        )
        timePickerDialog.show()
    }
}
