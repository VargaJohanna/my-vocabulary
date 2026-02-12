package com.vocabulary.myvocabulary.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

private var isNavigating = false
fun NavHostController.safeNavigate(
    route: Any,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    val currentEntry = this.currentBackStackEntry
    val currentState = currentEntry?.lifecycle?.currentState

    val isLifecycleReady = currentState == Lifecycle.State.RESUMED ||
            currentState == Lifecycle.State.STARTED

    if (isLifecycleReady && !isNavigating) {
        isNavigating = true

        // Add a listener to reset the flag when this screen is no longer active
        currentEntry?.lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: androidx.lifecycle.LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    isNavigating = false
                    source.lifecycle.removeObserver(this)
                }
            }
        })

        this.navigate(route, navOptionsBuilder)
        println("Nav Success: From State $currentState to Route $route")
    } else {
        println("Nav Blocked: State=$currentState, isNavigating=$isNavigating")
    }
}
