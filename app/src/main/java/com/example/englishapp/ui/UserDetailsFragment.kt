package com.example.englishapp.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.englishapp.R
import com.google.android.material.appbar.MaterialToolbar

// load the layout, replace setContentView
class UserDetailsFragment : Fragment(R.layout.user_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//  run when the layout exist
        super.onViewCreated(view, savedInstanceState)

        val userId = requireArguments().getInt("userId")
        // later: load this user from Supabase using userId

//  tool_bar: back arrow.
//  view is the layout parameter, so you search inside it
        view.findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { findNavController().navigateUp() }
    }
}