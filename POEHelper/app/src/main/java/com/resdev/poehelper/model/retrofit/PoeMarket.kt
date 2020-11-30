package com.resdev.poehelper.model.retrofit

import com.resdev.poehelper.model.Config
import com.resdev.poehelper.model.poemarket.PoeMarketResponse
import com.resdev.poehelper.model.poemarket.RequestBuilder
import com.resdev.poehelper.model.room.ItemEntity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//PoeMarketApi retrofit
object PoeMarket {
    private lateinit var retrofit : Retrofit
    private lateinit var poeMarketClient : PoeMarketApi
    init {
        Config.getObservableLanguage().observeForever{
            rebuildRetrofit(it)
        }
    }

    fun sendItemRequest(itemsEntity: ItemEntity): PoeMarketResponse? {
        val item = RequestBuilder.generateItemLink(itemsEntity)
        return try{
            poeMarketClient.sendItemRequest(Config.getLeague(), item)
                ?.execute()
                ?.body()!!
        } catch (e: Exception){
            null
        }
    }

    fun sendCurrencyRequest(want: String, have: String): PoeMarketResponse? {
        val item = RequestBuilder.generateCurrencyLink(want, have)
        return try{ poeMarketClient.sendCurrencyRequest(Config.getLeague(), item)
            ?.execute()
            ?.body()!!
        } catch (e: Exception){
            null
        }
    }

    fun rebuildRetrofit(language: String){
        var url = when(language){
            "en"-> "https://www.pathofexile.com"
            "ko"-> "https://poe.game.daum.net"
            "ge"-> "https://de.pathofexile.com"
            else-> "https://${language}.pathofexile.com"
        }
        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        poeMarketClient = retrofit.create(PoeMarketApi::class.java)
    }
}