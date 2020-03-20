package technology.bear.constans

enum class TaskFrequency(public val frequencyMessage: String, private val period: String) {
    EVERY_DAY("Раз в день", "1D"),
    EVERY_TWO_DAYS("Раз в два дня", "2D"),
    EVERY_WEEK("Раз в неделю", "1W"),
    EVERY_MONTH("Раз в месяц", "1M");

    companion object {
        fun parseTaskFrequency(frequency: String): TaskFrequency? {
            if (frequency.isBlank()) {
                return null
            }

            for (value in values()) {
                if (value.frequencyMessage == frequency) {
                    return value
                }
            }

            return null
        }
    }
}