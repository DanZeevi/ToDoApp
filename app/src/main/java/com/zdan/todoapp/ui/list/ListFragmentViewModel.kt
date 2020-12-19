package com.zdan.todoapp.ui.list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zdan.todoapp.data.Todo
import com.zdan.todoapp.data.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class ListFragmentViewModel @ViewModelInject constructor
    (var repository: TodoRepository)
    : ViewModel()
{

    private val _itemSelected = MutableLiveData<Todo>()
    val itemSelected : LiveData<Todo>
        get() = _itemSelected

    private val _itemDeselected = MutableLiveData<Todo>()
    val itemDeselected : LiveData<Todo>
        get() = _itemDeselected

    private val searchQueryLiveData = MutableLiveData<String>()

    private val _sortDscLiveData = MutableLiveData<Boolean>()
    val sortDscLiveData: LiveData<Boolean>
        get() = _sortDscLiveData

    private val _showCompletedLiveData = MutableLiveData<Boolean>()
    val showCompletedLiveData: LiveData<Boolean>
        get() = _showCompletedLiveData

    private val _importantFirstLiveData = MutableLiveData<Boolean>()
    val importantFirstLiveData : LiveData<Boolean>
        get() = _importantFirstLiveData

    val todoList =
        _sortDscLiveData.switchMap { sortDsc ->
            importantFirstLiveData.switchMap { importantFirst ->
        showCompletedLiveData.switchMap { showCompleted ->
            searchQueryLiveData.switchMap { query ->
                liveData {
                    repository.getAllTodos(showCompleted, query, sortDsc, importantFirst)
                        .collect { list ->
                            emit(list)
                        }
                }
                }
            }
        }
    }

    private val _itemToDelete = MutableLiveData<Todo>()
    val itemPendingDelete: LiveData<Todo>
        get() = _itemToDelete

    init {
        // sort list by descending order (new to old)
        _sortDscLiveData.value = true
        // show completed To_dos
        _showCompletedLiveData.value = false
        // sort by important first
        _importantFirstLiveData.value = true
        // empty query
        searchQueryLiveData.value = ""
    }


    fun updateItem(item: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTodo(item)
        }
    }

    private fun deleteItem(item: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("delete from view model")
            repository.removeTodo(item)
        }
    }

    fun onItemSwiped(position: Int) {
        // delete item
        todoList.value?.get(position)?.let { itemToDelete ->
            deleteItem(itemToDelete)
            // show snack bar
            _itemToDelete.value = itemToDelete
            // exit action mode if item swiped was selected
            if (_itemSelected.value == itemToDelete) {
                clearSelection()
            }
        } ?: run {
            Timber.e("error in list or position")
        }
    }

    fun addTodo(itemToDelete: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTodo(itemToDelete)
        }
    }

    fun switchSort() {
        _sortDscLiveData.value?.let { boolean ->
            _sortDscLiveData.value = !boolean
        } ?: run {
            Timber.e("sortAsc null!")
        }
    }

    fun switchShowCompleted() {
        _showCompletedLiveData.value?.let { boolean ->
            _showCompletedLiveData.value = !boolean
        } ?: run {
            Timber.e("showCompleted null!")
        }
    }

    fun switchImportantFirst() {
        _importantFirstLiveData.value?.let { boolean ->
            _importantFirstLiveData.value = !boolean
        } ?: run {
            Timber.e("importantFirst null!")
        }
    }

    fun search(query: String?) {
        if (query.isNullOrEmpty()) {
            searchQueryLiveData.value = ""
        } else {
            searchQueryLiveData.value = query
        }
    }

    fun setItemSelected(item: Todo) {
        if (_itemSelected.value == null) {
            // first selection
            _itemSelected.value = item
        } else {
            // item selected twice needs to be deselected
            clearSelection()
        }
    }

    fun deleteSelected() {
        _itemSelected.value?.let { deleteItem(it) }
        clearSelection()
    }

    fun clearSelection() {
        _itemDeselected.value = _itemSelected.value
        _itemSelected.value = null
    }

    fun setPreferences(importantFirst: Boolean, showCompleted: Boolean, sortDsc: Boolean) {
        _importantFirstLiveData.value = importantFirst
        _showCompletedLiveData.value = showCompleted
        _sortDscLiveData.value = sortDsc
    }
}
