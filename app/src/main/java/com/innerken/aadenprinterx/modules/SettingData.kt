package com.innerken.aadenprinterx.modules

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.lang.reflect.Type

interface SettingData<T> {
    val key: String
    val hideValue: Boolean
    val getterFunc: () -> T
    val setterFunc: ((v: T) -> Unit)?
    val type: Type
    val currentValue: MutableState<T>

    fun getValue(): T {
        return getterFunc()
    }

    fun setValue(v: T) {
        currentValue.value = v
        setterFunc?.let { it(v) }
    }
}

class SettingListSelector(
    override val key: String,
    override val getterFunc: () -> String,
    val stringEditor: (String) -> Unit,
    val valueList: List<String>,
    override val hideValue: Boolean = false,

    ) : SettingData<String> {
    override val currentValue: MutableState<String> = mutableStateOf("")
    var currentIndex by mutableStateOf(0)
    override val type: Type
        get() = String::class.java
    override val setterFunc: ((v: String) -> Unit)
        get() = { v ->
            updateCurrentIndex(valueList.indexOf(v))
            stringEditor(v)
        }


    private fun updateCurrentIndex(index: Int) {
        currentIndex = index
        currentValue.value = valueList[currentIndex]
    }

    init {
        val value = getValue()
        val defaultIndex = valueList.indexOf(value)
        updateCurrentIndex(if (defaultIndex == -1) 0 else defaultIndex)
    }
}


class SettingDataBoolean(
    override val key: String,
    override val hideValue: Boolean = false,
    override val getterFunc: () -> Boolean,
    override val setterFunc: (Boolean) -> Unit,
) : SettingData<Boolean> {
    override val type: Type
        get() = Boolean::class.java
    override val currentValue: MutableState<Boolean> = mutableStateOf(getValue())

    init {
        currentValue.value = getValue()
    }
}

class SettingDataString(
    override val key: String,
    override val hideValue: Boolean = false,
    override val getterFunc: () -> String,
    override val setterFunc: (String) -> Unit,
) : SettingData<String> {
    override val type: Type
        get() = String::class.java
    override val currentValue: MutableState<String> = mutableStateOf(getValue())

    init {
        currentValue.value = getValue()
    }
}


data class SettingCategoryStore(
    val name: String,
    val settingList: SnapshotStateList<SettingData<*>> = mutableStateListOf(),
)