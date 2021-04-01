package com.mebk.pan;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.Scene;
import androidx.transition.TransitionManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mebk.pan.utils.LogUtil;
import com.mebk.pan.vm.MainViewModel;

import java.util.List;
import java.util.Observable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //几个代表页面的常量
    private ViewPager2 v_pager;
    RadioGroup radioGroup;
    RadioButton radioButton_file;
    RadioButton radioButton_img;
    List<Fragment> list;
    BottomNavigationView navView;
    private ConstraintLayout rootLayout;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

//        v_pager = findViewById(R.id.v_pager);
//        list = new ArrayList<>();
//        list.add(new FragmentDirectory());
//        list.add(new Main_farment_IMG());
//        FragAdapter adapter = new FragAdapter(this, list);
//        v_pager.setAdapter(adapter);
//
        initView();

        mainViewModel.isFileOperator().observe(this, item -> {
            fileOperatorAnimation(item);
        });

//
//        //动画
//        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
//        compositePageTransformer.addTransformer(new MarginPageTransformer(10));
//        compositePageTransformer.addTransformer(new TransFormer());
//        v_pager.setPageTransformer(compositePageTransformer);
//
//        //检测当前页面
//        v_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                //获取页面id并传下去
//                msetRb(position);
//                super.onPageSelected(position);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                super.onPageScrollStateChanged(state);
//            }
//        });

        navView = findViewById(R.id.nav_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

    }

    /**
     * 创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setings, menu); //通过getMenuInflater()方法得到MenuInflater对象，再调用它的inflate()方法就可以给当前活动创建菜单了，第一个参数：用于指定我们通过哪一个资源文件来创建菜单；第二个参数：用于指定我们的菜单项将添加到哪一个Menu对象当中。
        return true; // true：允许创建的菜单显示出来，false：创建的菜单将无法显示。
    }

    /**
     * 菜单的点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.list_item:
                Toast.makeText(this, "你点击了 添加！", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        return true;
    }

    private void initView() {
//        radioButton_file = findViewById(R.id.rb_file);
//        radioButton_file.setOnClickListener(this);
//        radioButton_img = findViewById(R.id.rb_img);
//        radioButton_img.setOnClickListener(this);
        navView = findViewById(R.id.nav_view);
        rootLayout = findViewById(R.id.container);
    }

    /**
     * 得到position并进行处理
     *
     * @param position
     */
    private void msetRb(int position) {
        switch (position) {
            case 0:
                //点亮图标
                radioButton_file.setChecked(true);
                break;
            case 1:
                radioButton_img.setChecked(true);
        }
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.rb_file:
//                v_pager.setCurrentItem(0);
//                break;
//            case R.id.rb_img:
//                v_pager.setCurrentItem(1);
//                break;
//        }
//    }

    private void fileOperatorAnimation(boolean isFileOperator) {
        ConstraintSet constraintSet = new ConstraintSet();
        if (isFileOperator) {
            constraintSet.load(this, R.layout.layout_file_opreator);
        } else {
            constraintSet.load(this, R.layout.activity_main);
        }
        TransitionManager.beginDelayedTransition(rootLayout);
        constraintSet.applyTo(rootLayout);
    }



}