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
import com.example.englishapp.adapter.ExerciseListAdapter
import com.example.englishapp.model.ExerciseList
import com.example.englishapp.model.ExerciseType
import kotlinx.coroutines.launch
class ExerciseListFragment : Fragment(R.layout.exercise_list) {

    private val vm: AdminViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // read the 3 arguments sent by ExerciseSessionFragment
        val section = requireArguments().getString("section").orEmpty()
        val sessionId = requireArguments().getInt("sessionId")
        val sessionTitle = requireArguments().getString("sessionTitle").orEmpty()

        view.findViewById<TextView>(R.id.tvSessionLocation).text = "$section • $sessionTitle"

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }
        // navigate to exercise form
        view.findViewById<View>(R.id.btnNewExercise).setOnClickListener {
            findNavController().navigate(
                R.id.action_exerciseList_to_exerciseForm,
                // the form needs section + title for its type list and breadcrumb
                bundleOf("section" to section, "sessionId" to sessionId, "sessionTitle" to sessionTitle)
            )
        }

        val rvExercises = view.findViewById<RecyclerView>(R.id.rvExercises)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyExercises)

        // fetch Exercise records then .map converts into the display model ExerciseList
        fun loadExercises() {
            viewLifecycleOwner.lifecycleScope.launch {
                // the adapter still uses your ExerciseList model, so convert
                val items = vm.repo.getExercises(sessionId).map {
                    ExerciseList(it.id, it.prompt, ExerciseType.fromKey(it.type)?.label ?: it.type, it.category)
                }

                // build a fresh adapter
                rvExercises.adapter = ExerciseListAdapter(ArrayList(items)) { exercise ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        vm.repo.deleteExercise(exercise.id)
                        loadExercises()
                    }
                }

                // show empty message if needed. then first load
                // re-run when return from the form because the view is recreated
                tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        // runs every time you come back from the form, so new exercises appear
        loadExercises()
    }
}