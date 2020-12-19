package com.zdan.todoapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    // retrieve item by ID
    @Query("SELECT * FROM todo WHERE uid = :uid")
    fun getItemById(uid: Int): List<Todo>

    // retrieve all by date created ordered ascending (old to new)
    @Query("SELECT * FROM todo ORDER BY date_created ASC")
    fun getAllAsc(): Flow<List<Todo>>

    // retrieve all by date created ordered ascending (new to old)
    @Query("SELECT * FROM todo WHERE description LIKE '%' || :search || '%'  ORDER BY date_created DESC")
    fun getAllDsc(search: String): Flow<List<Todo>>

    // retrieve all by date created ordered ascending (new to old)
    @Query("SELECT * FROM todo WHERE is_completed IN (0, :IncludeCompleted) AND description LIKE '%' || :search || '%' ORDER BY CASE WHEN :importantFirst = 1 THEN is_important END DESC, CASE WHEN :sortDsc = 1 THEN date_created END DESC, CASE WHEN :sortDsc = 0 THEN date_created END ASC")
    fun getAllDscByQueryIncludeCompletedImportantFirst(search: String, IncludeCompleted: Boolean, sortDsc: Boolean = true, importantFirst: Boolean): Flow<List<Todo>>

    // retrieve important first

    // insert
    @Insert
    suspend fun insert(todo: Todo)

    // delete
    @Delete
    fun delete(todo: Todo)

    // update
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(todo: Todo)

}