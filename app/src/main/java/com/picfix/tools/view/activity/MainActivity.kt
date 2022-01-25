package com.picfix.tools.view.activity

import android.content.Intent
import android.os.*
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.picfix.tools.R
import com.picfix.tools.utils.JLog
import com.picfix.tools.view.base.BaseFragment
import com.picfix.tools.view.base.BaseFragmentActivity
import com.picfix.tools.view.fragment.FHome
import com.picfix.tools.view.fragment.FTools
import com.picfix.tools.view.fragment.FMine

class MainActivity : BaseFragmentActivity(), View.OnClickListener {

    private val FRAGMENT_HOME = 0

    private val DEFAULT_INDEX = FRAGMENT_HOME

    val BOTTOM_ICON_CHECKED = arrayOf(
        R.drawable.ic_global_camera_select,
        R.drawable.ic_global_pic_select,
        R.drawable.ic_global_mine_select
    )

    val BOTTOM_ICON_UNCHECKED = arrayOf(
        R.drawable.ic_global_camera_unselect,
        R.drawable.ic_global_pic_unselect,
        R.drawable.ic_global_mine_unselect
    )

    var BOTTOM_TEXT_ARRAY = arrayOf("Home", "Tools", "Me")

    val BOTTOM_CHECKED_COLOR: Int = 0xff6BB4FF.toInt()
    val BOTTOM_UNCHECKED_COLOR: Int = 0xff212121.toInt()

    val FRAGMENT_CLASS_ARRAY: Array<Class<out BaseFragment>> = arrayOf(
        FHome::class.java,
        FTools::class.java,
        FMine::class.java
    )

    private var mCheckedFragmentID: Int = DEFAULT_INDEX


    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.a_home)
        initData()
        super.onCreate(savedInstanceState)
    }

    private fun initData() {
        JLog.i("size = ${BOTTOM_TEXT_ARRAY.size}")
        if (BOTTOM_TEXT_ARRAY.size == 3) {
            BOTTOM_TEXT_ARRAY[0] = getString(R.string.home)
            BOTTOM_TEXT_ARRAY[1] = getString(R.string.tools)
            BOTTOM_TEXT_ARRAY[2] = getString(R.string.me)
        }
    }

    override fun initView() {
    }


    override fun onItemClick(item: View?, index: Int) {
        mCheckedFragmentID = index
    }


    override fun putFragments(): Array<Class<out BaseFragment>> {
        return FRAGMENT_CLASS_ARRAY
    }

    private val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
    override fun getBottomItemView(index: Int): View {
        val bottomView = bottomLayoutInflater.inflate(R.layout.l_home_bottom, null)
        val bottomLayout = bottomView.findViewById<LinearLayout>(R.id.home_page_bottom_layout)
        bottomLayout.layoutParams = params

        val bottomImage = bottomView.findViewById<ImageView>(R.id.home_page_bottom_image)
        bottomImage.setImageResource(BOTTOM_ICON_UNCHECKED[index])
        val buttonName = bottomView.findViewById<TextView>(R.id.home_page_bottom_btn_name)
        buttonName.text = BOTTOM_TEXT_ARRAY[index]
        return bottomView
    }

    override fun getFLid(): Int {
        return R.id.fl_home_body
    }

    override fun getBottomLayout(): LinearLayout? {
        return this@MainActivity.findViewById(R.id.ll_home_bottom)
    }

    override fun checkAllBottomItem(item: View?, position: Int, isChecked: Boolean) {
        (item?.findViewById<ImageView>(R.id.home_page_bottom_image))?.setImageResource(if (isChecked) BOTTOM_ICON_CHECKED[position] else BOTTOM_ICON_UNCHECKED[position])
        (item?.findViewById<TextView>(R.id.home_page_bottom_btn_name))?.setTextColor(if (isChecked) BOTTOM_CHECKED_COLOR else BOTTOM_UNCHECKED_COLOR)
    }

    override fun setTabSel(item: View?, index: Int) {
        super.setTabSel(item, index)

    }

    fun changeFragment(index: Int) {
        setTabSel(bottomLayout?.getChildAt(index), index)
    }


    private var mLastClick: Long = 0L
    override fun onBackPressed() {
        if (System.currentTimeMillis() - mLastClick < 2000) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addCategory(Intent.CATEGORY_HOME)
            }
            startActivity(homeIntent)
        } else {
            Toast.makeText(this@MainActivity, "再按一下后退键退出程序", Toast.LENGTH_SHORT).show()
            mLastClick = System.currentTimeMillis()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.hook -> {

            }
        }
    }


}