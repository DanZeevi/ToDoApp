package com.zdan.todoapp.data

import timber.log.Timber
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val todoDao: TodoDao,
) {

    fun getAllTodos(includeCompleted: Boolean = false, query: String, sortDsc: Boolean = true, importantFirst: Boolean = true) =
            todoDao.getAllDscByQueryIncludeCompletedImportantFirst(query, includeCompleted, sortDsc, importantFirst)

    fun updateTodo(todo: Todo) {
        todoDao.update(todo)
    }

    fun removeTodo(todo: Todo) {
        Timber.d("delete from repository")
        todoDao.delete(todo)
    }

    suspend fun addTodo(todo: Todo) = todoDao.insert(todo)

    fun getTodoById(uid: Int) = todoDao.getItemById(uid)

}