package com.example.englishapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.model.UserList
import com.example.englishapp.R
import com.google.android.material.color.MaterialColors

class UserListAdapter(
    private val dataList: ArrayList<UserList>,
    private val onItemClick: (UserList) -> Unit = {}   // used later to open user details
) : RecyclerView.Adapter<UserListAdapter.ViewHolderClass>() {

    // Holds the views of ONE row, so they're only looked up once
    class ViewHolderClass(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val progress: TextView = itemView.findViewById(R.id.progress)
        val status: TextView = itemView.findViewById(R.id.status)
        val lastActive: TextView = itemView.findViewById(R.id.tvLastActive)
    }

    // 1. Creates a row from item_user.xml (called only a few times, then rows get reused)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return ViewHolderClass(view)
    }

    // 2. Fills a row with the data at this position (called every time a row appears)
    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val user = dataList[position] // row number

        holder.name.text = user.name
        holder.progress.text = "${user.progress}%"

        // Status pill: red if struggling, green otherwise
        holder.status.text = user.status
        holder.status.setTextColor(Color.WHITE)
        holder.status.setBackgroundResource(
            if (user.status == "struggling") R.drawable.bg_pill_red
            else R.drawable.bg_pill_green
        )

        // Last active: green pill only when "active", plain text otherwise
        holder.lastActive.text = user.lastActive
        if (user.lastActive == "active") {
            holder.lastActive.setBackgroundResource(R.drawable.bg_pill_green)
            holder.lastActive.setTextColor(Color.WHITE)
        } else {
            holder.lastActive.background = null
            holder.lastActive.setTextColor(
                MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnSurface)
            )
        }

        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    // 3. How many rows there are
    override fun getItemCount(): Int = dataList.size
}