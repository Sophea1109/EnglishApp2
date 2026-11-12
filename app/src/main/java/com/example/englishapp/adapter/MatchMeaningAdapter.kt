package com.example.englishapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.R
import com.example.englishapp.model.MatchMeaning
import androidx.core.widget.doAfterTextChanged

class MatchMeaningAdapter (
    private val dataList: ArrayList<MatchMeaning>
): RecyclerView.Adapter<MatchMeaningAdapter.ViewHolderClass>() {
    class ViewHolderClass(itemView: View): RecyclerView.ViewHolder(itemView){
        val inputWord: EditText = itemView.findViewById(R.id.etPairWord)
        val pairMeaning: EditText = itemView.findViewById(R.id.etPairMeaning)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exercise_form_match_pair_item, parent, false)
        val holder = ViewHolderClass(view)
        holder.inputWord.doAfterTextChanged { text ->
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) dataList[pos].inputWord = text.toString()
        }
        holder.pairMeaning.doAfterTextChanged { text ->
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) dataList[pos].pairMeaning = text.toString()
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val match = dataList[position]
        holder.inputWord.setText(match.inputWord)
        holder.pairMeaning.setText(match.pairMeaning)
    }

    override fun getItemCount(): Int = dataList.size

    fun addPair() {
        dataList.add(MatchMeaning())
        notifyItemInserted(dataList.size - 1)
    }

    // Only the rows where both fields are filled in (use this when saving)
    fun getFilledPairs(): List<MatchMeaning> =
        dataList.filter { it.inputWord.isNotBlank() && it.pairMeaning.isNotBlank() }
}