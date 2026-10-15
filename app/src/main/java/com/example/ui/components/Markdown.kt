package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SurfaceHigh
import com.example.ui.theme.Teal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private sealed interface Block {
    data class Heading(val level: Int, val text: String) : Block
    data class Paragraph(val text: String) : Block
    data class Bullet(val marker: String, val text: String, val indent: Int) : Block
    data class Code(val text: String) : Block
    data class Quote(val text: String) : Block
}

private val bulletRegex = Regex("^(\\s*)([-*+]|\\d+[.)])\\s+(.*)$")

private fun parse(markdown: String): List<Block> {
    val blocks = mutableListOf<Block>()
    val paragraph = StringBuilder()
    fun flush() {
        if (paragraph.isNotBlank()) blocks += Block.Paragraph(paragraph.toString().trim())
        paragraph.clear()
    }
    val lines = markdown.lines()
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()
        when {
            trimmed.startsWith("```") -> {
                flush()
                val code = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    code.appendLine(lines[i]); i++
                }
                blocks += Block.Code(code.toString().trimEnd())
            }
            trimmed.isEmpty() -> flush()
            trimmed.startsWith("#") -> {
                flush()
                val level = trimmed.takeWhile { it == '#' }.length
                blocks += Block.Heading(level, trimmed.drop(level).trim())
            }
            trimmed.startsWith(">") -> {
                flush(); blocks += Block.Quote(trimmed.removePrefix(">").trim())
            }
            trimmed == "---" || trimmed == "***" -> flush()
            bulletRegex.matches(line) -> {
                flush()
                val m = bulletRegex.find(line)!!
                val marker = m.groupValues[2].let { if (it.first().isDigit()) it else "•" }
                blocks += Block.Bullet(marker, m.groupValues[3], m.groupValues[1].length / 2)
            }
            else -> {
                if (paragraph.isNotEmpty()) paragraph.append('\n')
                paragraph.append(trimmed)
            }
        }
        i++
    }
    flush()
    return blocks
}

private val inlineRegex = Regex("\\*\\*(.+?)\\*\\*|`([^`]+)`|\\[([^\\]]+)]\\(([^)\\s]+)\\)|(?<![*\\w])\\*(?!\\s)(.+?)\\*(?!\\w)")

private fun inline(text: String): AnnotatedString = buildAnnotatedString {
    var last = 0
    for (m in inlineRegex.findAll(text)) {
        append(text.substring(last, m.range.first))
        val (bold, code, linkText, url, italic) = m.destructured
        when {
            bold.isNotEmpty() -> withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(bold) }
            code.isNotEmpty() -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = SurfaceHigh)) { append(code) }
            linkText.isNotEmpty() -> withLink(
                LinkAnnotation.Url(url, TextLinkStyles(SpanStyle(color = Teal)))
            ) { append(linkText) }
            italic.isNotEmpty() -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(italic) }
        }
        last = m.range.last + 1
    }
    append(text.substring(last))
}

/** Small Markdown renderer covering what chat answers typically use. */
@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val blocks = remember(markdown) { parse(markdown) }
    SelectionContainer(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            blocks.forEach { block ->
                when (block) {
                    is Block.Heading -> Text(
                        inline(block.text),
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = when (block.level) { 1 -> 22.sp; 2 -> 19.sp; else -> 17.sp },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    is Block.Paragraph -> Text(inline(block.text), color = TextPrimary, fontSize = 16.sp, lineHeight = 25.sp)
                    is Block.Bullet -> Row(Modifier.padding(start = (block.indent * 16).dp)) {
                        Text(block.marker, color = TextSecondary, fontSize = 16.sp, modifier = Modifier.width(22.dp))
                        Text(inline(block.text), color = TextPrimary, fontSize = 16.sp, lineHeight = 25.sp)
                    }
                    is Block.Quote -> Text(
                        inline(block.text),
                        color = TextSecondary,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    is Block.Code -> Text(
                        block.text,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceHigh)
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}
