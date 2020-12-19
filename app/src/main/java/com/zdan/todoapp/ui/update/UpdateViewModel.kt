package com.zdan.todoapp.ui.update

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdan.todoapp.data.Todo
import com.zdan.todoapp.data.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class UpdateViewModel @ViewModelInject constructor(private val repository: TodoRepository) :
    ViewModel() {

    private val _item = MutableLiveData<Todo>()
    val item: LiveData<Todo>
        get() = _item

    private val _toastLiveData = MutableLiveData<String>()
    val toastLiveData: LiveData<String>
        get() = _toastLiveData

    fun updateTodo(description: String?, isImportant: Boolean, callBack: (Boolean) -> Unit) {
        item.value?.let { itemUpdated ->
            if (description.isNullOrEmpty()) {
                // field is empty - show toast
                _toastLiveData.value = "Empty field!"
                Timber.d("description null")
                callBack.invoke(false)
            } else {
                // update item
                itemUpdated.description = description
                itemUpdated.isImportant = isImportant
                // add to DB using repository
                viewModelScope.launch(Dispatchers.IO) {
                    repository.updateTodo(itemUpdated)
                }
                callBack.invoke(true)
            }
        }
    }

    fun getItem(uid: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getTodoById(uid)
            if (list.isNullOrEmpty()) {
                Timber.d("getItem error: id does not exist!")
            } else {
                _item.postValue(list[0])
            }
        }
    }


}