package com.innerken.aadenprinterx.modules

import java.util.regex.Pattern

enum class FilterMode {
    BlackList, WhiteList,
}

fun printingThis(targetId: String?, filterSet: Set<String>, filterMode: FilterMode): Boolean {
    val result = when (filterMode) {
        FilterMode.BlackList -> targetId == null || !filterSet.contains(targetId)
        FilterMode.WhiteList -> targetId != null && filterSet.contains(targetId)

    }
    return result && (targetId == null || !targetId.startsWith("k2"))
}

val targetPattern: Pattern = Pattern.compile("t:(.+?);")

const val RefreshIntervalMs: Long = 1000