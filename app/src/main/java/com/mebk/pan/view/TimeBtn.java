package com.mebk.pan.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.mebk.pan.R;

/**
 * @author
 * @name
 * @class name：
 * @class 倒计时控件
 * @time 2018/11/28 18:28
 * @change
 * @chang time
 * @class describe
 */
public class TimeBtn extends View {
    private Paint textPaint, circlePaint;
    private int width, height;
    int angle = 0;
    private int text = 3;
    private static final String TAG = "TimeButton";
    private Context context;

    public TimeBtn(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        textPaint = new Paint();
        circlePaint = new Paint();
        textPaint.setColor(context.getResources().getColor(R.color.half_white));
        circlePaint.setColor(context.getResources().getColor(R.color.half_white));
        circlePaint.setStrokeWidth(6);
        circlePaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(50);
        circlePaint.setAntiAlias(true);
    }

    public TimeBtn(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimeBtn(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        if (width != height) {
            if (width > height) width = height;
            else height = width;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(5, 5, width - 5, height - 5, -90, angle, false, circlePaint);
        canvas.drawText(text + "", (getRight() - getLeft()) / 2.0f, (getBottom() - getTop()) / 2.0f + textPaint.getTextSize() / 3, textPaint);
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
        invalidate();
    }

    public void startAnimation() {
        ObjectAnimator oa = ObjectAnimator.ofInt(this, "angle", 0, 360);
        oa.setDuration(1000);
        oa.setInterpolator(new LinearInterpolator());
        oa.setRepeatCount(2);
        oa.start();
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                text--;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (context instanceof WaitingAnimationEndInterface) {
                    ((WaitingAnimationEndInterface) context).waitingEnd(true);
                }
            }
        });
    }
}