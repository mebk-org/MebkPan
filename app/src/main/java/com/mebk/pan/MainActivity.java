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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mebk.pan.aa.FragAdapter;
import com.mebk.pan.ab.TransFormer;
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
                //获取页面id并传下去
                msetRb(position);
                super.onPageSelected(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

    }
    /**
     *创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setings,menu); //通过getMenuInflater()方法得到MenuInflater对象，再调用它的inflate()方法就可以给当前活动创建菜单了，第一个参数：用于指定我们通过哪一个资源文件来创建菜单；第二个参数：用于指定我们的菜单项将添加到哪一个Menu对象当中。
        return true; // true：允许创建的菜单显示出来，false：创建的菜单将无法显示。
    }

    /**
     *菜单的点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.list_item:
                Toast.makeText(this, "你点击了 添加！", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        return true;
    }
    private void inti() {
        radioButton_file=findViewById(R.id.rb_file);
        radioButton_file.setOnClickListener(this);
        radioButton_img=findViewById(R.id.rb_img);
        radioButton_img.setOnClickListener(this);

    }

    /**
     * 得到position并进行处理
     * @param position
     */
    private void msetRb(int position) {
        switch (position)
        {
            case 0:
                //点亮图标
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
                break;
        }
    }
}