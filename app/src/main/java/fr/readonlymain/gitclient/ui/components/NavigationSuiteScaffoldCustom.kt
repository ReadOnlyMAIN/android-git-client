@file:Suppress("DEPRECATION") // Suppress for WindowWidthSizeClass

package fr.readonlymain.gitclient.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import fr.readonlymain.gitclient.data.model.AppDestinations
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Suppress("DEPRECATION") // WindowWidthSizeClass is deprecated
fun NavigationSuiteScaffoldCustom(
    currentRoute: String?,
    onChangeRoute: (String) -> Unit,
    content: @Composable () -> Unit
) {
// Custom configuration that shows a wide navigation rail in small/medium width screens, an
// expanded wide navigation rail in expanded width screens, and a short navigation bar in small
// height screens.
    /*val navSuiteType =
        with(currentWindowAdaptiveInfo()) {
            if (
                windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT ||
                windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM
            ) {
                NavigationSuiteType.WideNavigationRailCollapsed
            } else if (windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT) {
                NavigationSuiteType.ShortNavigationBarMedium
            } else if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
                //NavigationSuiteType.WideNavigationRailExpanded
                NavigationSuiteType.WideNavigationRailCollapsed
            } else {
                NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo())
            }
        }*/
    val navSuiteType =
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    val state = rememberNavigationSuiteScaffoldState()
    val scope = rememberCoroutineScope()
    val railState = rememberWideNavigationRailState()
    val railExpanded = railState.currentValue == WideNavigationRailValue.Expanded
    val isWideNavRailCollapsedType = navSuiteType == NavigationSuiteType.WideNavigationRailCollapsed
    /*val animateFAB =
        if (
            navSuiteType == NavigationSuiteType.ShortNavigationBarMedium ||
            navSuiteType == NavigationSuiteType.NavigationBar
        ) {
            Modifier.animateFloatingActionButton(
                visible = state.currentValue == NavigationSuiteScaffoldValue.Visible,
                alignment = Alignment.BottomEnd,
            )
        } else {
            Modifier
        }
    val fab =
        @Composable {
            val startPadding =
                if (navSuiteType == NavigationSuiteType.ShortNavigationBarMedium) {
                    0.dp
                } else {
                    24.dp
                }
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .padding(start = startPadding)
                    .then(animateFAB),
                onClick = { /* onClick function for FAB. */ },
                expanded =
                    if (isWideNavRailCollapsedType) railExpanded
                    else navSuiteType == NavigationSuiteType.WideNavigationRailExpanded,
                icon = { Icon(Icons.Filled.Add, "FAB") },
                text = { Text("Add new") },
            )
        }*/
    val menuButton =
        @Composable {
            IconButton(
                modifier =
                    Modifier
                        .padding(start = 24.dp, bottom = 8.dp)
                        .semantics {
                            stateDescription = if (railExpanded) "Expanded" else "Collapsed"
                        },
                onClick = { scope.launch { railState.toggle() } },
            ) {
                if (railExpanded) {
                    Icon(Icons.AutoMirrored.Filled.MenuOpen, "Collapse rail")
                } else {
                    Icon(Icons.Filled.Menu, "Expand rail")
                }
            }
        }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        NavigationSuiteScaffoldLayout(
            navigationSuiteType = navSuiteType,
            state = state,
            navigationSuite = {
                if (isWideNavRailCollapsedType || navSuiteType == NavigationSuiteType.NavigationRail) {
                    WideNavigationRail(
                        state = railState,
                        colors = WideNavigationRailDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        header = { menuButton() }
                    ) {
                        Column(modifier = Modifier.fillMaxHeight()) {
                            AppDestinations.entries.forEachIndexed { index, destination ->
                                if (index == AppDestinations.entries.size - 1) {
                                    Spacer(
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                WideNavigationRailItem(
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == destination.label) destination.selectedIcon else destination.unselectedIcon,
                                            contentDescription = destination.label
                                        )
                                    },
                                    label = { Text(destination.label) },
                                    selected = currentRoute == destination.label,
                                    onClick = {
                                        onChangeRoute(destination.label)
                                    },
                                    railExpanded = railExpanded,
                                )
                            }
                        }
                    }
                } else {
                    NavigationSuite(
                        navigationSuiteType = navSuiteType,
                    ) {
                        AppDestinations.entries.forEach { destination ->
                            NavigationSuiteItem(
                                navigationSuiteType = navSuiteType,
                                icon = {
                                    Icon(
                                        imageVector = if (currentRoute == destination.label) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.label
                                    )
                                },
                                label = { Text(destination.label) },
                                selected = currentRoute == destination.label,
                                onClick = {
                                    onChangeRoute(destination.label)
                                },
                            )
                        }
                    }
                }
            },
        ) {
            content()
        }
    }
}