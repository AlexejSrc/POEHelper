package com.resdev.poehelper.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.resdev.poehelper.MyApplication
import com.resdev.poehelper.model.Config
import com.resdev.poehelper.model.room.ItemEntity
import com.resdev.poehelper.repository.ItemRepository
import com.resdev.poehelper.utils.fromRetrofitItemToRoomEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookmarksViewModel @Inject constructor(application: Application) : AndroidViewModel(application){
    @Inject lateinit var repository: ItemRepository
    @Inject lateinit var config: Config
    private  var _itemsData: MutableLiveData<List<ItemEntity>> = MutableLiveData()
    private  var itemsData: MutableLiveData<List<ItemEntity>> = MutableLiveData()
    private var filter = ""

    private var job: Job? = null
    init {
        launchUpdating()
        _itemsData.observeForever {
            filterData(it)
        }
        config.getObservableLeague().observeForever{
            restartLaunchUpdating()
        }
        config.getObservableCurrency().observeForever{
            restartLaunchUpdating()
        }

    }

    private fun filterData(itemsModel: List<ItemEntity>){
        itemsData.postValue(itemsModel
            .filter {(it.translatedName?:it.name).toLowerCase().contains(filter.toLowerCase())})

    }

    fun setFiler(filter: String){
        this.filter = filter
        _itemsData.value?.let {
            filterData(it)
        }

    }

    fun loadItems(){
        viewModelScope.launch(IO) { updateBookmarksItems() }
    }

    fun getItems(): LiveData<List<ItemEntity>> {
        return itemsData
    }


    fun launchUpdating(){
        job = viewModelScope.launch(IO) {
            while (true){
                updateBookmarksItems()
                _itemsData.postValue(repository.getItemsFromDatabase())
                delay(60_000)
            }
        }
    }

    fun restartLaunchUpdating(){
        job?.cancel()
        launchUpdating()
    }

    suspend fun updateBookmarksItems(){
        var itemsTypes = repository.getTypes()
        _itemsData.value?.let { value ->
            val idMap = value.map { it.id }
            for (i in itemsTypes){
                val items = repository.getItem(i)
                items.bindModel()
                for (j in items.lines){
                    val id = idMap.indexOf(j.id)
                    if (id!=-1){
                        repository.updateItem(
                            fromRetrofitItemToRoomEntity(
                                j,
                                i
                            )
                        )
                    }
                }
            }
        }

    }
}