package com.innerken.aadenprinterx.modules.printer

import android.graphics.Bitmap
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.enums.Align
import com.sunmi.printerx.enums.DividingLine
import com.sunmi.printerx.enums.ErrorLevel
import com.sunmi.printerx.style.BarcodeStyle
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.BitmapStyle
import com.sunmi.printerx.style.QrStyle
import com.sunmi.printerx.style.TextStyle

class PrintTranslator {

    companion object {
        private val tokenPattern = Regex("<(.+?)>")
        private val qrPattern = Regex("<QR>(.*?)</QR>", RegexOption.DOT_MATCHES_ALL)
        private val barPattern = Regex("<BARCODE>(.*?)</BARCODE>")

        private var qrContent: String? = null
        private var barContent: String? = null

        data class TextStyleState(
            var size: Int = 24,
            var bold: Boolean = false
        )

        data class BaseStyleState(
            var align: Align = Align.LEFT
        )

        private fun buildTextStyle(state: TextStyleState): TextStyle =
            TextStyle.getStyle()
                .setTextSize(state.size)
                .enableBold(state.bold)

        private fun buildBaseStyle(state: BaseStyleState): BaseStyle =
            BaseStyle.getStyle()
                .setAlign(state.align)
        fun translate(
            content: String,
            bonImage: Bitmap?,
            printer: PrinterSdk.Printer
        ): List<() -> Unit> {
            val actions = mutableListOf<() -> Unit>()
            var text = content

            val qrMatch = qrPattern.find(text)
            val barMatch = barPattern.find(text)
            qrContent = qrMatch?.groupValues?.get(1)
            barContent = barMatch?.groupValues?.get(1)
            if (qrContent != null) text = text.replace(qrContent!!, "")
            if (barContent != null) text = text.replace(barContent!!, "")

            val textState = TextStyleState()
            val baseState = BaseStyleState()

            actions.add { printer.lineApi().initLine(BaseStyle.getStyle()) }

            val lines = text.split("<BR>")
            for (line in lines) {
                val segments = mutableListOf<Pair<String, TextStyle>>()

                var remaining = line

                if (remaining.isEmpty()) {
                    actions.add {
                        printer.lineApi().initLine(BaseStyle.getStyle())
                        printer.lineApi().printText(" ", TextStyle.getStyle())
                    }
                    continue
                }

                while (remaining.isNotEmpty()) {
                    val tokenMatch = tokenPattern.find(remaining)
                    if (tokenMatch != null) {
                        val tokenFull = tokenMatch.value
                        val tokenName = tokenMatch.groupValues[1]
                        val tokenAt = remaining.indexOf(tokenFull)
                        val tokenLen = tokenFull.length

                        // 打印 token 前的内容（行内累积）
                        val before = remaining.substring(0, tokenAt)
                        if (before.isNotEmpty()) {
                            segments.add(before to buildTextStyle(textState))
                        }

                        if (segments.isNotEmpty()) {
                            val segCopy = segments.toList()
                            val baseCopy = buildBaseStyle(baseState)
                            actions.add {
                                printer.lineApi().initLine(baseCopy)
                                segCopy.forEach { (t, s) -> printer.lineApi().addText(t, s) }
                                printer.lineApi().printText("\n", segCopy.last().second)
                            }
                            segments.clear()
                        }
                        when (tokenName) {
                            "CUT" -> {
                                printer.lineApi().autoOut()
                            }

                            // ----- 文本样式 -----
                            "BOLD" -> textState.bold = true
                            "/BOLD" -> textState.bold = false

                            "B" -> textState.size = 48
                            "/B" -> textState.size = 24

                            "SMALL" -> textState.size = 18
                            "/SMALL" -> textState.size = 24

                            // ----- 行样式 -----
                            "C" -> baseState.align = Align.CENTER
                            "/C" -> baseState.align = Align.LEFT

                            "RIGHT" -> baseState.align = Align.RIGHT
                            "/RIGHT" -> baseState.align = Align.LEFT

                            // ----- 文本+行样式 -----
                            "CB" -> {
                                baseState.align = Align.CENTER
                                textState.size = 48
                                textState.bold = true
                            }

                            "/CB" -> {
                                baseState.align = Align.LEFT
                                textState.size = 24
                                textState.bold = false
                            }

                            "QR" -> {
                                qrContent?.let { content ->
                                    var dotSize = 4
                                    var errorLevel = ErrorLevel.L

                                    val sizePattern = Regex("<QRSIZE>(\\d+)</QRSIZE>")
                                    val recPattern = Regex("<QREC>([MLH])</QREC>")

                                    sizePattern.find(content)?.groupValues?.get(1)?.toIntOrNull()?.let { dotSize = it }
                                    recPattern.find(content)?.groupValues?.get(1)?.let {
                                        errorLevel = when (it.uppercase()) {
                                            "L" -> ErrorLevel.L
                                            "M" -> ErrorLevel.M
                                            "Q" -> ErrorLevel.Q
                                            "H" -> ErrorLevel.H
                                            else -> ErrorLevel.L
                                        }
                                    }

                                    val qrText = content
                                        .replace(sizePattern, "")
                                        .replace(recPattern, "")
                                        .trim()

                                    actions.add {
                                        printer.lineApi().initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
                                        printer.lineApi().printQrCode(
                                            qrText,
                                            QrStyle.getStyle()
                                                .setDot(dotSize)
                                                .setErrorLevel(errorLevel)
                                        )
                                    }
                                }

                            }


                            "BARCODE" -> {
                                barContent?.let {
                                    actions.add {
                                        printer.lineApi().initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
                                        printer.lineApi().printBarCode(it, BarcodeStyle.getStyle())
                                    }
                                }
                            }

                            "BONLOGO" -> {
                                bonImage?.let {
                                    actions.add {
                                        printer.lineApi().printBitmap(it, BitmapStyle.getStyle())
                                    }
                                }
                            }

                            "/BONLOGO", "/BARCODE", "/QR" -> {
                                printer.lineApi().initLine(BaseStyle.getStyle())
                            }

                            "PLUGIN" -> {
                                actions.add { printer.cashDrawerApi().open(null) }
                            }

                            else -> {

                            }
                        }

                        remaining = remaining.substring(tokenAt + tokenLen)
                    } else {
                        segments.add(remaining to buildTextStyle(textState))

                        val lastSegCopy = segments.toList()
                        val lastBaseCopy = buildBaseStyle(baseState)
                        actions.add {
                            printer.lineApi().initLine(lastBaseCopy)
                            lastSegCopy.forEach { (t, s) -> printer.lineApi().addText(t, s) }
                            printer.lineApi().printText("\n", lastSegCopy.last().second)
                        }
                        segments.clear()
                        break
                    }
                }
            }

            actions.add { printer.lineApi().printDividingLine(DividingLine.EMPTY, 30) }
            actions.add { printer.lineApi().autoOut() }

            return actions
        }
    }
}