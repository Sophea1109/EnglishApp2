package com.example.englishapp.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.example.englishapp.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ExerciseSessionFormFragment : Fragment(R.layout.exercise_session_form) {
    private val vm: AdminViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // sent by ExerciseSessionFragment: which tab's session are we creating?
        val section = requireArguments().getString("section").orEmpty()

        val etTitle = view.findViewById<TextInputEditText>(R.id.etSessionTitle)
        val etOrder = view.findViewById<TextInputEditText>(R.id.etSessionOrder)
        val tvError = view.findViewById<TextView>(R.id.tvSessionError)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveSession)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        fun showError(message: String) {
            tvError.text = message
            tvError.visibility = View.VISIBLE
        }

        // hide the error as soon as the admin edits something
        etTitle.doAfterTextChanged { tvError.visibility = View.GONE }
        etOrder.doAfterTextChanged { tvError.visibility = View.GONE }

        // suggest the next order number (same as the prototype)
        viewLifecycleOwner.lifecycleScope.launch {
            etOrder.setText((vm.repo.getSessions(section).size + 1).toString())
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val order = etOrder.text.toString().toIntOrNull()

            when {
                title.isEmpty() -> showError("Enter a session title.")
                order == null -> showError("Enter a display order (a number).")
                else -> {
                    btnSave.isEnabled = false          // stop double taps
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            vm.repo.addSession(section, title, order)
                            findNavController().navigateUp()   // back to the list; it reloads itself
                        } catch (e: Exception) {
                            // will matter once Supabase is used (no internet, rejected insert...)
                            showError("Could not save: ${e.message}")
                            btnSave.isEnabled = true
                        }
                    }
                }
            }
        }
    }
}