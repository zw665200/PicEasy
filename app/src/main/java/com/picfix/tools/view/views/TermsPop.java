package com.picfix.tools.view.views;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.picfix.tools.R;
import com.picfix.tools.callback.Callback;
import com.picfix.tools.view.activity.PayActivity;

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
     * 添加子View
     */
//    private void addView(View v) {
//        LinearLayout lin_layout = v.findViewById(R.id.layout_popup_add);
//        //Title
//        TextView tv_pop_title = v.findViewById(R.id.tv_popup_title);
//        //取消按钮
//        Button btn_cancel = v.findViewById(R.id.btn_cancel);
//        btn_cancel.setOnClickListener(v1 -> dismiss());
//        if (mTitle != null) {
//            tv_pop_title.setText(mTitle);
//        } else {
//            tv_pop_title.setVisibility(View.GONE);
//        }
//        if (options != null && options.length > 0) {
//            for (int i = 0; i < options.length; i++) {
//                View item = LayoutInflater.from(mContext).inflate(R.layout.pop_terms, null);
//                Button btn_txt = (Button) item.findViewById(R.id.btn_popup_option);
//                btn_txt.setText(options[i]);
//                if (Colors != null && Colors.length == options.length) {
//                    btn_txt.setTextColor(Colors[i]);
//                }
//                final int finalI = i;
//                btn_txt.setOnClickListener(v12 -> {
//                    if (itemClickListener != null) {
//                        itemClickListener.onItemClick(finalI);
//                    }
//                });
//                lin_layout.addView(item);
//            }
//        }
//    }

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
        mPopupWindow.setOnDismissListener(() -> setWindowAlpa(false));
        show(rootView);

    }


    /**
     * 显示PopupWindow
     */
    private void show(View v) {
        if (mPopupWindow != null && !mPopupWindow.isShowing()) {
            mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }
        setWindowAlpa(true);
    }


    /**
     * 消失PopupWindow
     */
    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    /**
     * 动态设置Activity背景透明度
     *
     * @param isopen
     */
    public void setWindowAlpa(boolean isopen) {
        final Window window = mContext.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        ValueAnimator animator;
        if (isopen) {
            animator = ValueAnimator.ofFloat(1.0f, 0.5f);
        } else {
            animator = ValueAnimator.ofFloat(0.5f, 1.0f);
        }
        animator.setDuration(400);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            lp.alpha = alpha;
            window.setAttributes(lp);
        });
        animator.start();
    }


    /**
     * 点击事件选择回调
     */
    public interface onPopupWindowItemClickListener {
        void onItemClick(int position);
    }
}
