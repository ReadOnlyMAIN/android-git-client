package fr.readonlymain.gitclient.ui.navigation

sealed class Route(val route: String) {
    object Workspace : Route("Workspace")
    object Repositories : Route("Repositories")
    object Settings : Route("Settings")
}