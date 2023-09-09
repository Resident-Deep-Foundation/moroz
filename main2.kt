import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Valute(
    val ID: String,
    @SerialName("NumCode") val numCode: String,
    @SerialName("CharCode") val charCode: String,
    val Nominal: Int,
    val Name: String,
    val Value: Double,
    val Previous: Double
)

@Serializable
data class DailyJson(
    val Date: String,
    @SerialName("PreviousDate") val previousDate: String,
    @SerialName("PreviousURL") val previousURL: String,
    val Timestamp: String,
    val Valute: Map<String, Valute>
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

    val dailyJson = Json.decodeFromString<DailyJson>(jsonString)

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
