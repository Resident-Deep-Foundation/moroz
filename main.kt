data class Valute(
    val ID: String,
    val NumCode: String,
    val CharCode: String,
    val Nominal: Int,
    val Name: String,
    val Value: Double,
    val Previous: Double
)

data class DailyJson(
    val Date: String,
    val PreviousDate: String,
    val PreviousURL: String,
    val Timestamp: String,
    val Valute: List<Valute>
)

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


    val dailyJson = parseJson(jsonString)

    
    println("DailyJson(Date= \"${dailyJson.Date}\", PreviousDate= \"${dailyJson.PreviousDate}\", PreviousURL= \"${dailyJson.PreviousURL}\", Timestamp= \"${dailyJson.Timestamp}\", Valute=[")
    dailyJson.Valute.forEachIndexed { index, valute ->
        val separator = if (index < dailyJson.Valute.size - 1) "," else ""
        println("Valute(ID= \"${valute.ID}\", NumCode= \"${valute.NumCode}\", CharCode= \"${valute.CharCode}\", Nominal= ${valute.Nominal}, Name= \"${valute.Name}\", Value= ${valute.Value}, Previous= ${valute.Previous})$separator")
    }
    println("])")
    
}

fun parseJson(jsonString: String): DailyJson {
    val lines = jsonString.lines()

    
    val date = getValue(lines, "Date")
    val previousDate = getValue(lines, "PreviousDate")
    val previousURL = getValue(lines, "PreviousURL")
    val timestamp = getValue(lines, "Timestamp")

  
    val valuteJson = getValue(lines, "Valute")
    val valuteList = parseValute(valuteJson)

    return DailyJson(date, previousDate, previousURL, timestamp, valuteList)
}

fun parseValute(valuteJson: String): List<Valute> {
    val valuteList = mutableListOf<Valute>()

    val lines = valuteJson.lines()
    for (line in lines) {
        if (line.trim().startsWith("{")) {
            val valuteData = parseValuteData(lines)
            valuteList.add(
                Valute(
                    getValue(valuteData, "ID"),
                    getValue(valuteData, "NumCode"),
                    getValue(valuteData, "CharCode"),
                    getValue(valuteData, "Nominal").toInt(),
                    getValue(valuteData, "Name"),
                    getValue(valuteData, "Value").toDouble(),
                    getValue(valuteData, "Previous").toDouble()
                )
            )
        }
    }

    return valuteList
}

fun parseValuteData(lines: List<String>): List<String> {
    val valuteData = mutableListOf<String>()
    var openBraceCount = 0
    var closeBraceCount = 0
    var insideValute = false

    for (line in lines) {
        if (line.contains("{")) {
            openBraceCount++
            insideValute = true
        }
        if (line.contains("}")) {
            closeBraceCount++
        }
        if (insideValute) {
            valuteData.add(line)
        }

        if (openBraceCount == closeBraceCount && openBraceCount > 0) {
            break
        }
    }

    return valuteData
}

fun getValue(lines: List<String>, key: String): String {
    for (line in lines) {
        if (line.trim().startsWith("\"$key\":")) {
            return line.trim(' ', '"', ':')
        }
    }
    return ""
}
