package com.zdan.todoapp.di

import android.content.Context
import androidx.room.Room
import com.zdan.todoapp.data.TodoDao
import com.zdan.todoapp.data.TodoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    fun provideTodoDao(todoDatabase: TodoDatabase): TodoDao
        = todoDatabase.todoDao()

    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext applicationContext: Context)
        = Room
        .databaseBuilder(applicationContext, TodoDatabase::class.java, "Todos")
        .build()
}