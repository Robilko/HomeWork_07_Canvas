package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GraphChartActivity : AppCompatActivity() {

    private var data = listOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_chart)

        val graphChartView = findViewById<GraphChartView>(R.id.graph_chart)

        initData()
        graphChartView.setData(data.map { GraphChartView.Item(it.amount, it.time) })
    }

    private fun initData() {
        val type = object : TypeToken<List<Transaction>>() {}.type
        data = Gson().fromJson(intent.getStringExtra(KEY_DATA), type) ?: listOf()
    }


    companion object {
        const val KEY_DATA = "2d512508-18c1-48d8-b4d9-376075805456"
    }
}