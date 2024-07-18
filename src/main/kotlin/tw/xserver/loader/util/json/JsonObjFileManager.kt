package tw.xserver.loader.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger

class JsonObjFileManager(file: File) : JsonFileManager<JsonObject>(file) {
    override val dataType: Class<JsonObject> = JsonObject::class.java
    override fun defaultFileAndData(): JsonObject = JsonObject()

    fun get(): JsonObject = data
    fun getAsString(key: String): String = data[key].asString
    fun getAsByte(key: String): Byte = data[key].asByte
    fun getAsShort(key: String): Short = data[key].asShort
    fun getAsInt(key: String): Int = data[key].asInt
    fun getAsLong(key: String): Long = data[key].asLong
    fun getAsDouble(key: String): Double = data[key].asDouble
    fun getAsBoolean(key: String): Boolean = data[key].asBoolean
    fun getAsJsonObject(key: String): JsonObject = data[key].asJsonObject
    fun getAsJsonArray(key: String): JsonArray = data[key].asJsonArray
    fun getAsBigInteger(key: String): BigInteger = data[key].asBigInteger
    fun getAsBigDecimal(key: String): BigDecimal = data[key].asBigDecimal
    fun getAsJsonPrimitive(key: String): JsonPrimitive = data[key].asJsonPrimitive
    fun getAsNumber(key: String): Number = data[key].asNumber

    val asString: String = data.asString
    val asByte: Byte = data.asByte
    val asShort: Short = data.asShort
    val asInt: Int = data.asInt
    val asLong: Long = data.asLong
    val asDouble: Double = data.asDouble
    val asBoolean: Boolean = data.asBoolean
    val asJsonArray: JsonArray = data.asJsonArray
    val asBigInteger: BigInteger = data.asBigInteger
    val asBigDecimal: BigDecimal = data.asBigDecimal
    val asJsonPrimitive: JsonPrimitive = data.asJsonPrimitive
    val asNumber: Number = data.asNumber

    fun add(key: String, value: Number?): JsonObjFileManager {
        data.addProperty(key, value)
        return this
    }

    fun add(key: String, value: String?): JsonObjFileManager {
        data.addProperty(key, value)
        return this
    }

    fun add(key: String, value: Boolean?): JsonObjFileManager {
        data.addProperty(key, value)
        return this
    }

    fun add(key: String, value: Char?): JsonObjFileManager {
        data.addProperty(key, value)
        return this
    }

    fun add(key: String, value: JsonElement?): JsonObjFileManager {
        data.add(key, value)
        return this
    }

    fun getOrDefault(key: String, defaultElement: JsonElement): JsonElement =
        if (data.has(key)) data[key]
        else defaultElement

    fun computeIfAbsent(key: String, defaultElement: JsonElement): JsonElement = data.get(key) ?: run {
        data.add(key, defaultElement)
        defaultElement
    }

    fun computeIfPresent(key: String, defaultElement: JsonElement): JsonElement? {
        return if (data.has(key)) {
            data.add(key, defaultElement)
            defaultElement
        } else {
            null
        }
    }

    fun remove(key: String): JsonObjFileManager {
        if (data.has(key)) data.remove(key)
        return this
    }

    fun has(key: String): Boolean = data.has(key)
}
