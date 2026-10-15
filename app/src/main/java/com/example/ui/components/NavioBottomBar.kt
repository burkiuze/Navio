package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.NavTab
import com.example.ui.theme.NavioCyan
import com.example.ui.theme.NavioCyanContainer
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextTertiaryDark

@Composable
fun NavioBottomBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets.navigationBars,
        containerColor = SurfaceDark,
        tonalElevation = 4.dp
    ) {
        NavTab.values().forEach { tab ->
            val isSelected = tab == currentTab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavioCyan,
                    selectedTextColor = NavioCyan,
                    indicatorColor = NavioCyanContainer.copy(alpha = 0.5f),
                    unselectedIconColor = TextTertiaryDark,
                    unselectedTextColor = TextTertiaryDark
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
