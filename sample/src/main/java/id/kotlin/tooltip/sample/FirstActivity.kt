package id.kotlin.tooltip.sample

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.CompoundButton
import id.kotlin.tooltip.Tooltip
import id.kotlin.tooltip.Tooltip.AnimationBuilder
import id.kotlin.tooltip.Tooltip.TooltipView
import kotlinx.android.synthetic.main.activity_first.*
import kotlinx.android.synthetic.main.fragment_main.*

class FirstActivity : AppCompatActivity(), OnPageChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        setSupportActionBar(first_tool_bar)
        first_view_pager.addOnPageChangeListener(this)
        setupViewPager(first_view_pager)
        Tooltip.dbg = true
    }

    override fun onDestroy() {
        super.onDestroy()
        first_view_pager.removeOnPageChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.action_screen_2 -> {
                startActivity(Intent(this, SecondActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {}
    override fun onPageScrollStateChanged(state: Int) {}

    private fun setupViewPager(viewPager: ViewPager?): Adapter {
        viewPager?.adapter as? Adapter

        val adapter = Adapter(supportFragmentManager).apply {
            addFragment(CustomFragment(), "Tab 1")
            addFragment(CustomFragment(), "Tab 2")
        }
        viewPager?.adapter = adapter
        first_tabs.setupWithViewPager(viewPager)

        return adapter
    }

    inner class Adapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        private val fragments = arrayListOf<Fragment>()
        private val titles = arrayListOf<String>()
        private var currentFragment: Fragment? = null

        override fun getItem(position: Int): Fragment = fragments[position]
        override fun getCount(): Int = fragments.size
        override fun getPageTitle(position: Int): CharSequence? = titles[position]
        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            if (`object` != currentFragment) {
                currentFragment = `object` as? Fragment
            }

            super.setPrimaryItem(container, position, `object`)
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }
    }

    class CustomFragment : Fragment(), OnClickListener, Tooltip.Callback, CompoundButton.OnCheckedChangeListener {

        private var tooltipView: Tooltip.TooltipView? = null
        private val closePolicy: Tooltip.ClosePolicy = Tooltip.ClosePolicy.TOUCH_ANYWHERE_CONSUME

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(
                R.layout.fragment_main, container, false
        )

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            button_1.setOnClickListener(this)
            button_2.setOnClickListener(this)
            button_3.setOnClickListener(this)
            button_4.setOnClickListener(this)
            button_5.setOnClickListener(this)

            val policy = closePolicy.policy
            switch_1.isChecked = Tooltip.ClosePolicy.touchInside(policy)
            switch_2.isChecked = Tooltip.ClosePolicy.consumeInside(policy)
            switch_3.isChecked = Tooltip.ClosePolicy.touchOutside(policy)
            switch_4.isChecked = Tooltip.ClosePolicy.consumeOutside(policy)

            switch_1.setOnCheckedChangeListener(this)
            switch_2.setOnCheckedChangeListener(this)
            switch_3.setOnCheckedChangeListener(this)
            switch_4.setOnCheckedChangeListener(this)
        }

        override fun onClick(view: View?) {
            val id = view?.id
            val metrics = resources.displayMetrics
            when (id) {
             button_1.id -> {
                Tooltip.make(context, Tooltip.Builder()
                   .anchor(view, Tooltip.Gravity.RIGHT)
                   .closePolicy(closePolicy, 5000)
                   .text("RIGHT. Touch outside to close this tooltip. RIGHT. Touch outside to close this tooltip. RIGHT. Touch outside to close this tooltip.")
                   .withStyleId(R.style.ToolTipCustomFont)
                   .fitToScreen(true)
                   .activateDelay(2000)
                   .maxWidth(metrics.widthPixels / 2)
                   .withCallback(this)
                   .floatingAnimation(AnimationBuilder.DEFAULT)
                   .build()).show()
             }
             button_2.id -> {
                Tooltip.make(context, Tooltip.Builder()
                       .anchor(button_2, Tooltip.Gravity.BOTTOM)
                       .fitToScreen(true)
                       .closePolicy(closePolicy, 10000)
                       .text("BOTTOM. Touch outside to dismiss the tooltip")
                       .withArrow(true)
                       .maxWidth(metrics.widthPixels / 2)
                       .withCallback(this)
                       .withStyleId(R.style.AppTheme_Tooltip)
                       .build()).show()
             }
             button_3.id -> {
                 Tooltip.make(context, Tooltip.Builder()
                        .anchor(button_3, Tooltip.Gravity.TOP)
                        .closePolicy(closePolicy, 10000)
                        .text("TOP. Touch Inside the tooltip to dismiss..")
                        .withArrow(true)
                        .maxWidth((metrics.widthPixels / 2.5).toInt())
                        .withCallback(this)
                        .floatingAnimation(AnimationBuilder.DEFAULT)
                        .build()).show()
             }
             button_4.id -> {
                 Tooltip.make(context, Tooltip.Builder()
                        .anchor(view, Tooltip.Gravity.TOP)
                        .closePolicy(closePolicy, 5000)
                        .text("TOP. Touch Inside exclusive.")
                        .withArrow(true)
                        .withOverlay(false)
                        .maxWidth(metrics.widthPixels / 3)
                        .withCallback(this)
                        .build()).show()
             }
             button_5.id -> {
                 tooltipView = if (null == tooltipView) {
                     Tooltip.make(activity, Tooltip.Builder()
                            .anchor(view, Tooltip.Gravity.LEFT)
                            .closePolicy(closePolicy, 5000)
                            .text("LEFT. Touch None, so the tooltip won't disappear with a touch, but with a delay")
                            .withArrow(false)
                            .withOverlay(false)
                            .maxWidth(metrics.widthPixels / 3)
                            .showDelay(300)
                            .withCallback(this)
                            .build()).apply { show() }
                 } else {
                     tooltipView?.hide()
                     null
                 }
             }
            }
        }

        override fun onTooltipClose(tooltip: TooltipView?, fromUser: Boolean, containsTouch: Boolean) {
            if (null != tooltipView && tooltipView?.tooltipId == tooltip?.tooltipId) {
                tooltipView = null
            }
        }

        override fun onTooltipFailed(view: TooltipView?) {}
        override fun onTooltipShown(view: TooltipView?) {}
        override fun onTooltipHidden(view: TooltipView?) {}
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            closePolicy.insidePolicy(switch_1.isChecked, switch_2.isChecked)
                       .outsidePolicy(switch_3.isChecked, switch_4.isChecked)
            Tooltip.removeAll(activity)
        }
    }
}