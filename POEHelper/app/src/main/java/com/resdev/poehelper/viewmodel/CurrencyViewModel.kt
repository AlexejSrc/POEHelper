package com.resdev.poehelper.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.resdev.poehelper.MyApplication
import com.resdev.poehelper.model.Config
import com.resdev.poehelper.model.pojo.CurrenciesModel
import com.resdev.poehelper.repository.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CurrencyViewModel(val type: String, application: Application) : AndroidViewModel(application) {
    val repository = MyApplication.getCurrencyRepository()
    private var _currenciesData : MutableLiveData<CurrenciesModel> = MutableLiveData()
    private var currenciesData: MutableLiveData<CurrenciesModel> = MutableLiveData()
    private var filter = ""
    private var job: Job? = null
    init {
        updateListOfValues()
        _currenciesData.observeForever {
            filterData(it)
        }
        Config.getObservableLeague().observeForever{
            restartUpdatingOfListOfValue()
        }
        Config.getObservableCurrency().observeForever{
            restartUpdatingOfListOfValue()
        }
    }

    private fun filterData(currenciesModel: CurrenciesModel){
        val data = currenciesModel.lines
            .filter {(currenciesModel.language.translations[it.currencyTypeName]?:it.currencyTypeName).toLowerCase().contains(filter.toLowerCase())}
        val copy = currenciesModel.copy()
        copy.lines = data.toList()
        currenciesData.postValue(copy)
    }

    fun setFiler(filter: String){
        this.filter = filter
        filterData(_currenciesData.value!!)
    }

    fun restartUpdatingOfListOfValue(){
        job?.cancel()
        updateListOfValues()
    }
    fun updateListOfValues(){
        job = viewModelScope.launch(Dispatchers.IO) {
            while (true){
                _currenciesData.postValue(repository.getCurrency(type))
                delay(60_000)
            }
        }
    }

    fun getItems():LiveData<CurrenciesModel>{
        return currenciesData
    }






}
