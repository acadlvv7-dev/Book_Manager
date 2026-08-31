package com.example.bookmanager.fragments

import com.example.bookmanager.adapter.BookAdapter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmanager.AppDatabase
import com.example.bookmanager.R
import com.example.bookmanager.data.Book

class FirstScreenFragment : Fragment(R.layout.fragment_first_screen) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var db: AppDatabase
    private var allBooks = listOf<Book>()
    private var currentFilter = "all"
    private var prevFilter = "all"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getInstance(requireContext())

        recyclerView = view.findViewById(R.id.recyclerView)
        val btnAll = view.findViewById<Button>(R.id.btnAll)
        val btnGenre = view.findViewById<Button>(R.id.btnGenre)
        val btnAuthor = view.findViewById<Button>(R.id.btnAuthor)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        adapter = BookAdapter(
            bookList = emptyList(),
            onFavoriteClick = { book ->
                Thread {
                    db.bookDao().updateBook(book.copy(isFavorite = !book.isFavorite))
                }.start()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        db.bookDao().getAllBooks().observe(viewLifecycleOwner) { books ->
            if (books.isEmpty()) {
                Thread {
                    val sampleBooks = listOf(
                        Book(title = "1984", author = "George Orwell", genre = "Dystopia"),
                        Book(title = "And Then There Were None", author = "Agatha Christie", genre = "Mystery"),
                        Book(title = "Animal Farm", author = "George Orwell", genre = "Satire"),
                        Book(title = "Crime and Punishment", author = "Fyodor Dostoevsky", genre = "Classic"),
                        Book(title = "Dune", author = "Frank Herbert", genre = "Sci-Fi"),
                        Book(title = "For Whom the Bell Tolls", author = "Ernest Hemingway", genre = "Classic"),
                        Book(title = "The Lord of the Rings", author = "J.R.R. Tolkien", genre = "Fantasy")
                    )
                    sampleBooks.forEach { db.bookDao().insertBook(it) }
                }.start()
            }
            allBooks = books
            applyFilter(etSearch.text.toString())
        }

        fun updateButtonStyles(active: Button) {
            listOf(btnAll, btnGenre, btnAuthor).forEach { btn ->
                if (btn == active) {
                    btn.setBackgroundColor(0xFF7B2FBE.toInt())
                    btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                } else {
                    btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                    btn.setTextColor(0xFF7B2FBE.toInt())
                }
            }
        }
        btnAll.setOnClickListener {
            currentFilter = "all"
            updateButtonStyles(btnAll)
            applyFilter(etSearch.text.toString())
        }
        btnGenre.setOnClickListener {
            currentFilter = "genre"
            updateButtonStyles(btnGenre)
            applyFilter(etSearch.text.toString())
        }
        btnAuthor.setOnClickListener {
            currentFilter = "author"
            updateButtonStyles(btnAuthor)
            applyFilter(etSearch.text.toString())
        }
        etSearch.addTextChangedListener { text ->
            applyFilter(text.toString())
        }
        updateButtonStyles(btnAll)
    }

    private fun applyFilter(query: String) {
        var filtered = allBooks.filter {
            it.title.lowercase().contains(query.lowercase()) ||
                    it.author.lowercase().contains(query.lowercase())
        }
        val filterChanged = currentFilter != prevFilter
        filtered = when (currentFilter) {
            "genre" -> filtered.sortedBy { it.genre }
            "author" -> filtered.sortedBy { it.author }
            else -> filtered
        }
        adapter.updateData(filtered, resetScroll = filterChanged)
        if (filterChanged) {
            recyclerView.scrollToPosition(0)
            prevFilter = currentFilter
        }
    }
}
