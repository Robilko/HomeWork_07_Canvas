package otus.homework.customview

data class Transaction(
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)