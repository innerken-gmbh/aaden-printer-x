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

        fun translate(
            content: String,
            bonImage: Bitmap?,
            printer: PrinterSdk.Printer
        ): List<() -> Unit> {
            var text = content
            val actions = mutableListOf<() -> Unit>()


            val qrMatch = qrPattern.find(text)
            val barMatch = barPattern.find(text)
            qrContent = qrMatch?.groupValues?.get(1)
            println("11111$qrContent")
            barContent = barMatch?.groupValues?.get(1)
            if (qrContent != null) {
                text = content.replace(qrContent!!, "")
            }
            if (barContent != null) {
                text = content.replace(barContent!!, "")
            }

            var currentBaseStyle = BaseStyle.getStyle()
            var currentTextStyle = TextStyle.getStyle()

            // 初始化指令
            actions.add { printer.lineApi().initLine(currentBaseStyle)}

            var textLeft = text
            while (textLeft.isNotEmpty()) {
                val tokenMatch = tokenPattern.find(textLeft)
                if (tokenMatch != null) {
                    val tokenFull = tokenMatch.value
                    val tokenName = tokenMatch.groupValues[1]
                    val tokenAt = textLeft.indexOf(tokenFull)
                    val tokenLen = tokenFull.length

                    // 1) 打印 token 前面的文本（保留空格），使用当前样式
                    val before = textLeft.substring(0, tokenAt)
                    if (before.isNotEmpty()) {
                        // 注意：这里闭包会在 later 执行，读取到当时的 currentTextStyle/currentBaseStyle（引用）
                        actions.add {
                            // 每次打印文本前确保 baseStyle 已被初始化到 printer
                            printer.lineApi().initLine(currentBaseStyle)
                            printer.lineApi().printText(before, currentTextStyle)
                        }
                    }

                    // 2) 根据 token 更新样式或执行立即动作
                    when (tokenName) {
                        "BR", "/QR" -> {
                            actions.add {
                                printer.lineApi().initLine(currentBaseStyle)
                            }
                        }

                        "CUT" -> {
                            actions.add { printer.lineApi().autoOut() } // or cutPaper if available
                        }

                        "SMALL" -> {
                            actions.add { currentTextStyle = TextStyle.getStyle().setTextSize(18) }
                        }

                        "/SMALL" -> {
                            actions.add { currentTextStyle = TextStyle.getStyle().setTextSize(24) }
                        }

                        "CB" -> {
                            actions.add {
                                currentBaseStyle = BaseStyle.getStyle().setAlign(Align.CENTER)
                                currentTextStyle = TextStyle.getStyle().setTextSize(48).enableBold(true)
                            }
                        }

                        "/CB" -> {
                            actions.add {
                                currentBaseStyle = BaseStyle.getStyle().setAlign(Align.LEFT)
                                currentTextStyle = TextStyle.getStyle().setTextSize(24).enableBold(false)
                            }
                        }

                        "BOLD" -> {
                            actions.add { currentTextStyle = TextStyle.getStyle().enableBold(true) }
                        }

                        "/BOLD" -> {
                            actions.add { currentTextStyle = TextStyle.getStyle().enableBold(false) }
                        }

                        "C" -> {
                            actions.add { currentBaseStyle = BaseStyle.getStyle().setAlign(Align.CENTER) }
                        }

                        "/C" -> {
                            actions.add { currentBaseStyle = BaseStyle.getStyle().setAlign(Align.LEFT) }
                        }

                        "RIGHT" -> {
                            actions.add { currentBaseStyle = BaseStyle.getStyle().setAlign(Align.RIGHT) }
                        }

                        "/RIGHT" -> {
                            actions.add { currentBaseStyle = BaseStyle.getStyle().setAlign(Align.LEFT) }
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
                            actions.add {
                                barContent?.let {
                                    printer.lineApi().initLine(BaseStyle.getStyle().setAlign(Align.CENTER))
                                    printer.lineApi().printBarCode(it, BarcodeStyle.getStyle())
                                }
                            }
                        }

                        "BONLOGO" -> {
                            actions.add { bonImage?.let { printer.lineApi().printBitmap(it, BitmapStyle.getStyle()) } }
                        }

                        "PLUGIN" -> {
                            actions.add { printer.cashDrawerApi().open(null) }
                        }

                        else -> {
                            actions.add { printer.lineApi().initLine(currentBaseStyle) }
                        }
                    }

                    // 3) 剩余文本继续循环
                    textLeft = textLeft.substring(tokenAt + tokenLen)
                } else {
                    // 没有 token，直接打印剩余文本
                    val finalText = textLeft
                    actions.add {
                        printer.lineApi().initLine(currentBaseStyle)
                        printer.lineApi().printText(finalText, currentTextStyle)
                    }
                    break
                }
            }

            actions.add { printer.lineApi().printDividingLine(DividingLine.EMPTY, 30); }
            actions.add { printer.lineApi().autoOut() }

            return actions
        }
    }
}