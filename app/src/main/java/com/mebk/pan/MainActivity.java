package com.mebk.pan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mebk.pan.aa.FragAdapter;
import com.mebk.pan.home.Main_farment_IMG;
import com.mebk.pan.home.Main_fragment_File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //几个代表页面的常量
    private ViewPager2 v_pager;
    RadioGroup radioGroup;
    RadioButton radioButton_file;
    RadioButton radioButton_img;
    List<Fragment> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        v_pager = findViewById(R.id.v_pager);
         list= new ArrayList<>();
        list.add(new Main_fragment_File());
        list.add(new Main_farment_IMG());
        FragAdapter adapter=new FragAdapter(this,list);
        v_pager.setAdapter(adapter);
        inti();
        //动画
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(10));
        compositePageTransformer.addTransformer(new TransFormer());
        v_pager.setPageTransformer(compositePageTransformer);
//检测当前页面
        v_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
            @Override
            public void onPageSelected(int position) {
                //获取页面id
                msetRb(position);
                super.onPageSelected(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

    }

    private void inti() {
        radioButton_file=findViewById(R.id.rb_file);
        radioButton_file.setOnClickListener(this);
        radioButton_img=findViewById(R.id.rb_img);
        radioButton_img.setOnClickListener(this);
    }

    private void msetRb(int position) {
        switch (position)
        {
            case 0:
                radioButton_file.setChecked(true);
                break;
            case  1:
                radioButton_img.setChecked(true);
        }
    }
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case  R.id.rb_file :
                    v_pager.setCurrentItem(0);
            break;
            case  R.id.rb_img :
                v_pager.setCurrentItem(1);
        }
    }

    class TransFormer implements ViewPager2.PageTransformer {

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

}