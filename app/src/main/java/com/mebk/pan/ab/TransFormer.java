package com.mebk.pan.ab;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class TransFormer  implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {

        if (position >= -1.0f && position <= 0.0f) {
            //控制左侧滑入或者滑出的缩放比例
            page.setScaleX(1 + position * 0.1f);
            page.setScaleY(1 + position * 0.2f);
        } else if (position > 0.0f && position < 1.0f) {
            //控制右侧滑入或者滑出的缩放比例
            page.setScaleX(1 - position * 0.1f);
            page.setScaleY(1 - position * 0.2f);
        } else {
            //控制其他View缩放比例
            page.setScaleX(0.9f);
            page.setScaleY(0.8f);
        }
    }
}
