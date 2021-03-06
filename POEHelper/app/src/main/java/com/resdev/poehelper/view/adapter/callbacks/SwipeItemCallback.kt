package com.resdev.poehelper.view.adapter.callbacks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.resdev.poehelper.MyApplication
import com.resdev.poehelper.model.Config
import com.resdev.poehelper.utils.isColorLight
import com.resdev.poehelper.utils.openItemUrl
import com.resdev.poehelper.utils.showSnackbar
import com.resdev.poehelper.view.adapter.ItemAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class SwipeItemCallback(private val config: Config) : ItemTouchHelper.SimpleCallback(0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }
    var swipeBack = false
    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    private fun setTouchListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        recyclerView.setOnTouchListener { _, event ->
            swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (willActionBeTriggered(dX, recyclerView.width) and swipeBack and (dX<0)) {//
                makeOpenAction(viewHolder)
            }
            else if (willActionBeTriggered(dX, recyclerView.width) and swipeBack and (dX>0)){
                makeSaveAction(viewHolder)
            }
            false
        }
    }



    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(
            c!!, recyclerView!!, viewHolder, dX,
            dY, actionState, isCurrentlyActive
        )
        val itemView = viewHolder.itemView
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val paint = Paint()
            if (willActionBeTriggered(dX, recyclerView.width)){
                if (!isColorLight(Integer.toHexString(config.getColor()))){
                    paint.color = config.getColor()
                }
                else{
                    paint.color = Color.BLACK
                }

            }
            else{
                paint.color = Color.GRAY
            }

            paint.textSize = 40F
            paint.textAlign = Paint.Align.CENTER
            setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            var height = itemView.top + itemView.height / 2
            if (dX>0){
                c.drawText("Add to", 150F, (height-(paint.textSize/2)-1), paint)
                c.drawText("bookmarks", 150F, (height+(paint.textSize/2)+1), paint)
            }
            else{
                c.drawText("Check on", (itemView.right - 150).toFloat(), (height-(paint.textSize/2)-1), paint)
                c.drawText("pathofexile", (itemView.right - 150).toFloat(), (height+(paint.textSize/2)+1), paint)
            }

        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


    private fun willActionBeTriggered(dX: Float, viewWidth: Int): Boolean {
        return abs(dX) >= viewWidth / 3.5
    }

    private fun makeOpenAction(viewHolder: RecyclerView.ViewHolder){
        openItemUrl((viewHolder as ItemAdapter.ItemViewHolder).item, viewHolder.itemView)
    }

    private fun makeSaveAction(viewHolder: RecyclerView.ViewHolder){
        val holder= viewHolder as ItemAdapter.ItemViewHolder
        CoroutineScope(Dispatchers.IO).launch { MyApplication.getItemRepository().addItem(holder.item)}
        val itemName = holder.item.translatedName ?: holder.item.name
        showSnackbar( viewHolder.itemView, "$itemName is bookmarked")
    }


}