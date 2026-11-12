package com.example.englishapp

import android.app.ActionBar
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<View>(android.R.id.content)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

//  activity_main.xml
        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment // search the fragment in the container
        val navController = navHost.navController
        val tabLayout = findViewById<TabLayout>(R.id.myTabLayout)
        val adminHeader = findViewById<View>(R.id.adminHeader)

        // Tapping a tab switches between the two top-level screens
        // position = 0 = user tab, decided in nav_graph startDestination (@id/userListFragment)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // use as a safe call in case the second if returns null
                val destination =
                    if (tab.position == 0) R.id.userListFragment else R.id.exerciseSessionsFragment
                if (navController.currentDestination?.id == destination) return

                navController.navigate(destination, null, navOptions {
                    popUpTo(navController.graph.id) { inclusive = true }  // clear the back stack
                })
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Show the Admin header + tabs only on the two top-level screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val topLevel = destination.id == R.id.userListFragment ||
                    destination.id == R.id.exerciseSessionsFragment
            adminHeader.visibility = if (topLevel) View.VISIBLE else View.GONE
        }
    }
}