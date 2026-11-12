package com.example.englishapp.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.R
import com.example.englishapp.adapter.UserListAdapter
import com.example.englishapp.model.UserList

class UserListFragment : Fragment(R.layout.user_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val users = arrayListOf(
            UserList(1, "Dave", 68, "struggling", "active"),
            UserList(2, "Amy", 10, "on track", "active"),
            UserList(3, "James", 50, "struggling", "2hr ago")
        )



        view.findViewById<RecyclerView>(R.id.rvUsers).adapter = UserListAdapter(users) { user ->
            findNavController().navigate(
                R.id.action_userList_to_userDetails,
                bundleOf("userId" to user.id)
            )
        }
    }
}