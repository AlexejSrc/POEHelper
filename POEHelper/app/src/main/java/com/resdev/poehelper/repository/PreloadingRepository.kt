package com.resdev.poehelper.repository

import com.resdev.poehelper.model.retrofit.PoeLeagueLoading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreloadingRepository @Inject constructor(private val poeLeagueLoading: PoeLeagueLoading) {
    var league = CoroutineScope(Dispatchers.Default).async {
        var value = emptyArray<String>()
        while (value.isEmpty()){
            value = poeLeagueLoading.loadLeagues().getEditedLeagues() ?: emptyArray()
            delay(1000)
        }
        value
    }
}