package com.picfix.tools.view.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/12/21 14:22
 */
public class MoveImageView extends View {
    private int[] parentPos = new int[2];
    private int[] selfPos = new int[2];
    private int[] selfPosStart = new int[2];

    private int mParentWidth;
    private int mWidth;

    private int lastX;
    private int lastY;

    private FrameLayout dynamicLayout;

    public MoveImageView(Context context) {
        super(context);
        init(context);
    }

    public MoveImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MoveImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        setBackgroundColor(Color.rgb(255, 125, 0));
        post(new Runnable() {
            @Override
            public void run() {
                if (mWidth == 0) {
                    mWidth = getWidth();
                }
                if (mParentWidth == 0) {
                    mParentWidth = ((View) getParent()).getWidth();
                }
                Log.e("post", mWidth + "--" + mParentWidth);
            }
        });
    }

    public void setLayout(FrameLayout view, int width) {
        dynamicLayout = view;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e("ACTION_DOWN", x + "");
                lastX = x;
                lastY = y;

                break;
            case MotionEvent.ACTION_UP:

                break;
            case MotionEvent.ACTION_MOVE:

                //计算移动的距离
                int offX = x - lastX;
                CallBean callBean = noReachParentBorder(offX);
                if (callBean.noReachBorder) {
                    int left = this.getLeft() + callBean.lastOffX;
                    coreMoveLogic(left);
                }

                break;
        }
        return true;
    }

    private void coreMoveLogic(int left) {
        int right = left + mWidth;
        if (right < mParentWidth) {

        } else {
            right = mParentWidth;
            left = mParentWidth - mWidth;
        }
        Log.e("offX", right + "--" + left);
        ((View) getParent()).scrollBy(getLeft()-left,0);

        ViewGroup.LayoutParams layoutParams = dynamicLayout.getLayoutParams();
        layoutParams.width = left;
        dynamicLayout.setLayoutParams(layoutParams);
    }

    public CallBean noReachParentBorder(int offX) {
//        ((View) getParent()).getLocationOnScreen(parentPos);
        this.getLocationOnScreen(selfPos);
        CallBean callBean = new CallBean();
        callBean.lastOffX = offX;
        Log.e("pos", selfPos[0] + "--" + selfPosStart[0] + "--" + offX + "--" + selfPos[1]+ "--" + selfPosStart[1]+ "--" +mParentWidth+ "--" +mWidth );
        if (selfPos[0]>= selfPosStart[0] &&
                selfPos[0] <= (mParentWidth-mWidth)) {
            callBean.noReachBorder = true;
            if (selfPos[0]+offX<=selfPosStart[0]){
                callBean.lastOffX = selfPosStart[0]-selfPos[0];
            }if (selfPos[0]+offX>=(mParentWidth-mWidth)){
                callBean.lastOffX =(mParentWidth-mWidth)-selfPos[0];
            }
        }else {
            callBean.noReachBorder = false;
        }


        Log.e("callBean",callBean.toString());
        return callBean;
    }

    class CallBean {
        boolean noReachBorder;
        int lastOffX;

        @Override
        public String toString() {
            return "CallBean{" +
                    "noReachBorder=" + noReachBorder +
                    ", lastOffX=" + lastOffX +
                    '}';
        }
    }

}
