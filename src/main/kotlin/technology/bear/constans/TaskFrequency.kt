package technology.bear.constans

enum class TaskFrequency(private val frequencyMessage: String, private val period: String) {
    ONCE_A_MINUTE("Раз в минуту", "1M"),
    TWICE_A_MINUTE("Раз в две минуты", "2M"),
    THREE_TIMES_A_MINUTE("Раз в три минуты", "3M");

    override fun toString() = frequencyMessage

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