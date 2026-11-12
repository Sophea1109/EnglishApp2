package com.example.englishapp.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.R
import com.example.englishapp.adapter.ExerciseSessionAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class ExerciseSessionFragment : Fragment(R.layout.exercise_sessions) {

    private val vm: AdminViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupSection)
        val textView = view.findViewById<TextView>(R.id.tvSectionName)
        val rvSession = view.findViewById<RecyclerView>(R.id.rvSession)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptySession)

        fun showSession(sectionName: String) {
            vm.selectedSection = sectionName
            textView.text = sectionName
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val sessions = vm.repo.getSessions(sectionName)
                    rvSession.adapter = ExerciseSessionAdapter(
                        ArrayList(sessions),
                        onSessionClick = { session -> findNavController().navigate(
                            R.id.action_sessions_to_exerciseList, bundleOf(
                                "section" to session.section,
                                "sessionId" to session.id,
                                "sessionTitle" to session.title
                            )
                        ) },
                        onDeleteClick = { session -> viewLifecycleOwner.lifecycleScope.launch {
                            try {
                                vm.repo.deleteSession(session.id)
                                showSession(sectionName)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(requireContext(), "Delete failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                            }
                        } }
                    )
                    tvEmpty.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    android.util.Log.e("Sessions", "load failed", e)
                    rvSession.adapter = null
                    tvEmpty.visibility = View.VISIBLE
                    android.widget.Toast.makeText(requireContext(), "Load failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }

        chipGroup.check(chipIdFor(vm.selectedSection))

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip: Chip? = group.findViewById(checkedIds.first())
            if (chip != null) {
                showSession(chip.text.toString())
            }
        }
// --------------------------------------------
        showSession(vm.selectedSection)
// --------------------------------------------

        view.findViewById<View>(R.id.btnNewSession).setOnClickListener {
            findNavController().navigate(R.id.action_sessions_to_sessionForm, bundleOf("section" to vm.selectedSection))
        }
    }
    private fun chipIdFor(section: String): Int = when (section) {
        "Grammar" -> R.id.chipGrammar
        "Reading" -> R.id.chipReading
        "Speaking" -> R.id.chipSpeaking
        else -> R.id.chipVocabulary
    }
}