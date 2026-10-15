package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NavioCyan
import com.example.ui.theme.NavioCyanContainer
import com.example.ui.theme.SurfaceBorderDark

@Composable
fun CodeBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = NavioCyan,
    backgroundColor: Color = NavioCyanContainer.copy(alpha = 0.35f),
    borderColor: Color = color.copy(alpha = 0.5f)
) {
    Box(
        modifier = modifier
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.2.sp
        )
    }
}
