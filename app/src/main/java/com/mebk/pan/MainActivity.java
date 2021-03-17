package com.mebk.pan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toolbar;

import com.mebk.pan.aa.FragAdapter;
import com.mebk.pan.home.Main_farment_IMG;
import com.mebk.pan.home.Main_fragment_File;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FrameLayout frameLayout;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //将ToolBar对象设置为当前Activity的ActionBar
        List<Fragment> fragments=new ArrayList<Fragment>();
        fragments.add(new Main_fragment_File());
        fragments.add(new Main_farment_IMG());
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(), fragments);

        //设定适配器
        ViewPager vp = (ViewPager)findViewById(R.id.viewpager);
        vp.setAdapter(adapter);
    }

}