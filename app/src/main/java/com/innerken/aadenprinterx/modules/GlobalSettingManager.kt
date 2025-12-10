package com.innerken.aadenprinterx.modules

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.contains
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.javaType

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class SettingCategory(val category: String = "Basic")

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class SettingHideValue(val boolean: Boolean = true)

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class Selection(val options: Array<String>)

class StringPD(
    private val defaultValue: String, private val manager: GlobalSettingManager,
) {
    private var currentValue by mutableStateOf(defaultValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        currentValue = manager.stringGetter(property.name, defaultValue)
        return currentValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        manager.stringSetter(property.name, value)
    }
}

class BigDecimalPD(
    private val defaultValue: BigDecimal, private val manager: GlobalSettingManager,
) {
    private var currentValue by mutableStateOf(defaultValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): BigDecimal {
        currentValue = manager.stringGetter(property.name, defaultValue.toString()).toBigDecimal()
        return currentValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: BigDecimal) {
        manager.stringSetter(property.name, value.toString())
    }
}

class IntPD(
    private val defaultValue: Int, private val manager: GlobalSettingManager,
) {
    private var currentValue by mutableStateOf(defaultValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        currentValue = manager.stringGetter(property.name, defaultValue.toString()).toIntOrNull()
            ?: defaultValue
        return currentValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        manager.stringSetter(property.name, value.toString())
    }
}

class BooleanPD(
    private val defaultValue: Boolean, private val manager: GlobalSettingManager,
) {
    private var currentValue by mutableStateOf(defaultValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        currentValue = manager.booleanGetter(property.name, defaultValue)
        return currentValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        manager.booleanSetter(property.name, value)
    }
}

@Singleton
class GlobalSettingManager @Inject constructor(@ApplicationContext context: Context) {

    var deviceId: String by StringPD("", this)
    var blId: String by StringPD("", this)

    @SettingCategory("Network&Printer")
    var ip by StringPD("192.168.168.1", this)

    @SettingCategory("Network&Printer")
    var usePrintService: Boolean by BooleanPD(false, this)

    @SettingCategory("Network&Printer")
    var printingQueueNumber by BooleanPD(false, this)

    @SettingCategory("Network&Printer")
    var targetPrinter by StringPD("-1", this)

    @SettingCategory("Network&Printer")
    var filterSet by StringPD("-1", this)

    @SettingCategory("Network&Printer")
    var filterMode by BooleanPD(false, this)

    @SettingCategory("Network&Printer")
    var fpSn by StringPD("", this)

    var externalCustomerDisplayIp: String by StringPD("", this)

    // 以下接口都是要在for循环里调用的，注意性能。
    fun getFilterSet(): Set<String> {
        val string = filterSet
        val set = mutableSetOf<String>()
        if (string.isNotEmpty() && string != "-1") {
            string.split(',').forEach { set.add(it) }
        }
        Log.e("FILER", set.toString())
        return set
    }

    fun getFilterMode(): FilterMode {
        return if (filterMode) FilterMode.WhiteList else FilterMode.BlackList
    }

    fun getUrl(): String {
        return "http://$ip/"
    }

    fun getEndPoint(): String {
        return getUrl() + "PHP/"
    }

    fun getImgUrl(): String {
        return getResourceUrl() + "dishImg/"
    }

    fun getResourceUrl(): String {
        return getUrl() + "Resource/"
    }

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    fun stringGetter(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue)!!
    }

    fun booleanGetter(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun stringSetter(key: String, v: String) {
        prefs.edit().putString(key, v).commit()
    }

    fun booleanSetter(key: String, v: Boolean) {
        prefs.edit().putBoolean(key, v).commit()
    }

    @OptIn(ExperimentalStdlibApi::class)
    val settingCategoryList = mutableStateListOf<SettingCategoryStore>()
        get() {
            if (field.isEmpty()) {
                val map =
                    this.javaClass.kotlin.memberProperties.filterIsInstance<KMutableProperty<*>>()
                        .filter { f -> f.annotations.find { it is SettingCategory } != null }
                        .fold(mutableMapOf<String, MutableList<SettingData<*>>>()) { map, f ->
                            val category =
                                f.annotations.find { it is SettingCategory } as? SettingCategory
                            val hideValue =
                                (f.annotations.find { it is SettingHideValue } as? SettingHideValue)?.boolean == true
                            val key = category?.category ?: "UnKnown"
                            val data = when (f.returnType.javaType) {
                                Boolean::class.java -> {
                                    SettingDataBoolean(
                                        key = f.name,
                                        hideValue = hideValue,
                                        getterFunc = { f.getter.call(this) as Boolean },
                                        setterFunc = { f.setter.call(this, it) })
                                }

                                else -> {
                                    val selections = f.findAnnotations(Selection::class)
                                    if (selections.isNotEmpty()) {
                                        SettingListSelector(
                                            key = f.name,
                                            getterFunc = { f.getter.call(this) as String },
                                            stringEditor = { f.setter.call(this, it) },
                                            valueList = selections[0].options.asList(),
                                            hideValue = hideValue
                                        )
                                    } else {
                                        SettingDataString(
                                            key = f.name,
                                            hideValue = hideValue,
                                            getterFunc = { f.getter.call(this) as String },
                                            setterFunc = { f.setter.call(this, it) })
                                    }

                                }
                            }
                            if (map.contains(key)) {
                                map[key]?.add(data)
                            } else {
                                map[key] = mutableListOf(data)
                            }
                            map
                        }
                val list = map.keys.fold(mutableListOf<SettingCategoryStore>()) { list, key ->
                    if (map[key] != null) {
                        list.add(
                            SettingCategoryStore(
                                key, map[key]?.toMutableStateList() ?: mutableStateListOf()
                            )
                        )
                    }
                    list
                }
                field.clear()
                field.addAll(list)
            }
            return field.sortedBy { it.name }.toMutableStateList()
        }

}