package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SESSIONS(
        title = "Sessions",
        selectedIcon = Icons.Filled.Terminal,
        unselectedIcon = Icons.Outlined.Terminal,
        testTag = "nav_tab_sessions"
    ),
    MODELS(
        title = "Models",
        selectedIcon = Icons.Filled.Hub,
        unselectedIcon = Icons.Outlined.Hub,
        testTag = "nav_tab_models"
    ),
    SETTINGS(
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_tab_settings"
    )
}
