package com.sevvanam.android_interview_architect.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe Compose Navigation destinations. Kept in :app (not a feature module) because :app
 * already depends on every feature module, and because the route names would otherwise collide
 * with the existing FeedRoute()/ProfileRoute()/CheckoutRoute() composable function names.
 */
sealed interface AppRoute {
    @Serializable
    data object Feed : AppRoute

    @Serializable
    data object Profile : AppRoute

    @Serializable
    data object Checkout : AppRoute

    @Serializable
    data object Topic : AppRoute
}
