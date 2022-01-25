package com.picfix.tools.view.fragment

import android.content.Intent
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.picfix.tools.R
import com.picfix.tools.adapter.DataAdapter
import com.picfix.tools.bean.Resource
import com.picfix.tools.utils.*
import com.picfix.tools.view.activity.*
import com.picfix.tools.view.base.BaseFragment
import kotlinx.android.synthetic.main.item_heart.view.*
import kotlinx.android.synthetic.main.item_home_pic.view.*
import kotlinx.android.synthetic.main.item_other.view.*
import kotlinx.coroutines.*
import java.util.*

open class FHome : BaseFragment() {
    private lateinit var otherRv: RecyclerView
    private lateinit var fix: ImageView
    private lateinit var removeWatermark: ImageView
    private lateinit var more: TextView
    private var lastClickTime = 0L

    private lateinit var cartoon: FrameLayout
    private lateinit var cutout: FrameLayout
    private lateinit var format: FrameLayout

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.f_fix, container, false)
        otherRv = rootView.findViewById(R.id.other_tools)

        fix = rootView.findViewById(R.id.photo_fix)
        removeWatermark = rootView.findViewById(R.id.photo_remove_watermark)

        cartoon = rootView.findViewById(R.id.cartoon)
        cutout = rootView.findViewById(R.id.cutout)
        format = rootView.findViewById(R.id.format)

        more = rootView.findViewById(R.id.more)

        fix.setOnClickListener { checkPermissions { toPhotoDefinitionPage() } }
        removeWatermark.setOnClickListener { checkPermissions { toPhotoWatermarkPage() } }
        cartoon.setOnClickListener { checkPermissions { toPhotoCartoonPage() } }
        cutout.setOnClickListener { checkPermissions { toPhotoMattingPage() } }
        format.setOnClickListener { checkPermissions { toPhotoFormatTransPage() } }
        more.setOnClickListener { (activity as MainActivity).changeFragment(1) }

        return rootView
    }

    override fun initData() {

        initOtherRecycleView()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.color_light_white)
        }
    }


    override fun click(v: View?) {
    }

    private fun initOtherRecycleView() {
        val otherPics = mutableListOf<Resource>()
        otherPics.add(Resource("face_merge", R.drawable.home_face_fusion, getString(R.string.title_face_merge)))
        otherPics.add(Resource("colorize", R.drawable.home_colorizer, getString(R.string.title_colour)))
        otherPics.add(Resource("zip", R.drawable.home_compression, getString(R.string.title_compress)))

        val otherAdapter = DataAdapter.Builder<Resource>()
            .setData(otherPics)
            .setLayoutId(R.layout.item_other)
            .addBindView { itemView, itemData ->

                itemView.iv_other_icon.setImageResource(itemData.icon)
                itemView.tv_other_name.text = itemData.name

                itemView.setOnClickListener {
                    if (lastClickTime == 0L) {
                        lastClickTime = System.currentTimeMillis()
                    } else {
                        if (System.currentTimeMillis() - lastClickTime < 1000) return@setOnClickListener
                    }

                    checkPermissions {
                        when (itemData.type) {
                            "face_merge" -> toPhotoFaceMergePage()
                            "colorize" -> toPhotoColourPage()
                            "zip" -> toPhotoZipPage()

                            "style_trans" -> toPhotoTransStylePage()
                            "contrast" -> toPhotoContrastPage()
                            "resize" -> toPhotoResizePage()
                            "defogging" -> toPhotoDefoggingPage()
                            "stretch" -> toPhotoStretchPage()
                            "age_trans" -> toPhotoAgeTransPage()
                            "gender_trans" -> toPhotoGenderTransPage()
                            "morph" -> toPhotoMorphPage()
                        }
                    }
                }
            }
            .create()

        otherRv.layoutManager = GridLayoutManager(activity, 3)
        otherRv.adapter = otherAdapter
        otherAdapter.notifyItemRangeChanged(0, otherPics.size)
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
}