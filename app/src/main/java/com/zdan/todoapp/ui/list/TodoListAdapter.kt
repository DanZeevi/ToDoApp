package com.zdan.todoapp.ui.list

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zdan.todoapp.R
import com.zdan.todoapp.data.Todo
import com.zdan.todoapp.databinding.ItemTodoBinding
import com.zdan.todoapp.util.toDateString
import timber.log.Timber

class TodoListAdapter(
    val onItemUpdated: (item: Todo) -> Unit,
    val onLongClick: (item: Todo) -> Unit,
) : RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    private var list: List<Todo> = listOf()
    private var itemSelected: Todo? = null
    private var itemDeselected: Todo? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

/*    override fun onViewRecycled(holder: ViewHolder) {
        Timber.d("view recycled")
        super.onViewRecycled(holder)
    }*/

    override fun getItemCount(): Int = list.size

    fun submitList(todoList: List<Todo>) {
        list = todoList
        notifyDataSetChanged()
    }

    fun selectItem(item: Todo): Boolean {
        Timber.d("item selected in list adapter: $item")
        val position = list.indexOf(item)
        return if (position == -1) {
            false
        } else {
            itemSelected = item
            notifyItemChanged(position)
            true
        }
    }

    fun deselectItem(item: Todo) {
        Timber.d("item deselected from list adapter: $item")

        val position = list.indexOf(item)
        if (position == -1){
            Timber.d("deselect item does not exist!")
        } else {
            itemDeselected = item
            notifyItemChanged(position)
        }
    }


    inner class ViewHolder(private val itemViewBinding: ItemTodoBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {

        fun bind(position: Int) {
            val item = list[position]
            itemViewBinding.apply {
                // description text view
                textViewDescription.text = item.description
                // date created text view
                textViewDateCreated.text = item.dateCreated.toDateString()
                // check box completed
                checkBoxCompleted.isChecked = item.isCompleted
                // update item completed
                checkBoxCompleted.setOnClickListener { checkBox ->
                    val isChecked = (checkBox as CheckBox).isChecked
                    item.isCompleted = isChecked
                    onItemUpdated(item)
                }
                // show/ hide important icon
                imageViewImportant.visibility = when (item.isImportant) {
                    true -> VISIBLE
                    false -> INVISIBLE
                }
                // update background if selected
                if (itemSelected == item) {
                    highlightSelected(root)
                }
                if (itemDeselected == item) {
                    itemSelected = null
                    itemDeselected = null
                    removeHighlight(root)
                }

                // on long click
                root.setOnLongClickListener {
                    Timber.d("long click")
                    onLongClick(item)
                    return@setOnLongClickListener true
                }
            }
        }

        private fun highlightSelected(view: View) {
            view.apply {
                isSelected = true
                background = ContextCompat.getDrawable(view.context, R.drawable.shape_list_item_selected)
                invalidate()
            }
        }

        private fun removeHighlight(view: View) {
            view.apply {
                isSelected = false
                background = ContextCompat.getDrawable(view.context, R.drawable.shape_list_item)
                invalidate()
            }
        }
    }
}