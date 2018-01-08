package upm.dam.voteitup.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.charts.HorizontalBarChart
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.LineChart
import upm.dam.voteitup.charts.PollBarChart
import upm.dam.voteitup.charts.PollActivityLineChart
import upm.dam.voteitup.entities.PollActivity
import android.content.Intent
import android.view.View
import upm.dam.voteitup.entities.Poll
import kotlinx.android.synthetic.main.activity_poll.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import upm.dam.voteitup.ApiClient
import upm.dam.voteitup.R
import upm.dam.voteitup.entities.UserActivity
import android.widget.*

class PollsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poll)

        val pollId = intent.getStringExtra(INTENT_POLL_ID)
                ?: throw IllegalStateException("field ${INTENT_POLL_ID} missing in Intent")

        val horizontalBarChart = findViewById<HorizontalBarChart>(R.id.pollHorizontalBarChart)
        val lineChart = findViewById<LineChart>(R.id.pollActivityLineChart)

        val getPollsAsync = async { ApiClient.getPoll(pollId.toInt()) }

        launch(UI) {
            val poll = getPollsAsync.await() ?: error("Error retrieving Poll info")

            val pollText = findViewById<TextView>(R.id.pollText)
            pollText.text = poll.text

            val votesText = findViewById<TextView>(R.id.votesText)
            votesText.text = poll.Choices.toString()
            votesText.text = poll.Choices.orEmpty().sumBy { it.votes }.toString() + " votos"

            val pollBarChart = PollBarChart(poll.Choices!!)
            var typeFace: Typeface? = ResourcesCompat.getFont(baseContext, R.font.roboto_light)

            pollBarChart.typeFace = typeFace
            pollBarChart.getChart(horizontalBarChart).invalidate()

            val activities = mutableListOf(
                    PollActivity(134534, "01-11-2017"),
                    PollActivity(523325, "01-11-2017"),
                    PollActivity(233225, "01-11-2017"),
                    PollActivity(423434, "01-11-2017"),
                    PollActivity(323424, "01-11-2017"),
                    PollActivity(654645, "01-11-2017"),
                    PollActivity(764564, "01-11-2017"))

            val pollActivityLineChart = PollActivityLineChart(activities)
            val lineChart2 = pollActivityLineChart.getChart(lineChart)

            lineChart2.invalidate()

            floatingActionVoteButton.setOnClickListener{
                horizontalBarChart.visibility = View.GONE
                recentActivityText.visibility = View.GONE
                lineChart.visibility = View.GONE
                pollChoicesLinearLayout.visibility = View.VISIBLE

                poll.Choices.forEach { choice ->
                    val choiceButton = Button(baseContext)

                    val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    layoutParams.setMargins(10, 15, 10, 15)
                    choiceButton.setLayoutParams(layoutParams)
                    choiceButton.setText(choice.text)
                    choiceButton.setBackgroundResource(R.drawable.rounded_btn)

                    choiceButton.setOnClickListener{
                        val votePoolAsync = async { ApiClient.votePool(poll.id!!.toInt(), choice.id) }

                        launch(UI) {
                            if(votePoolAsync.await() == false)
                                Toast.makeText(baseContext, "Ya has votado en esta encuesta!",Toast.LENGTH_LONG).show()
                            finish();
                            startActivity(getIntent())
                        }
                    }

                    pollChoicesLinearLayout.addView(choiceButton);
                }
            }
        }
    }

    companion object {

        val INTENT_POLL_ID = "poll_id"

        fun newIntent(context: Context, poll: Poll): Intent {
            val intent = Intent(context, PollsActivity::class.java)
            intent.putExtra(INTENT_POLL_ID, poll.id)
            return intent
        }

        fun newIntent(context: Context, userActivity: UserActivity): Intent {
            val intent = Intent(context, PollsActivity::class.java)
            intent.putExtra(INTENT_POLL_ID, userActivity.id)
            return intent
        }
    }
}
