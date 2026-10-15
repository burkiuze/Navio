package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.Background
import com.example.ui.theme.Outline
import com.example.ui.theme.Surface
import com.example.ui.theme.SurfaceHigh
import com.example.ui.theme.SurfaceHighest
import com.example.ui.theme.Teal
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.AppMode

@Composable
fun NavioLogo(size: Dp, modifier: Modifier = Modifier, tint: Color = TextPrimary) {
    Image(
        painter = painterResource(R.drawable.navio_logo),
        contentDescription = "Navio",
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint),
        modifier = modifier.size(size)
    )
}

/** Centered "Chat | Computer" pill, the app's only mode switch. */
@Composable
fun ModeSwitcher(mode: AppMode, onModeChange: (AppMode) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Surface)
            .border(1.dp, Outline, CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ModeSegment("Chat", Icons.AutoMirrored.Outlined.Chat, mode == AppMode.CHAT, "mode_chat") { onModeChange(AppMode.CHAT) }
        ModeSegment("Computer", Icons.Outlined.Computer, mode == AppMode.COMPUTER, "mode_computer") { onModeChange(AppMode.COMPUTER) }
    }
}

@Composable
private fun ModeSegment(label: String, icon: ImageVector, selected: Boolean, tag: String, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) SurfaceHighest else Color.Transparent, label = "segment")
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (selected) TextPrimary else TextSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = if (selected) TextPrimary else TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

fun modelLabel(id: String, models: List<Pair<String, String>> = emptyList()): String =
    models.firstOrNull { it.first == id }?.second?.substringAfter(": ")?.removeSuffix(" (en iyi model)")
        ?: if (id == "openrouter/auto") "Auto" else id.substringAfter('/')

@Composable
fun Avatar(name: String, size: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(TealContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(name.trim().take(1).uppercase().ifBlank { "N" }, color = Teal, fontSize = (size * 0.42f).sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Bottom prompt box. [onLeading] is the "+" action; the model chip and web toggle are only shown
 * when their callbacks are provided.
 */
@Composable
fun PromptBar(
    placeholder: String,
    busy: Boolean,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    onLeading: (() -> Unit)? = null,
    model: String? = null,
    models: List<Pair<String, String>> = emptyList(),
    onModelChange: ((String) -> Unit)? = null,
    webSearch: Boolean? = null,
    onWebSearchChange: ((Boolean) -> Unit)? = null,
    accent: Boolean = false
) {
    var text by remember { mutableStateOf("") }
    val send = {
        if (text.isNotBlank() && !busy) {
            onSend(text)
            text = ""
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(if (accent) TealContainer.copy(alpha = 0.35f) else Surface)
            .border(1.dp, if (accent) Teal.copy(alpha = 0.35f) else Outline, RoundedCornerShape(28.dp))
            .padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 10.dp)
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = TextStyle(color = TextPrimary, fontSize = 17.sp, lineHeight = 24.sp),
            cursorBrush = SolidColor(Teal),
            maxLines = 8,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 28.dp)
                .testTag("prompt_input"),
            decorationBox = { inner ->
                Box {
                    if (text.isEmpty()) Text(placeholder, color = TextTertiary, fontSize = 17.sp)
                    inner()
                }
            }
        )
        Spacer(Modifier.size(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onLeading != null) {
                IconButton(onClick = onLeading) { Icon(Icons.Outlined.Add, "Yeni", tint = TextSecondary) }
            }
            if (model != null && onModelChange != null) ModelChip(model, models, onModelChange)
            Spacer(Modifier.weight(1f))
            if (webSearch != null && onWebSearchChange != null) {
                val color = if (webSearch) Teal else TextTertiary
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (webSearch) TealContainer else Color.Transparent)
                        .clickable { onWebSearchChange(!webSearch) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("web_toggle"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Language, null, tint = color, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Web", color = color, fontSize = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
            }
            val canSend = text.isNotBlank() && !busy
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (canSend) TextPrimary else SurfaceHigh)
                    .clickable(enabled = canSend, onClick = send)
                    .testTag("send_button"),
                contentAlignment = Alignment.Center
            ) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = TextSecondary)
                } else {
                    Icon(Icons.Filled.ArrowUpward, "Gönder", tint = if (canSend) Background else TextTertiary)
                }
            }
        }
    }
}

@Composable
private fun ModelChip(model: String, models: List<Pair<String, String>>, onModelChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceHigh)
                .clickable { open = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(modelLabel(model, models), color = TextPrimary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Outlined.ExpandMore, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.heightIn(max = 460.dp)) {
            models.ifEmpty { listOf(model to modelLabel(model)) }.forEach { (id, label) ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(label, color = TextPrimary)
                            Text(id, color = TextTertiary, fontSize = 12.sp)
                        }
                    },
                    onClick = {
                        onModelChange(id)
                        open = false
                    }
                )
            }
        }
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, color: Color = TextSecondary, background: Color = SurfaceHigh) {
    Text(
        text,
        color = color,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
