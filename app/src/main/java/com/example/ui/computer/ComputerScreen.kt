package com.example.ui.computer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Automation
import com.example.ui.components.MarkdownText
import com.example.ui.components.Pill
import com.example.ui.theme.Background
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.Outline
import com.example.ui.theme.Surface
import com.example.ui.theme.SurfaceHigh
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.Teal
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.ComputerReply
import java.text.DateFormat
import java.util.Date

@Composable
fun ComputerScreen(
    automations: List<Automation>,
    reply: ComputerReply?,
    onDismissReply: () -> Unit,
    onNewAutomation: () -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onRunNow: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (reply != null) item(key = "reply") { ReplyCard(reply, onDismissReply) }
        if (automations.isEmpty()) {
            item(key = "empty") { EmptyComputer() }
        } else {
            item(key = "header") {
                Text(
                    "Otomasyonlar · ${automations.size}",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
            items(automations, key = { it.id }) { automation ->
                AutomationCard(
                    automation,
                    onToggle = { onToggle(automation.id, it) },
                    onRunNow = { onRunNow(automation.id) },
                    onDelete = { onDelete(automation.id) }
                )
            }
        }
        item(key = "use") { UseComputerCard(onNewAutomation) }
    }
}

@Composable
private fun EmptyComputer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(TealContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Computer, null, tint = Teal, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Henüz otomasyon yok", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Aşağıya ne yapılmasını istediğini yaz ya da Chat'te iste:\n\"Her sabah 9'da sektör haberlerini özetle\"",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun UseComputerCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceHigh)
                .border(1.dp, Teal.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Computer, null, tint = TextPrimary)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text("Computer'ı kullan", color = TextPrimary, fontSize = 17.sp)
            Text("Herhangi bir işi zamanlayıp devredin", color = TextSecondary, fontSize = 14.sp)
        }
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.dp, Outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Add, "Yeni otomasyon", tint = TextPrimary)
        }
    }
}

@Composable
private fun ReplyCard(reply: ComputerReply, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (reply.isError) ErrorRed.copy(alpha = 0.12f) else TealContainer)
            .padding(start = 18.dp, top = 8.dp, bottom = 16.dp, end = 4.dp)
    ) {
        Column(Modifier.weight(1f).padding(top = 8.dp)) {
            if (reply.isError) Text(reply.text, color = ErrorRed, fontSize = 15.sp) else MarkdownText(reply.text)
        }
        IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "Kapat", tint = TextSecondary) }
    }
}

@Composable
private fun AutomationCard(
    automation: Automation,
    onToggle: (Boolean) -> Unit,
    onRunNow: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val lastRun = automation.lastRun
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, Outline, RoundedCornerShape(20.dp))
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(automation.name, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = TextTertiary, modifier = Modifier.size(15.dp))
                    Text(automation.schedule.describe(), color = TextSecondary, fontSize = 13.sp)
                    if (automation.useWebSearch) {
                        Icon(Icons.Outlined.Language, null, tint = TextTertiary, modifier = Modifier.size(15.dp))
                        Text("Web", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
            Switch(
                checked = automation.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedTrackColor = Teal, checkedThumbColor = Background)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            automation.prompt,
            color = TextSecondary,
            fontSize = 14.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis
        )
        if (lastRun != null) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(
                    if (lastRun.success) "Son çalışma" else "Hata",
                    color = if (lastRun.success) SuccessGreen else ErrorRed
                )
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(lastRun.startedAt)),
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            if (expanded) {
                MarkdownText(lastRun.output)
                if (lastRun.sources.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        lastRun.sources.joinToString("\n") { "• ${it.title}" },
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            } else {
                Text(
                    lastRun.output.replace(Regex("[#*`>]"), "").trim(),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onRunNow) {
                Icon(Icons.Outlined.PlayArrow, null, tint = Teal, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Şimdi çalıştır", color = Teal)
            }
            TextButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Sil", color = TextSecondary)
            }
        }
    }
}
