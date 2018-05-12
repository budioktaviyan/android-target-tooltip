package id.kotlin.tooltip.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import id.kotlin.tooltip.Tooltip
import id.kotlin.tooltip.Tooltip.TooltipView
import kotlinx.android.synthetic.main.activity_second.*
import kotlinx.android.synthetic.main.item_main.view.*

class SecondActivity : AppCompatActivity(), OnItemClickListener {

    companion object {
        private const val TOOLTIP_ID = 101
        private const val LIST_POSITION = 5
    }

    private lateinit var displayMetrics: DisplayMetrics

    private var tooltipView: Tooltip.TooltipView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        setSupportActionBar(second_toolbar)

        val items = arrayListOf<String>()
        for (i in 0 until 100) {
            items.add("Item $i")
        }
        displayMetrics = resources.displayMetrics
        second_recycler_view.adapter = Adapter(R.layout.item_main, items)
        second_recycler_view.setHasFixedSize(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.action_screen_1 -> {
                startActivity(Intent(this, FirstActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position == SecondActivity.LIST_POSITION) {
            if (null != tooltipView) {
                tooltipView?.hide()
                tooltipView = null
            }
        }
    }

    inner class Adapter(private val id: Int,
                        private val data: List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        init {
            setHasStableIds(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(this@SecondActivity).inflate(id, parent, false)
            val holder = object : RecyclerView.ViewHolder(view) {}
            view.setOnClickListener {
                if (null != tooltipView) {
                    tooltipView?.hide()
                    tooltipView = null
                } else {
                    showTooltip(holder)
                }
            }

            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.item_text.apply { text = data[position] }
            if (position == SecondActivity.LIST_POSITION) {
                showTooltip(holder)
            }
        }

        override fun getItemId(position: Int): Long = position.toLong()
        override fun getItemCount(): Int = data.size

        private fun showTooltip(holder: ViewHolder) {
            tooltipView = Tooltip.make(this@SecondActivity, Tooltip.Builder(SecondActivity.TOOLTIP_ID)
                                 .maxWidth((displayMetrics.widthPixels / 2))
                                 .anchor(holder.itemView.item_text, Tooltip.Gravity.RIGHT)
                                 .closePolicy(Tooltip.ClosePolicy.TOUCH_INSIDE_NO_CONSUME, 0)
                                 .text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc lacinia sem non neque commodo consectetur")
                                 .fitToScreen(false)
                                 .fadeDuration(200)
                                 .showDelay(50)
                                 .withCallback(object : Tooltip.Callback{
                                     override fun onTooltipClose(tooltip: TooltipView?, fromUser: Boolean, containsTouch: Boolean) {
                                         tooltipView = null
                                     }

                                     override fun onTooltipFailed(view: TooltipView?) {}
                                     override fun onTooltipShown(view: TooltipView?) {}
                                     override fun onTooltipHidden(view: TooltipView?) {}
                                 })
                                 .build())
            tooltipView?.show()
        }
    }
}