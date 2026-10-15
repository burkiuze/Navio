package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Automation
import com.example.data.ChatMessage
import com.example.data.Conversation
import com.example.data.Role
import com.example.data.Source
import com.example.ui.components.MarkdownText
import com.example.ui.components.NavioLogo
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.Outline
import com.example.ui.theme.Surface
import com.example.ui.theme.SurfaceHigh
import com.example.ui.theme.Teal
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

private val SUGGESTIONS = listOf(
    "Bugünün teknoloji gündemini özetle",
    "Her sabah 09:00'da sektör haberlerini bana özetleyen bir otomasyon kur",
    "Her cuma 18:00'de haftalık kripto piyasası raporu hazırla",
    "İstanbul'da bu hafta sonu ne yapılır?"
)

@Composable
fun ChatScreen(
    conversation: Conversation?,
    automations: List<Automation>,
    busy: Boolean,
    webSearch: Boolean,
    onSuggestion: (String) -> Unit,
    onOpenAutomation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages = conversation?.messages.orEmpty()
    if (messages.isEmpty() && !busy) {
        EmptyChat(onSuggestion, modifier)
        return
    }
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, busy) {
        val count = messages.size + if (busy) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            if (message.role == Role.USER) UserMessage(message) else AssistantMessage(message, automations, onOpenAutomation)
        }
        if (busy) item(key = "thinking") { Thinking(webSearch) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyChat(onSuggestion: (String) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NavioLogo(36.dp, tint = TextSecondary)
        Spacer(Modifier.height(20.dp))
        Text("Navio", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Sor, araştır ya da tek cümleyle otomasyon kur.",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.widthIn(max = 640.dp)
        ) {
            SUGGESTIONS.forEach { text ->
                Text(
                    text,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Outline, RoundedCornerShape(16.dp))
                        .clickable { onSuggestion(text) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun UserMessage(message: ChatMessage) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Text(
            message.content,
            color = TextPrimary,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            modifier = Modifier
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(22.dp, 22.dp, 6.dp, 22.dp))
                .background(SurfaceHigh)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun AssistantMessage(message: ChatMessage, automations: List<Automation>, onOpenAutomation: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (message.sources.isNotEmpty()) Sources(message.sources)
        Row(verticalAlignment = Alignment.Top) {
            if (message.isError) {
                Icon(Icons.Outlined.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(22.dp))
                Spacer(Modifier.size(10.dp))
                Text(message.content, color = ErrorRed, fontSize = 15.sp)
            } else {
                MarkdownText(message.content, Modifier.fillMaxWidth())
            }
        }
        message.createdAutomationIds.mapNotNull { id -> automations.firstOrNull { it.id == id } }.forEach {
            AutomationCreatedCard(it, onOpenAutomation)
        }
    }
}

@Composable
private fun Sources(sources: List<Source>) {
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Link, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(6.dp))
            Text("Kaynaklar", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sources.withIndex().toList()) { (index, source) ->
                Column(
                    modifier = Modifier
                        .widthIn(min = 150.dp, max = 220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface)
                        .border(1.dp, Outline, RoundedCornerShape(14.dp))
                        .clickable { runCatching { uriHandler.openUri(source.url) } }
                        .padding(12.dp)
                ) {
                    Text(source.title, color = TextPrimary, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${index + 1} · ${source.url.removePrefix("https://").removePrefix("www.").substringBefore('/')}",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AutomationCreatedCard(automation: Automation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(TealContainer)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Teal.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.AutoAwesome, null, tint = Teal, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Otomasyon kuruldu: ${automation.name}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(automation.schedule.describe() + " · Computer'da gör", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun Thinking(webSearch: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Teal)
        Spacer(Modifier.size(12.dp))
        Text(if (webSearch) "Web'de aranıyor ve düşünülüyor…" else "Düşünülüyor…", color = TextSecondary, fontSize = 15.sp)
    }
}
