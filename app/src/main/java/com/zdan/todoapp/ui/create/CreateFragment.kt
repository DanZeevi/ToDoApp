package com.zdan.todoapp.ui.create

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zdan.todoapp.R
import com.zdan.todoapp.databinding.FragmentCreateBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CreateFragment: Fragment(R.layout.fragment_create) {

    private val viewModel: CreateViewModel by viewModels()

    private var _binding: FragmentCreateBinding? = null
    private val binding: FragmentCreateBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCreateBinding.bind(view)

        setViews()

        setObservers()

    }

    private fun setObservers() {
        viewModel.toastLiveData.observe(viewLifecycleOwner) { toastMessage ->
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setViews() {
        binding.apply {
            buttonCreate.setOnClickListener {
                createTodo()
            }
        }
    }

    private fun createTodo() {
        binding.apply {
            val description: String? = editTextDescription.editableText?.toString()
            val isImportant: Boolean = checkBoxImportant.isChecked
            viewModel.createTodo(description, isImportant) { callBack ->
                if (callBack) { // success
                    // navigate back to list fragment
                    val action = CreateFragmentDirections.actionCreateFragmentToListFragment()
                    findNavController().navigate(action)
                } else { // failure
                    Timber.d("Error creating todo item")
                }
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}