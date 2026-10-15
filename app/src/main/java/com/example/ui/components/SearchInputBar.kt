package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NavioCyan
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextTertiaryDark

@Composable
fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(id = R.string.search_sessions_placeholder)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .border(width = 1.dp, color = SurfaceBorderDark, shape = RoundedCornerShape(12.dp))
            .background(color = SurfaceContainerDark, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search icon",
            tint = if (query.isNotEmpty()) NavioCyan else TextTertiaryDark,
            modifier = Modifier.size(20.dp)
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .testTag("sessions_search_box"),
            textStyle = TextStyle(
                color = TextPrimaryDark,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp
            ),
            singleLine = true,
            cursorBrush = SolidColor(NavioCyan),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {}),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = TextTertiaryDark,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("search_clear_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear search",
                    tint = TextTertiaryDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
