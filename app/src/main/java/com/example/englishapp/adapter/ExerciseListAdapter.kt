package com.example.englishapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.model.ExerciseList
import com.example.englishapp.R

class ExerciseListAdapter (
    private val dataList: ArrayList<ExerciseList>,
    private val onDeleteClick: (ExerciseList) -> Unit
): RecyclerView.Adapter<ExerciseListAdapter.ViewHolderClass>() {
    class ViewHolderClass(itemView: View): RecyclerView.ViewHolder(itemView) {
        val exercisePrompt: TextView = itemView.findViewById(R.id.tvExercisePrompt)
        val type: TextView = itemView.findViewById(R.id.tvExerciseType)
        val category: TextView = itemView.findViewById(R.id.tvExerciseCategory)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteExercise)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderClass {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_item, parent, false)
        return ViewHolderClass(view)
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val exercise = dataList[position]
        holder.exercisePrompt.text = exercise.exercisePrompt
        holder.type.text = exercise.type
        holder.category.text = exercise.category
        holder.btnDelete.setOnClickListener { onDeleteClick(exercise) }
    }

    override fun getItemCount(): Int = dataList.size
}