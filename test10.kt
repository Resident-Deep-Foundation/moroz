@file:Suppress("UNCHECKED_CAST")

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class Valute(
    val ID: String,
    val numCode: String,
    val charCode: String,
    val Nominal: Int,
    val Name: String,
    val Value: Double,
    val Previous: Double
)

data class DailyJson(
    val Date: String,
    val previousDate: String,
    val previousURL: String,
    val Timestamp: String,
    val Valute: Map<String, Valute>
)

fun parseJsonObject(jsonString: String): DailyJson? {
    try {
        val jsonMap = parseJsonMap(jsonString)

        println("JSON Map:")
        jsonMap.forEach { (key, value) ->
            println("  $key: $value")
        }

        return createDailyJsonObject(jsonMap)
    } catch (e: Exception) {
        println("Ошибка при разборе JSON: ${e.message}")
        e.printStackTrace()
    }
    return null
}

fun parseJsonValue(value: String): Any? {
    return when {
        value == "true" || value == "false" -> value.toBoolean()
        value.contains(".") -> value.toDoubleOrNull()
        value.toIntOrNull() != null -> value.toInt()
        else -> value
    }
}

fun parseJsonMap(jsonString: String): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    var key: String? = null
    var value: Any?
    var isKey = true
    var isInsideString = false
    val currentString = StringBuilder()
    val stack = mutableListOf<Map<String, Any>>()
    stack.add(map)

    for (char in jsonString) {
        when {
            char == '"' -> {
                isInsideString = !isInsideString
                currentString.clear()
            }
            char == ':' && !isInsideString -> {
                key = currentString.toString().trim()
                currentString.clear()
                isKey = false
            }
            char == ',' && !isInsideString -> {
                value = parseJsonValue(currentString.toString().trim())
                currentString.clear()
                val currentMap = stack.last().toMutableMap()
                if (key != null) {
                    currentMap[key] = value ?: ""
                }
                isKey = true
            }
            char == '{' && !isInsideString -> {
                if (isKey) {
                    val newMap = mutableMapOf<String, Any>()
                    val currentMap = stack.last().toMutableMap()
                    if (key != null) {
                        currentMap[key] = newMap
                    }
                    stack.add(newMap)
                }
            }
            char == '}' && !isInsideString -> {
                if (stack.size > 1) {
                    stack.removeAt(stack.size - 1)
                }
            }
            else -> {
                currentString.append(char)
            }
        }
    }

    if (!isKey) {
        value = parseJsonValue(currentString.toString().trim())
        val currentMap = stack.last().toMutableMap()
        if (key != null) {
            currentMap[key] = value ?: ""
        }
    }

    return map
}



fun createValuteObjects(valuteMap: Map<String, Any>?): Map<String, Valute> {
    val valuteData = mutableMapOf<String, Valute>()

    valuteMap?.forEach { (key, value) ->
        if (value is Map<*, *>) {
            val valuteObject = value as Map<String, Any>
            valuteData[key] = Valute(
                ID = valuteObject["ID"] as? String ?: "",
                numCode = valuteObject["NumCode"] as? String ?: "",
                charCode = valuteObject["CharCode"] as? String ?: "",
                Nominal = (valuteObject["Nominal"] as? Int) ?: 0,
                Name = valuteObject["Name"] as? String ?: "",
                Value = (valuteObject["Value"] as? Double) ?: 0.0,
                Previous = (valuteObject["Previous"] as? Double) ?: 0.0
            )
        }
    }

    return valuteData
}

fun createDailyJsonObject(jsonMap: Map<String, Any>?): DailyJson? {
    if (jsonMap == null) {
        return null
    }

    val valuteData = createValuteObjects(jsonMap["Valute"] as? Map<String, Any>)

    return DailyJson(
        Date = jsonMap["Date"] as? String ?: "",
        previousDate = jsonMap["PreviousDate"] as? String ?: "",
        previousURL = jsonMap["PreviousURL"] as? String ?: "",
        Timestamp = jsonMap["Timestamp"] as? String ?: "",
        Valute = valuteData
    )
}

fun printDailyJsonInfo(dailyJson: DailyJson) {
    println("Date: ${dailyJson.Date}")
    println("PreviousDate: ${dailyJson.previousDate}")
    println("PreviousURL: ${dailyJson.previousURL}")
    println("Timestamp: ${dailyJson.Timestamp}")
    println("Valute:")
    dailyJson.Valute.forEach { (key, value) ->
        println("  $key:")
        println("    ID: ${value.ID}")
        println("    NumCode: ${value.numCode}")
        println("    CharCode: ${value.charCode}")
        println("    Nominal: ${value.Nominal}")
        println("    Name: ${value.Name}")
        println("    Value: ${value.Value}")
        println("    Previous: ${value.Previous}")
    }
}

fun main() {
    val jsonString = """
        {
            "Date": "2023-09-02T11:30:00+03:00",
            "PreviousDate": "2023-09-01T11:30:00+03:00",
            "PreviousURL": "\/\/www.cbr-xml-daily.ru\/archive\/2023\/09\/01\/daily_json.js",
            "Timestamp": "2023-09-03T20:00:00+03:00",
            "Valute": {
                "AUD": {
                    "ID": "R01010",
                    "NumCode": "036",
                    "CharCode": "AUD",
                    "Nominal": 1,
                    "Name": "Австралийский доллар",
                    "Value": 62.3423,
                    "Previous": 62.4729
                },
                "AZN": {
                    "ID": "R01020A",
                    "NumCode": "944",
                    "CharCode": "AZN",
                    "Nominal": 1,
                    "Name": "Азербайджанский манат",
                    "Value": 56.6712,
                    "Previous": 56.6673
                }
            }
        }
    """.trimIndent()

    val dailyJson = parseJsonObject(jsonString)

    if (dailyJson != null) {
        printDailyJsonInfo(dailyJson)
    } else {
        println("Ошибка при разборе JSON")
    }
}
