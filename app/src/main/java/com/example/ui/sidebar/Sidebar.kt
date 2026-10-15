package com.example.ui.sidebar

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Conversation
import com.example.data.NavioUser
import com.example.ui.components.Avatar
import com.example.ui.theme.Outline
import com.example.ui.theme.Surface
import com.example.ui.theme.SurfaceHigh
import com.example.ui.theme.SurfaceHighest
import com.example.ui.theme.Teal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.AppMode

private enum class HistoryFilter(val label: String) { ALL("Tümü"), TODAY("Bugün"), WEEK("Bu hafta") }

/** Left panel in the style of the reference app: account, quick actions and searchable history. */
@Composable
fun Sidebar(
    user: NavioUser?,
    conversations: List<Conversation>,
    automationCount: Int,
    selectedConversationId: String?,
    mode: AppMode,
    onNewChat: () -> Unit,
    onOpenConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onOpenComputer: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }

    val now = System.currentTimeMillis()
    val visible = conversations
        .filter { query.isBlank() || it.title.contains(query.trim(), ignoreCase = true) }
        .filter {
            when (filter) {
                HistoryFilter.ALL -> true
                HistoryFilter.TODAY -> DateUtils.isToday(it.updatedAt)
                HistoryFilter.WEEK -> now - it.updatedAt < 7 * DateUtils.DAY_IN_MILLIS
            }
        }

    Column(
        modifier
            .fillMaxHeight()
            .padding(12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Surface)
            .border(1.dp, Outline, RoundedCornerShape(28.dp))
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        // Account row
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 6.dp)) {
            Avatar(user?.name ?: "Navio", size = 36)
            Spacer(Modifier.width(12.dp))
            Text(
                user?.name ?: "Navio",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            RoundIcon(if (searching) Icons.Outlined.Close else Icons.Outlined.Search, "Ara") {
                searching = !searching
                if (!searching) query = ""
            }
            Spacer(Modifier.width(8.dp))
            RoundIcon(Icons.Outlined.Settings, "Ayarlar", onClick = onOpenSettings)
        }

        if (searching) {
            Spacer(Modifier.height(14.dp))
            SearchField(query) { query = it }
        }

        Spacer(Modifier.height(18.dp))
        NavItem(Icons.Outlined.EditNote, "Yeni sohbet", selected = false, onClick = onNewChat)
        NavItem(
            Icons.Outlined.Computer,
            "Computer",
            trailing = if (automationCount > 0) "$automationCount otomasyon" else null,
            selected = mode == AppMode.COMPUTER,
            onClick = onOpenComputer
        )

        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .height(1.dp)
                .background(Outline)
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)) {
            Text("Oturumlar", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            FilterChipMenu(filter) { filter = it }
        }

        if (visible.isEmpty()) {
            Text(
                if (conversations.isEmpty()) "Henüz sohbet yok. Bir şey sorarak başla." else "Eşleşen sohbet yok.",
                color = TextTertiary,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp, top = 10.dp)
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(visible, key = { it.id }) { conversation ->
                ConversationRow(
                    conversation,
                    selected = mode == AppMode.CHAT && conversation.id == selectedConversationId,
                    onClick = { onOpenConversation(conversation.id) },
                    onDelete = { onDeleteConversation(conversation.id) }
                )
            }
        }
    }
}

@Composable
private fun RoundIcon(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(SurfaceHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceHigh)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) Text("Sohbetlerde ara", color = TextTertiary, fontSize = 15.sp)
            BasicTextField(
                value = query,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp),
                cursorBrush = SolidColor(Teal),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, selected: Boolean, trailing: String? = null, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) SurfaceHigh else Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = TextPrimary, fontSize = 17.sp, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, color = TextTertiary, fontSize = 13.sp)
    }
}

@Composable
private fun FilterChipMenu(filter: HistoryFilter, onChange: (HistoryFilter) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .clip(CircleShape)
                .border(1.dp, Outline, CircleShape)
                .clickable { open = true }
                .padding(start = 14.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(filter.label, color = TextPrimary, fontSize = 14.sp)
            Icon(Icons.Outlined.ExpandMore, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            HistoryFilter.entries.forEach {
                DropdownMenuItem(text = { Text(it.label) }, onClick = { onChange(it); open = false })
            }
        }
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, selected: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val usedComputer = conversation.messages.any { it.createdAutomationIds.isNotEmpty() }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) SurfaceHighest else Surface)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                conversation.title,
                color = TextPrimary,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    DateUtils.getRelativeTimeSpanString(conversation.updatedAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString(),
                    color = TextTertiary,
                    fontSize = 13.sp
                )
                if (usedComputer) {
                    Text("  ·  ", color = TextTertiary, fontSize = 13.sp)
                    Icon(Icons.Outlined.Schedule, null, tint = TextTertiary, modifier = Modifier.size(13.dp))
                    Text(" Computer", color = TextTertiary, fontSize = 13.sp)
                }
            }
        }
        Box {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { menu = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.MoreVert, "Seçenekler", tint = TextTertiary, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Sil") },
                    leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null) },
                    onClick = {
                        menu = false
                        onDelete()
                    }
                )
            }
        }
    }
}
