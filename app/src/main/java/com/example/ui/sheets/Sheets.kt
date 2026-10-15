package com.example.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Conversation
import com.example.data.NavioStore
import com.example.data.Schedule
import com.example.data.ScheduleType
import com.example.ui.components.MODEL_PRESETS
import com.example.ui.theme.Background
import com.example.ui.theme.Surface
import com.example.ui.theme.Teal
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavioSheet(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Surface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) { content() }
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(text, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(16.dp))
}

@Composable
fun HistorySheet(
    conversations: List<Conversation>,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) = NavioSheet(onDismiss) {
    SheetTitle("Sohbetler")
    if (conversations.isEmpty()) {
        Text("Henüz sohbet yok.", color = TextSecondary, modifier = Modifier.padding(vertical = 24.dp))
        return@NavioSheet
    }
    LazyColumn(Modifier.heightIn(max = 560.dp)) {
        items(conversations, key = { it.id }) { conversation ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(conversation.id) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(conversation.title, color = TextPrimary, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(conversation.updatedAt)),
                        color = TextTertiary,
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = { onDelete(conversation.id) }) {
                    Icon(Icons.Outlined.DeleteOutline, "Sil", tint = TextSecondary)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    settings: NavioStore.Settings,
    onSave: (apiKey: String, model: String) -> Unit,
    onDismiss: () -> Unit
) = NavioSheet(onDismiss) {
    var apiKey by remember { mutableStateOf(settings.apiKeyOverride) }
    var model by remember { mutableStateOf(settings.model) }
    SheetTitle("Ayarlar")
    Text("OpenRouter anahtarı kaynağı: ${settings.keySource}", color = TextSecondary, fontSize = 14.sp)
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = apiKey,
        onValueChange = { apiKey = it },
        label = { Text("API anahtarı (boş = Forge.env)") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = model,
        onValueChange = { model = it },
        label = { Text("Model (OpenRouter kimliği)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MODEL_PRESETS.forEach { (id, label) ->
            FilterChip(selected = model == id, onClick = { model = id }, label = { Text(label) }, colors = chipColors())
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        "Web araması OpenRouter'ın web eklentisiyle yapılır; istek başına küçük bir ek ücret alınır.",
        color = TextTertiary,
        fontSize = 13.sp
    )
    Spacer(Modifier.height(20.dp))
    PrimaryButton("Kaydet") {
        onSave(apiKey, model)
        onDismiss()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewAutomationSheet(
    onCreate: (name: String, prompt: String, schedule: Schedule, web: Boolean) -> Unit,
    onDismiss: () -> Unit
) = NavioSheet(onDismiss) {
    var name by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ScheduleType.DAILY) }
    var interval by remember { mutableStateOf("6") }
    var time by remember { mutableStateOf("09:00") }
    var weekday by remember { mutableStateOf(1) }
    var web by remember { mutableStateOf(true) }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        SheetTitle("Yeni otomasyon")
        OutlinedTextField(name, { name = it }, label = { Text("Ad") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            prompt,
            { prompt = it },
            label = { Text("Ne yapılsın?") },
            placeholder = { Text("Ör. Yapay zekâ sektöründeki son 24 saatin önemli haberlerini maddeler halinde özetle") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text("Zamanlama", color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                ScheduleType.HOURLY to "Saatlik",
                ScheduleType.DAILY to "Günlük",
                ScheduleType.WEEKLY to "Haftalık",
                ScheduleType.MANUAL to "Elle"
            ).forEach { (t, label) ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(label) }, colors = chipColors())
            }
        }
        Spacer(Modifier.height(12.dp))
        when (type) {
            ScheduleType.HOURLY -> OutlinedTextField(
                interval,
                { interval = it.filter(Char::isDigit).take(3) },
                label = { Text("Kaç saatte bir") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(200.dp)
            )
            ScheduleType.DAILY, ScheduleType.WEEKLY -> {
                if (type == ScheduleType.WEEKLY) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Schedule.WEEKDAYS_TR.forEachIndexed { i, day ->
                            FilterChip(
                                selected = weekday == i + 1,
                                onClick = { weekday = i + 1 },
                                label = { Text(day.take(3)) },
                                colors = chipColors()
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(
                    time,
                    { time = it.filter { c -> c.isDigit() || c == ':' }.take(5) },
                    label = { Text("Saat (SS:DD)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(200.dp)
                )
            }
            ScheduleType.MANUAL -> Text("Yalnızca \"Şimdi çalıştır\" ile çalışır.", color = TextTertiary, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Web araması kullan", color = TextPrimary, modifier = Modifier.weight(1f))
            Switch(web, { web = it }, colors = SwitchDefaults.colors(checkedTrackColor = Teal, checkedThumbColor = Background))
        }
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Otomasyonu kur", enabled = prompt.isNotBlank()) {
            val parts = time.split(":")
            val schedule = Schedule(
                type = type,
                intervalHours = interval.toIntOrNull()?.coerceIn(1, 168) ?: 1,
                hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9,
                minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0,
                weekday = weekday
            )
            onCreate(name.ifBlank { prompt.take(40) }, prompt, schedule, web)
            onDismiss()
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = TealContainer,
    selectedLabelColor = TextPrimary
)

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = TextPrimary, contentColor = Background),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium) }
}
