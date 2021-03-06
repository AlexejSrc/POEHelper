package com.resdev.poehelper.view.pop_up_window

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.resdev.poehelper.MyApplication
import com.resdev.poehelper.R
import com.resdev.poehelper.model.room.ItemEntity
import com.resdev.poehelper.utils.getGraphDataset
import com.resdev.poehelper.utils.roundPercentages
import com.resdev.poehelper.utils.setupGraph
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_info_window.view.*

fun setupWindow(itemEntity: ItemEntity,  view: View){
    val currentValue = MyApplication.getCurrentValue()
    Picasso.get().load(currentValue.getDetails().icon).into(view.item_graph_cuurency_image)
    Picasso.get().load(getUrl(itemEntity)).into(view.item_graph_item_image)
    view.findViewById<TextView>(R.id.item_graph_percentage).text = roundPercentages(itemEntity.sparkline.totalChange)
    if ((itemEntity.sparkline.totalChange)>=0.0){
        view.findViewById<TextView>(R.id.item_graph_percentage).setTextColor(ContextCompat.getColor(view.context, R.color.green))
    }
    else{
        view.findViewById<TextView>(R.id.item_graph_percentage).setTextColor(ContextCompat.getColor(view.context, R.color.red))
    }
    view.item_graph_exchange_rate.text = if (itemEntity.chaosValue == null){
         view.context.getString(R.string.no_data)
    }
    else{ ("1.0 "+view.context.resources.getString( R.string.string_for) +
            " %.2f".format(itemEntity.chaosValue!! /(currentValue.getLine().chaosEquivalent?:1.0)))
    }
    val buyingGraph = view.findViewById<LineChart>(R.id.buying_item_graph)
    setupGraph(buyingGraph)
    var receiveValue =  (itemEntity.chaosValue?: 1.0)/ currentValue.getLine().chaosEquivalent!!
    buyingGraph.data = LineData(
        getGraphDataset(itemEntity.sparkline.getData(),
        receiveValue,
        true, view.context))



}

private fun getUrl(item: ItemEntity):String{
    return when {
        item.icon==null -> ""
        item.icon!!.endsWith(".png") -> {
            item.icon!!
        }
        item.variant!=null -> {
            (item.icon+("&${item.variant?.toLowerCase()}=1"))
        }
        else -> item.icon!!
    }
}









