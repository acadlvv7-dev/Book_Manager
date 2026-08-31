package com.example.bookmanager.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bookmanager.data.Book

@Dao
interface BookDao {
    @Query("SELECT * FROM books")
    fun getAllBooks(): LiveData<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBook(book: Book)

    @Update
    fun updateBook(book: Book)

    @Query("SELECT * FROM books WHERE genre = :genre")
    fun getBooksByGenre(genre: String): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE isFavorite = 1")
    fun getFavoriteBooks(): LiveData<List<Book>>
}
