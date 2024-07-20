package otus.homework.customview

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private var data = listOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
        val pieChartText = findViewById<TextView>(R.id.pie_chart_text)

        if (savedInstanceState == null) {
            getInitialData()

            pieChartView.setData(
                data.groupBy { it.category }.map { (cat, amounts) ->
                    PieChartView.Category(cat, amounts.sumOf { it.amount })
                }
            )
        }

        pieChartView.setOnCategoryClickListener(
            object : PieChartView.OnCategoryClickListener {
                override fun onClick(category: PieChartView.Category) {
                    pieChartText.text =
                        getString(
                            R.string.selected_category_text,
                            category.name,
                            category.sum.toString()
                        )

                    val list = data.filter { category.name.contains(it.category) }
                    val intent = Intent(this@MainActivity, GraphChartActivity::class.java)
                    intent.putExtra(GraphChartActivity.KEY_DATA, Gson().toJson(list))
                    startActivity(intent)
                }
            }
        )
    }

    private fun getInitialData() {
        val jsonString =
            resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        val transactions = JSONArray(jsonString)

        val data = mutableListOf<Transaction>()

        for (i in 0 until transactions.length()) {
            val transaction = transactions.getJSONObject(i)
            data.add(
                Transaction(
                    name = transaction.getString("name"),
                    amount = transaction.getInt("amount"),
                    category = transaction.getString("category"),
                    time = transaction.getLong("time")
                )
            )
        }
        this.data = data
    }
}