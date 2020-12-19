package com.zdan.todoapp.ui.update

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.zdan.todoapp.R
import com.zdan.todoapp.data.Todo
import com.zdan.todoapp.databinding.FragmentUpdateBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class UpdateFragment : Fragment(R.layout.fragment_update) {

    private val viewModel: UpdateViewModel by viewModels()

    private val args: UpdateFragmentArgs by navArgs()

    private var _binding: FragmentUpdateBinding? = null
    private val binding: FragmentUpdateBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get item by view model
        viewModel.getItem(args.uid)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentUpdateBinding.bind(view)

        setObservers()

        setHasOptionsMenu(true)
    }

    private fun setObservers() {
        viewModel.item.observe(viewLifecycleOwner) { item ->
            item?.let {
                setViews(it)
            }
        }

        viewModel.toastLiveData.observe(viewLifecycleOwner) { toastMessage ->
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setViews(item: Todo) {
        binding.apply {
            editTextDescription.hint = item.description
            editTextDescription.text = Editable.Factory.getInstance().newEditable(item.description)
            checkBoxImportant.isChecked = item.isImportant
        }
    }

    private fun updateTodo() {
        binding.apply {
            // close keyboard
            editTextDescription.clearFocus()
            // get fields
            val description = editTextDescription.editableText?.toString()
            val isImportant: Boolean = checkBoxImportant.isChecked
            // update by view model
            viewModel.updateTodo(description, isImportant) { callBack ->
                if (callBack) { // success
                    // navigate back to list fragment
                    val action = UpdateFragmentDirections.actionUpdateFragmentToListFragment()
                    findNavController().navigate(action)
                } else { // failure
                    Timber.d("Error updating todo item")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_update, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_update_save -> {
                updateTodo()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}