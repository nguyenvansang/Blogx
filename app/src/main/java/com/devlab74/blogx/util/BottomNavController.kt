package com.devlab74.blogx.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.devlab74.blogx.R
import com.google.android.material.bottomnavigation.BottomNavigationView

const val BOTTOM_NAV_BACKSTACK_KEY = "com.devlab74.blogx.util.BottomNavController.bottom_nav_backstack"

class BottomNavController(
    val context: Context,
    @IdRes val containerId: Int,
    @IdRes val appStartDestinationId: Int,
    val graphChangeListener: OnNavigationGraphChanged?,
    val navGraphProvider: NavGraphProvider
) {
    private val TAG: String = "AppDebug"
    lateinit var navigationBackStack: BackStack
    lateinit var activity: Activity
    lateinit var fragmentManager: FragmentManager
    lateinit var navItemChangeListener: OnNavigationItemChanged

    init {
        if (context is Activity) {
            activity = context
            fragmentManager = (activity as FragmentActivity).supportFragmentManager
        }
    }

    fun setupBottomNavigationBackStack(previousBackStack: BackStack?){
        navigationBackStack = previousBackStack?.let{
            it
        }?: BackStack.of(appStartDestinationId)
    }

    fun onNavigationItemSelected(itemId: Int = navigationBackStack.last()): Boolean {

        // Replace fragment representing a navigation item
        val fragment = fragmentManager.findFragmentByTag(itemId.toString())
            ?: NavHostFragment.create(navGraphProvider.getNavGraphId(itemId))
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(containerId, fragment, itemId.toString())
            .addToBackStack(null)
            .commit()

        // Add to back stack
        navigationBackStack.moveLast(itemId)

        // Update checked icon
        navItemChangeListener.onItemChanged(itemId)

        // communicate with Activity
        graphChangeListener?.onGraphChange()

        return true
    }

    @SuppressLint("RestrictedApi")
    fun onBackPressed() {
        val navController = fragmentManager.findFragmentById(containerId)!!
            .findNavController()

        when {
            navController.backStack.size > 2 ->{
                navController.popBackStack()
            }

            // Fragment back stack is empty so try to go back on the navigation stack
            navigationBackStack.size > 1 -> {

                // Remove last item from back stack
                navigationBackStack.removeLast()

                // Update the container with new fragment
                onNavigationItemSelected()
            }
            // If the stack has only one and it's not the navigation home we should
            // ensure that the application always leave from startDestination
            navigationBackStack.last() != appStartDestinationId -> {
                navigationBackStack.removeLast()
                navigationBackStack.add(0, appStartDestinationId)
                onNavigationItemSelected()
            }
            // Navigation stack is empty, so finish the activity
            else -> {
                activity.finish()
            }
        }
    }

    class BackStack : ArrayList<Int>() {
        companion object {
            fun of(vararg elements: Int): BackStack {
                val b = BackStack()
                b.addAll(elements.toTypedArray())
                return b
            }
        }

        fun removeLast() = removeAt(size - 1)

        fun moveLast(item: Int) {
            remove(item) // if present, remove
            add(item) // add to end of list
        }
    }


    // For setting the checked icon in the bottom nav
    interface OnNavigationItemChanged {
        fun onItemChanged(itemId: Int)
    }

    // Get id of each graph
    interface NavGraphProvider {
        @NavigationRes
        fun getNavGraphId(itemId: Int): Int
    }

    // Execute when Navigation Graph changes.
    interface OnNavigationGraphChanged{
        fun onGraphChange()
    }

    interface OnNavigationReselectedListener{

        fun onReselectNavItem(navController: NavController, fragment: Fragment)
    }

    fun setOnItemNavigationChanged(listener: (itemId: Int) -> Unit) {
        this.navItemChangeListener = object : OnNavigationItemChanged {
            override fun onItemChanged(itemId: Int) {
                listener.invoke(itemId)
            }
        }
    }
}

fun BottomNavigationView.setUpNavigation(
    bottomNavController: BottomNavController,
    onReselectListener: BottomNavController.OnNavigationReselectedListener
) {

    setOnNavigationItemSelectedListener {
        bottomNavController.onNavigationItemSelected(it.itemId)
    }

    setOnNavigationItemReselectedListener {
        bottomNavController
            .fragmentManager
            .findFragmentById(bottomNavController.containerId)!!
            .childFragmentManager
            .fragments[0]?.let { fragment ->

            onReselectListener.onReselectNavItem(
                bottomNavController.activity.findNavController(bottomNavController.containerId),
                fragment
            )
        }
        bottomNavController.onNavigationItemSelected()
    }

    bottomNavController.setOnItemNavigationChanged { itemId ->
        menu.findItem(itemId).isChecked = true
    }
}