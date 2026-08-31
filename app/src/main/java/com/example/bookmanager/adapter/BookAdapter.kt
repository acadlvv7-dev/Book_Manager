package com.example.bookmanager.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmanager.R
import com.example.bookmanager.data.Book

class BookAdapter(
    private var bookList: List<Book>,
    private val showCheckBox: Boolean = false,
    private val onFavoriteClick: (Book) -> Unit = {},
    private val onCheckChanged: (Book, Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private val selectedBooks = mutableSetOf<Int>()

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val author: TextView = itemView.findViewById(R.id.tvAuthor)
        val genre: TextView = itemView.findViewById(R.id.tvGenre)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.imgFavorite)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBook)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.title.text = book.title
        holder.author.text = book.author
        holder.genre.text = book.groupName ?: book.genre

        if (showCheckBox) {
            holder.checkBox.visibility = View.VISIBLE
        } else {
            holder.checkBox.visibility = View.GONE
        }

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selectedBooks.contains(book.id)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedBooks.add(book.id) else selectedBooks.remove(book.id)
            onCheckChanged(book, isChecked)
        }

        if (book.isFavorite) {
            holder.favoriteIcon.setImageResource(R.drawable.ic_like_on)
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_like)
        }

        holder.favoriteIcon.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(it, "scaleX", 1f, 1.4f, 1f)
            val scaleY = ObjectAnimator.ofFloat(it, "scaleY", 1f, 1.4f, 1f)
            val anim = AnimatorSet()
            anim.playTogether(scaleX, scaleY)
            anim.duration = 300
            anim.start()
            onFavoriteClick(book)
        }
    }

    override fun getItemCount() = bookList.size

    fun updateData(newList: List<Book>, resetScroll: Boolean = false) {
        val validIds = newList.map { it.id }.toSet()
        selectedBooks.retainAll(validIds)
        if (resetScroll) {
            bookList = newList
            notifyDataSetChanged()
        } else {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = bookList.size
                override fun getNewListSize() = newList.size
                override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                    bookList[oldPos].id == newList[newPos].id
                override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                    bookList[oldPos] == newList[newPos]
            })
            bookList = newList
            diff.dispatchUpdatesTo(this)
        }
    }

    fun getSelectedBooks(): List<Book> {
        return bookList.filter { selectedBooks.contains(it.id) }
    }

    fun clearSelection() {
        selectedBooks.clear()
        notifyDataSetChanged()
    }
}
