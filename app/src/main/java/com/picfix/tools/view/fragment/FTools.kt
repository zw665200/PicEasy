package com.picfix.tools.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.Resource
import com.picfix.tools.utils.*
import com.picfix.tools.view.activity.*
import com.picfix.tools.view.base.BaseFragment
import com.picfix.tools.view.views.ScaleInTransformer
import kotlinx.android.synthetic.main.item_banner.view.*
import kotlinx.android.synthetic.main.item_heart.view.*
import kotlinx.android.synthetic.main.item_home_pic.view.*
import kotlinx.android.synthetic.main.item_other.view.*
import kotlinx.android.synthetic.main.item_recommend.view.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*

open class FTools : BaseFragment() {
    private lateinit var rv: RecyclerView
    private lateinit var pager: ViewPager2
    private var lastClickTime = 0L
    private var isLoop = true
    private var currentPosition = 0
    private var handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    private lateinit var pointOne: ImageView
    private lateinit var pointTwo: ImageView
    private lateinit var pointThree: ImageView
    private lateinit var pointFour: ImageView

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.f_tools, container, false)
        rv = rootView.findViewById(R.id.ry_billboard)
        pager = rootView.findViewById(R.id.pager)

        pointOne = rootView.findViewById(R.id.point_one)
        pointTwo = rootView.findViewById(R.id.point_two)
        pointThree = rootView.findViewById(R.id.point_three)
        pointFour = rootView.findViewById(R.id.point_four)

        return rootView
    }

    override fun initData() {

        initPager()
        initMainRecycleView()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.color_light_white)
        }

    }


    override fun click(v: View?) {
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initPager() {
        if (runnable != null) {
            return
        }

        runnable = Runnable {
            if (currentPosition == 3) {
                currentPosition = 0
                pager.setCurrentItem(0, true)
            } else {
                currentPosition += 1
                pager.setCurrentItem(currentPosition, true)
            }
        }

        val mainPics = mutableListOf<Resource>()
        mainPics.add(Resource("pic", R.drawable.home_banner_01, "图片恢复"))
        mainPics.add(Resource("audio", R.drawable.home_banner_02, "语音恢复"))
        mainPics.add(Resource("video", R.drawable.home_banner_03, "视频恢复"))
        mainPics.add(Resource("video", R.drawable.home_banner_04, "视频恢复"))

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    pager.setCurrentItem(currentPosition, true)
                    return
                }

                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    handler.removeCallbacks(runnable!!)
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
                if (isLoop) {
                    handler.postDelayed(runnable!!, 2000)
                }

                when (position) {
                    0 -> {
                        pointOne.setBackgroundResource(R.drawable.ic_point_select)
                        pointTwo.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointThree.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointFour.setBackgroundResource(R.drawable.ic_point_unselect)
                    }

                    1 -> {
                        pointOne.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointTwo.setBackgroundResource(R.drawable.ic_point_select)
                        pointThree.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointFour.setBackgroundResource(R.drawable.ic_point_unselect)
                    }

                    2 -> {
                        pointOne.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointTwo.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointThree.setBackgroundResource(R.drawable.ic_point_select)
                        pointFour.setBackgroundResource(R.drawable.ic_point_unselect)
                    }

                    3 -> {
                        pointOne.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointTwo.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointThree.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointFour.setBackgroundResource(R.drawable.ic_point_select)
                    }
                }
            }

        })

        val pagerAdapter = DataAdapter.Builder<Resource>()
            .setData(mainPics)
            .setLayoutId(R.layout.item_banner)
            .addBindView { itemView, itemData ->
                itemView.iv_icon.setImageResource(itemData.icon)
            }
            .create()

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(ScaleInTransformer())
        compositePageTransformer.addTransformer(MarginPageTransformer(resources.getDimension(R.dimen.dp_5).toInt()))

        pager.apply {
            adapter = pagerAdapter
            setPageTransformer(compositePageTransformer)
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 4
        }

    }


    private fun initMainRecycleView() {
        val mainPics = mutableListOf<Resource>()
        mainPics.add(Resource("contrast", R.drawable.tools_enhance, "Constrast"))
        mainPics.add(Resource("style_trans", R.drawable.tools_style_shift, "Style Shift"))
        mainPics.add(Resource("morph", R.drawable.tools_portrait, "Portrait Gradient"))
        mainPics.add(Resource("age_trans", R.drawable.tools_age_conversion, "Age Conversion"))
        mainPics.add(Resource("defogging", R.drawable.tools_defogging, "Defogging"))
        mainPics.add(Resource("gender_trans", R.drawable.tools_gender_transtion, "Gender"))
        mainPics.add(Resource("resize", R.drawable.tools_amplification, "Amplification"))
        mainPics.add(Resource("stretch", R.drawable.tools_repair, "Stretch Recovery"))
        mainPics.add(Resource("comming_soon", R.drawable.tools_coming_soon, "Coming soon"))

        val mainAdapter = DataAdapter.Builder<Resource>()
            .setData(mainPics)
            .setLayoutId(R.layout.item_other)
            .addBindView { itemView, itemData ->

                itemView.tv_other_name.text = itemData.name
                itemView.iv_other_icon.setImageResource(itemData.icon)

                itemView.setOnClickListener {
                    if (lastClickTime == 0L) {
                        lastClickTime = System.currentTimeMillis()
                    } else {
                        if (System.currentTimeMillis() - lastClickTime < 1000) return@setOnClickListener
                    }

                    lastClickTime = System.currentTimeMillis()

                    checkPermissions {
                        when (itemData.type) {
                            "contrast" -> toPhotoContrastPage()
                            "style_trans" -> toPhotoTransStylePage()
                            "morph" -> toPhotoMorphPage()
                            "age_trans" -> toPhotoAgeTransPage()
                            "defogging" -> toPhotoDefoggingPage()
                            "gender_trans" -> toPhotoGenderTransPage()
                            "resize" -> toPhotoResizePage()
                            "stretch" -> toPhotoStretchPage()

                            "face_merge" -> toPhotoFaceMergePage()
                            "colorize" -> toPhotoColourPage()
                            "zip" -> toPhotoZipPage()
                        }
                    }

                }
            }
            .create()

        rv.adapter = mainAdapter
        rv.layoutManager = GridLayoutManager(activity, 3)
        mainAdapter.notifyItemRangeChanged(0, mainPics.size)
    }

    private fun toPayPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PayActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoMattingPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoMattingActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoColourPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoColourActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoWatermarkPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoWatermarkActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoResizePage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoResizeActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoDefinitionPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoDefinitionActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoContrastPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoContrastActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoCartoonPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoCartoonActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoTransStylePage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoTransStyleActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoDefoggingPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoDefoggingActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoStretchPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoStretchActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoZipPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoZipActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoAgeTransPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoAgeTransActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoGenderTransPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoGenderTransActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoMorphPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoMorphActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoFormatTransPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoFormatTransActivity::class.java)
        startActivity(intent)
    }

    private fun toPhotoFaceMergePage() {
        val intent = Intent()
        intent.setClass(requireActivity(), PhotoFaceMergeActivity::class.java)
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}