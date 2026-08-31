package com.example.bookmanager.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmanager.AppDatabase
import com.example.bookmanager.R
import com.example.bookmanager.adapter.BookAdapter
import com.example.bookmanager.data.Book

class SecondScreenFragment : Fragment(R.layout.fragment_second_screen) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var db: AppDatabase
    private var allFavorites = listOf<Book>()
    private var currentTab = "all"
    private var groups = mutableListOf<String>()
    private lateinit var tabContainer: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getInstance(requireContext())

        recyclerView = view.findViewById(R.id.recyclerView_favorites)
        tabContainer = view.findViewById(R.id.tabContainer)
        val menuIcon = view.findViewById<ImageView>(R.id.favroites_menu)

        adapter = BookAdapter(
            emptyList(),
            showCheckBox = false,
            onFavoriteClick = { book ->
                Thread {
                    db.bookDao().updateBook(book.copy(isFavorite = !book.isFavorite))
                }.start()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        db.bookDao().getFavoriteBooks().observe(viewLifecycleOwner) { books ->
            allFavorites = books
            groups = books.mapNotNull { it.groupName }.distinct().toMutableList()
            refreshTabs()
            applyTab()
        }

        menuIcon.setOnClickListener {
            showMenuPopup(it)
        }
    }

    private fun refreshTabs() {
        tabContainer.removeAllViews()
        val tabNames = mutableListOf("All", "Ungrouped") + groups

        tabNames.forEach { name ->
            val tv = TextView(requireContext())
            tv.text = name
            tv.textSize = 14f
            tv.setPadding(24, 12, 24, 12)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = 4
            tv.layoutParams = params

            val tabKey = when (name) {
                "All" -> "all"
                "Ungrouped" -> "ungrouped"
                else -> name
            }

            updateTabStyle(tv, tabKey == currentTab)

            tv.setOnClickListener {
                currentTab = tabKey
                refreshTabs()
                applyTab()
            }

            tabContainer.addView(tv)
        }
    }

    private fun updateTabStyle(tv: TextView, isActive: Boolean) {
        if (isActive) {
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tv.setBackgroundResource(R.drawable.tab_active_underline)
        } else {
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tv.background = null
        }
    }

    private fun applyTab() {
        val filtered = when (currentTab) {
            "all" -> allFavorites
            "ungrouped" -> allFavorites.filter { it.groupName == null || it.groupName!!.isEmpty() }
            else -> allFavorites.filter { it.groupName == currentTab }
        }
        adapter.updateData(filtered)
    }

    private fun showMenuPopup(anchor: View) {
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, "New group")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> showCreateGroupDialog()
            }
            true
        }
        popup.show()
    }

    private fun showCreateGroupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_group, null)
        val etGroupName = dialogView.findViewById<EditText>(R.id.etGroupName)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Create new group")
            .setPositiveButton("Create") { _, _ ->
                val name = etGroupName.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (!groups.contains(name)) {
                        groups.add(name)
                    }
                    showAddBooksToGroupDialog(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddBooksToGroupDialog(groupName: String) {
        val ungrouped = allFavorites.filter { it.groupName == null || it.groupName!!.isEmpty() }
        if (ungrouped.isEmpty()) {
            refreshTabs()
            return
        }

        val titles = ungrouped.map { it.title }.toTypedArray()
        val checked = BooleanArray(ungrouped.size) { false }

        AlertDialog.Builder(requireContext())
            .setTitle("Add books to \"$groupName\"")
            .setMultiChoiceItems(titles, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("Add") { _, _ ->
                Thread {
                    ungrouped.forEachIndexed { i, book ->
                        if (checked[i]) {
                            db.bookDao().updateBook(book.copy(groupName = groupName))
                        }
                    }
                }.start()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
