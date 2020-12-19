package com.zdan.todoapp.ui.list

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.zdan.todoapp.R
import com.zdan.todoapp.data.Todo
import com.zdan.todoapp.databinding.FragmentListBinding
import com.zdan.todoapp.util.SwipeToDeleteCallback
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var todoListAdapter: TodoListAdapter

    private lateinit var actionModeCallback: ActionMode.Callback
    private var actionMode: ActionMode? = null

    private val viewModel: ListFragmentViewModel by viewModels()

    private var _binding: FragmentListBinding? = null
    private val binding: FragmentListBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // view binding
        _binding = FragmentListBinding.bind(view)

        setViews()
        setObservers()
        setHasOptionsMenu(true)
        setActionModeCallback()

        loadPrefrences()
    }

    private fun loadPrefrences() {
        val sharedPref = activity?.getPreferences(MODE_PRIVATE ) ?: return
        val importantFirst = sharedPref.getBoolean(getString(R.string.shared_pref_key_important_first), true)
        val showCompleted = sharedPref.getBoolean(getString(R.string.shared_pref_key_show_completed), true)
        val sortDsc = sharedPref.getBoolean(getString(R.string.shared_pref_key_sort_dsc), true)
        viewModel.setPreferences(importantFirst, showCompleted, sortDsc)
    }

    private fun setObservers() {
        // observe todoList
        viewModel.apply {
            // list to update in list adapter
            todoList.observe(viewLifecycleOwner) { todoList ->
                todoListAdapter.submitList(todoList)
            }

            // item to delete or undo delete
            itemPendingDelete.observe(viewLifecycleOwner) { itemToDelete ->
                showSnackBarUndo(itemToDelete)
            }
            // item selected to highlight and handle action mode
            itemSelected.observe(viewLifecycleOwner) { itemSelected ->
                Timber.d("item selected: $itemSelected")
                if (itemSelected == null) {
                    // close action mode menu
                    actionMode?.finish()
                } else {
                    todoListAdapter.selectItem(itemSelected)
                    enterActionMode()
                }
            }
            itemDeselected.observe(viewLifecycleOwner) { item ->
                Timber.d("item deselected: $item")
                item?.let {
                    todoListAdapter.deselectItem(item)
                }
            }
        }
    }

    private fun showSnackBarUndo(itemToDelete: Todo) {
        Snackbar.make(
            binding.root,
            getString(R.string.snackbacr_delete_item, itemToDelete.description),
            Snackbar.LENGTH_LONG)
            .setAction(R.string.snackbar_undo) {
                viewModel.addTodo(itemToDelete)
            }
            .show()
    }

    private fun setViews() {
        // init list adapter
        todoListAdapter = TodoListAdapter(
            onItemUpdated = { item ->
                viewModel.updateItem(item)
            },
            onLongClick = { item -> viewModel.setItemSelected(item) }
        )

        binding.apply {
            // recycler view
            setRecyclerView(recyclerViewTodos)
            // swipe callback
            setSwipeCallback(recyclerViewTodos)
            // Floating action button
            fabAddItem.setOnClickListener {
                // navigate to create fragment
                actionMode?.finish()
                val action = ListFragmentDirections.actionListFragmentToCreateFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun setRecyclerView(recyclerViewTodos: RecyclerView) {
        recyclerViewTodos.apply {
            // layout manager
            layoutManager = LinearLayoutManager(context)
            // adapter
            adapter = todoListAdapter
            // item animator
            itemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(
                    viewHolder: RecyclerView.ViewHolder,
                    payloads: MutableList<Any>,
                ) = false

                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder) = false
            }
        }
    }

    private fun setSwipeCallback(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDeleteCallback(recyclerView.context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                viewModel.onItemSwiped(position)
            }
        }
        ItemTouchHelper(swipeToDeleteCallback)
            .attachToRecyclerView(recyclerView)
    }

    private fun goToUpdate(item: Todo) {
        Timber.d("update")
        actionMode?.finish()
        val action = ListFragmentDirections.actionListFragmentToUpdateFragment(item.uid)
        findNavController().navigate(action)
    }

    private fun enterActionMode() {
        // enable action mode
        actionMode = activity?.startActionMode(actionModeCallback)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_list, menu)
        setListMenu(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_list_sort -> {
                // switch sorting of list asc/dsc
                viewModel.switchSort()
                true
            }
            R.id.menu_list_checkbox_show_completed -> {
                // hide/show completed
                viewModel.switchShowCompleted()
                true
            }
            R.id.menu_list_checkbox_important_first -> {
                // sort by important first
                viewModel.switchImportantFirst()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setActionModeCallback() {
        actionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.menuInflater.inflate(R.menu.menu_edit, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem) =
                when (item.itemId) {
                    R.id.menu_edit_delete -> {
                        viewModel.deleteSelected()
                        mode.finish()
                        true
                    }
                    R.id.menu_edit_update -> {
                        viewModel.itemSelected.value?.let {
                            goToUpdate(it)
                        }
                        true
                    }
                    else -> false
                }

            override fun onDestroyActionMode(mode: ActionMode) {
                viewModel.clearSelection()
                actionMode = null
            }
        }
    }


    private fun setListMenu(menu: Menu) {
        // observe live data to update check box
        // show completed
        viewModel.showCompletedLiveData.observe(viewLifecycleOwner) { show ->
            menu.findItem(R.id.menu_list_checkbox_show_completed).isChecked = show
        }
        // important first
        viewModel.importantFirstLiveData.observe(viewLifecycleOwner) { boolean ->
            menu.findItem(R.id.menu_list_checkbox_important_first).isChecked = boolean
        }
        // set search action
        val searchItem = menu.findItem(R.id.menu_list_search)
        Timber.d("searchItem: $searchItem")
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // close keyboard
                searchView.clearFocus()
                // close search view
                searchItem.collapseActionView()
                // reset recycler view position
                binding.recyclerViewTodos.scrollToPosition(0)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText)
                Timber.d("query: $newText")
                return true
            }
        })
    }

    override fun onStop() {
        super.onStop()
        // save settings in shared preferences
        savePreferences()
    }

    private fun savePreferences() {
        val showCompletedPref = activity?.getPreferences(MODE_PRIVATE) ?: return
        with (showCompletedPref.edit()) {
            // show completed
            viewModel.showCompletedLiveData.value?.let { showCompleted ->
                putBoolean(getString(R.string.shared_pref_key_show_completed), showCompleted)
            }
            // important first
            viewModel.importantFirstLiveData.value?.let { isImportant ->
                putBoolean(getString(R.string.shared_pref_key_important_first), isImportant)
            }
            // sort descending (new to old)
            viewModel.sortDscLiveData.value?.let { sortDsc ->
                putBoolean(getString(R.string.shared_pref_key_sort_dsc), sortDsc)
            }
            apply()
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}