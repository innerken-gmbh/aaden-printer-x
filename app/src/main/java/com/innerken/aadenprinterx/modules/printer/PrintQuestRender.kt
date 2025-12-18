package com.innerken.aadenprinterx.modules.printer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.innerken.aadenprinterx.modules.printer.RenderFlags.BOLD
import com.innerken.aadenprinterx.modules.printer.RenderFlags.DOUBLE_HEIGHT
import com.innerken.aadenprinterx.modules.printer.RenderFlags.DOUBLE_WIDTH
import com.innerken.aadenprinterx.modules.printer.RenderFlags.IS_IMG
import com.innerken.aadenprinterx.modules.printer.RenderFlags.LINE
import com.innerken.aadenprinterx.modules.printer.RenderFlags.LR_RENDER
import com.innerken.aadenprinterx.modules.printer.RenderFlags.NOT_SUPPORT
import com.innerken.aadenprinterx.modules.printer.RenderFlags.REVERSE_COLOR
import org.w3c.dom.Node
import com.innerken.aadenprinterx.modules.printer.RenderFlags.hasFlag

//单据预览UI
@Composable
fun PrintQuestRender(element: ExpandNode) {
    if (element.node.childNodes.length > 0) {
        Column(modifier = Modifier.width(390.dp)) {
            if (element.node.nodeName == "NODE") {
                Row(modifier = Modifier.background(Color.Blue)) {
                    for (i in 0 until element.node.childNodes.length) {
                        PrintQuestRender(
                            element = ExpandNode(
                                element.node.childNodes.item(i),
                                element.parentPath.toMutableList()
                                    .apply { add(element.node.nodeName) })
                        )
                    }
                }
            } else {
                for (i in 0 until element.node.childNodes.length) {
                    PrintQuestRender(
                        element = ExpandNode(
                            element.node.childNodes.item(i),
                            element.parentPath.toMutableList().apply { add(element.node.nodeName) })
                    )
                }
            }

        }
    } else {
        if (element.node.textContent != "\n") {
            val text = element.node.textContent
            PrintRenderText(text = text, element.parentPath.fold(0) { acc, s: String ->
                acc or RenderFlags.labelToFlag(s) and RenderMasks.labelToMask(s)
            })
        }

    }
}

@Composable
fun PrintRenderText(text: String, renderFlag: Int = 0) {
    var modifier: Modifier = Modifier
    var align = TextAlign.Start
    var fontSize = 14.sp
    var fontWeight = FontWeight.Normal
    var backgroundColor = Color.White
    var frontColor = Color.Black
    var tempText = text
    if (renderFlag.hasFlag(RenderFlags.CENTER)) {
        modifier = Modifier.fillMaxWidth()
        align = TextAlign.Center
    }
    if (renderFlag.hasFlag(DOUBLE_WIDTH) or renderFlag.hasFlag(DOUBLE_HEIGHT)) {
        fontSize = 20.sp
    }
    if (renderFlag.hasFlag(BOLD)) {
        fontWeight = FontWeight.Bold
    }
    if (renderFlag.hasFlag(REVERSE_COLOR)) {
        backgroundColor = Color.Black
        frontColor = Color.White
    }
    val style = TextStyle(
        textAlign = align,
        fontSize = fontSize,
        fontWeight = fontWeight,
        background = backgroundColor,
        color = frontColor,
        fontFamily = FontFamily.Monospace
    )
    if (renderFlag.hasFlag(LINE)) {
        tempText = text.repeat(46)
    }
    Row(modifier = modifier) {
        if (renderFlag.hasFlag(LR_RENDER)) {
            val (l, r) = if (tempText.contains("-*-")) {
                tempText.split("-*-")
            } else {
                listOf(tempText, "")
            }

            Text(text = l, style = style)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = r, style = style)
        } else if (renderFlag.hasFlag(IS_IMG)) {
            Text("图片")
        } else if (renderFlag.hasFlag(NOT_SUPPORT)) {
            Text(text = "")
        } else {
            Text(text = tempText, modifier = modifier, style = style)
        }
    }


}

object RenderMasks {
    private const val NO_CENTER_MASK = 0b111111110
    fun labelToMask(string: String): Int {
        return when (string) {
            "NODE" -> NO_CENTER_MASK
            else -> 0b111111111
        }
    }
}

object RenderFlags {
    const val CENTER = 1
    const val DOUBLE_WIDTH = 2
    const val DOUBLE_HEIGHT = 4
    const val LR_RENDER = 8
    const val BOLD = 16
    const val LINE = 32
    const val REVERSE_COLOR = 64
    const val IS_IMG = 128
    const val NOT_SUPPORT = 256
    fun labelToFlag(string: String): Int {
        return when (string) {
            "CB" -> CENTER or DOUBLE_HEIGHT or DOUBLE_WIDTH
            "LINE" -> LINE
            "LR" -> LR_RENDER
            "H" -> DOUBLE_HEIGHT
            "W" -> DOUBLE_WIDTH
            "BOLD" -> BOLD
            "B" -> DOUBLE_HEIGHT or DOUBLE_WIDTH
            "C" -> CENTER
            "RC" -> REVERSE_COLOR
            "PIC" -> IS_IMG
            "TEXT", "PRINTER" -> 0
            else -> NOT_SUPPORT
        }
    }

    fun Int.hasFlag(flag: Int): Boolean {
        return this and flag == flag
    }
}

data class ExpandNode(val node: Node, val parentPath: MutableList<String>)