package com.zdan.todoapp.ui.create

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

class CreateViewModel @ViewModelInject constructor(private val repository: TodoRepository): ViewModel() {

    private val _toastLiveData = MutableLiveData<String>()
    val toastLiveData: LiveData<String>
        get() = _toastLiveData

    fun createTodo(description: String?, isImportant: Boolean, callBack: (Boolean) -> Unit) {
        if (description.isNullOrEmpty()) {
            // field is empty - show toast
            _toastLiveData.value = "Empty field!"
            Timber.d("description null")
            callBack.invoke(false)
        } else {
            // create item
            val todoItem = Todo(description = description, isImportant = isImportant)
            // add to DB using repository
            viewModelScope.launch(Dispatchers.IO) {
                repository.addTodo(todoItem)
            }
            callBack.invoke(true)
        }
    }


}