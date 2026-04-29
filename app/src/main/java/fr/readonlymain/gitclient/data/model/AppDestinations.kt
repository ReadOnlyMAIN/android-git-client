package fr.readonlymain.gitclient.data.model

import androidx.annotation.DrawableRes
import fr.readonlymain.gitclient.R

enum class AppDestinations(
    val label: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int
) {
    WORKSPACE(
        "Workspace",
        R.drawable.ic_filled_table_restaurant,
        R.drawable.ic_outlined_table_restaurant
    ),
    REPOSITORIES(
        "Repositories",
        R.drawable.ic_filled_inventory_2,
        R.drawable.ic_outlined_inventory_2
    ),
    SETTINGS("Settings", R.drawable.ic_filled_settings, R.drawable.ic_outlined_settings),
}