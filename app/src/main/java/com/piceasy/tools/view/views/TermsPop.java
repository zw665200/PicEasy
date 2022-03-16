package com.piceasy.tools.view.views;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.piceasy.tools.R;
import com.piceasy.tools.callback.Callback;
import com.piceasy.tools.utils.JLog;

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/1/25 10:37
 */
public class TermsPop {
    //上下文对象
    private Activity mContext;
    //Title文字
    private String mTitle;
    //PopupWindow对象
    private PopupWindow mPopupWindow;
    //选项的文字
    private String[] options;
    //选项的颜色
    private int[] Colors;
    //点击事件
    private onPopupWindowItemClickListener itemClickListener;
    private TextView agreement;
    private Callback callback;

    /**
     * 一个参数的构造方法，用于弹出无标题的PopupWindow
     *
     * @param context
     */
    public TermsPop(Activity context) {
        this.mContext = context;
    }

    /**
     * 2个参数的构造方法，用于弹出有标题的PopupWindow
     *
     * @param context
     * @param title   标题
     */
    public TermsPop(Activity context, String title) {
        this.mContext = context;
        this.mTitle = title;
    }

    public TermsPop(Activity context, Callback callback) {
        this.mContext = context;
        this.callback = callback;
    }

    /**
     * 设置选项的点击事件
     *
     * @param itemClickListener
     */
    public void setItemClickListener(onPopupWindowItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * 设置选项文字
     */
    public void setItemText(String... items) {
        options = items;
    }

    /**
     * 设置选项文字颜色，必须要和选项的文字对应
     */
    public void setColors(int... color) {
        Colors = color;
    }


    /**
     * 弹出Popupwindow
     */
    public void showPopupWindow(View rootView) {
        View popupWindow_view = LayoutInflater.from(mContext).inflate(R.layout.pop_terms, null);
        agreement = popupWindow_view.findViewById(R.id.agreement);
        agreement.setOnClickListener(v -> callback.onSuccess());

        //添加子View
//        addView(popupWindow_view);
        mPopupWindow = new PopupWindow(popupWindow_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setAnimationStyle(R.style.translate_up_down);
//        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setOnDismissListener(this::dismiss);
        show(rootView);

    }


    /**
     * 显示PopupWindow
     */
    private void show(View v) {
        if (mPopupWindow != null && !mPopupWindow.isShowing()) {
            mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }
        setWindowAlpa(0.5f);
    }


    /**
     * 消失PopupWindow
     */
    public void dismiss() {
        JLog.i("123");
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }

        setWindowAlpa(1.0f);
    }

    /**
     * 动态设置Activity背景透明度
     *
     * @param bgAlpha
     */
    public void setWindowAlpa(float bgAlpha) {
        final Window window = mContext.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = bgAlpha;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(lp);
    }


    /**
     * 点击事件选择回调
     */
    public interface onPopupWindowItemClickListener {
        void onItemClick(int position);
    }
}
