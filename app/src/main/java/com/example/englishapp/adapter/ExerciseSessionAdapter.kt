package com.example.englishapp.adapter
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.model.ExerciseSession
import com.example.englishapp.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton

class ExerciseSessionAdapter(
    private val dataList: ArrayList<ExerciseSession>,
    private val onSessionClick: (ExerciseSession) -> Unit,
    private val onDeleteClick: (ExerciseSession) -> Unit,
): RecyclerView.Adapter<ExerciseSessionAdapter.ViewHolderClass>() {
    class ViewHolderClass(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvSessionTitle)
        val sessionOrder: TextView = itemView.findViewById(R.id.tvSessionOrder)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteSession)
    }

    // build one empty row. RecyclerView extends to ViewGroup (any view that contain other views like linearlayout)
    // false: only build the row, don't add the row yet
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_session_item, parent, false)
        return ViewHolderClass(view)
    }

    // fill the row with data
    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val unit = dataList[position]
        holder.title.text = unit.title
        holder.sessionOrder.text = "Order ${unit.sessionOrder}"
        holder.itemView.setOnClickListener { onSessionClick(unit) }
        holder.btnDelete.setOnClickListener { onDeleteClick(unit) }
    }

    override fun getItemCount(): Int = dataList.size
}