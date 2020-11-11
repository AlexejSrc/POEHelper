package com.resdev.poehelper.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.resdev.poehelper.model.CurrentValue
import com.resdev.poehelper.R
import com.resdev.poehelper.utils.Util
import com.resdev.poehelper.databinding.ItemViewHolderBinding
import com.resdev.poehelper.model.room.ItemEntity
import com.resdev.poehelper.view.datawrappers.ItemEntityUiWrapper
import com.resdev.poehelper.view.pop_up_window.PopupItemInfoWindowSetuper
import com.resdev.poehelper.view.pop_up_window.PopupItemWindowSetuper
import com.resdev.poehelper.view.pop_up_window.showCurrencyWindow
import com.resdev.poehelper.view.pop_up_window.showItemWindow
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_view_holder.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarkAdapter() :
    ListAdapter<ItemEntity, BookmarkAdapter.BookmarkItemViewHolder>(DIFF_CALLBACK) {

    companion object{
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<ItemEntity> =
            object : DiffUtil.ItemCallback<ItemEntity>() {
                override fun areItemsTheSame(oldItem: ItemEntity, newItem: ItemEntity): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: ItemEntity, newItem: ItemEntity): Boolean {
                    return oldItem.chaosValue == newItem.chaosValue && ((oldItem.translatedName?:oldItem.name) == (newItem.translatedName?:newItem.name))
                }
            }
    }

    var popupWindow: PopupWindow? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemViewHolderBinding>(layoutInflater, R.layout.item_view_holder, parent, false);
        return BookmarkItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener{onClickShowPopupWindow(item,holder.itemView)}
        holder.itemView.setOnLongClickListener{onLongClickShowPopupWindow(item, holder.itemView) }
    }

    class BookmarkItemViewHolder(binding: ItemViewHolderBinding) : RecyclerView.ViewHolder(binding.root){
        lateinit var item : ItemEntity
        var binding: ItemViewHolderBinding = binding
        init {
            setViewDefaults()
        }

        fun bind(itemLine: ItemEntity) {
            item = itemLine
            binding.item = ItemEntityUiWrapper(itemLine, binding.root.context)
            binding.executePendingBindings()
        }

        fun setViewDefaults(){
                itemView.currency_name.text = Util.getFromMap(
                CurrentValue.getDetails().name,
                CurrentValue.getData().language.translations
            )
            Picasso.get().load(CurrentValue.getDetails().icon).into(itemView.currency_view)
        }
    }

    fun onLongClickShowPopupWindow(item: ItemEntity, view: View?): Boolean {
        val popupView: View = LayoutInflater.from(view!!.context).inflate(R.layout.item_stats_info_window, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        if (!PopupItemInfoWindowSetuper.chooseWindow(item, popupView)){
            return true
        }
        this.popupWindow = popupWindow
        popupWindow.showItemWindow(view)
        return true
    }
    fun onClickShowPopupWindow(item: ItemEntity, view: View?):Boolean{
        val popupView: View = LayoutInflater.from(view!!.context).inflate(R.layout.item_info_window, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        PopupItemWindowSetuper.setupWindow(item, popupView)
        this.popupWindow = popupWindow
        popupWindow.showCurrencyWindow(view)
        return true
    }

    fun closeWindow(){
        popupWindow?.dismiss()
    }


}