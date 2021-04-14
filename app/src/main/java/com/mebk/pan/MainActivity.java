package com.mebk.pan;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
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
import androidx.work.WorkInfo;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.mebk.pan.aa.ExpandableListviewAdapter;


import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.mebk.pan.dtos.DirectoryDto;
import com.mebk.pan.utils.LogUtil;
import com.mebk.pan.utils.RetrofitClient;
import com.mebk.pan.vm.MainViewModel;

import java.util.Dictionary;

import java.util.List;
import java.util.Observable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    //数据
    private String[] data = {
           "我的分享","离线下载","容量配额","任务队列"
    };
    //左侧菜单栏
    private ExpandableListView expand_list_id;
    ListView listView;

    //几个代表页面的常量

    RadioButton radioButton_file;
    RadioButton radioButton_img;
    BottomNavigationView navView;
    ImageView imageView;
    DrawerLayout drawer_layout;
    private ConstraintLayout rootLayout;
    private MainViewModel mainViewModel;
    private TabLayout.Tab downloadItem, shareItem, deleteItem, moreItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       //组件初始化
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        views();


        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initView();

        onClick();

        mainViewModel.isFileOperator().observe(this, item -> {
            fileOperatorAnimation(item);
        });

        mainViewModel.getCheckInfo().observe(this, item -> {
            for (DirectoryDto.Object obj : item) {
                Log.e(TAG, "onCreate: " + obj.toString());
            }
        });

        mainViewModel.getDownloadWorkerInfo().observe(this, item -> {
            if (item == null) return;

            Log.e(TAG, "onCreate: " + item.toString());
            if (item.getState().isFinished()) {
                mainViewModel.downloadDone(item.getState());
            }

        });
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);


        ExpandableListviewAdapter adapter = new ExpandableListviewAdapter();
        expand_list_id.setAdapter(adapter);
        //默认展开第一个数组
        expand_list_id.expandGroup(0);

        ArrayAdapter<String> list_adapter = new ArrayAdapter<String>(this ,
                android.R.layout.simple_list_item_1, data );//适配器
        listView.setAdapter(list_adapter);//添加适配器

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                drawer_layout.open();
            }
        });
    }

    private void views() {
        navView=findViewById(R.id.nav_view);
        drawer_layout=findViewById(R.id.drawer_layout);
        expand_list_id =findViewById(R.id.expand_list_id_dynamic);
        listView = findViewById(R.id.list_item_main);//获取组件对象
        imageView=findViewById(R.id.header_title);
    }

    private void onClick() {
        downloadItem.view.setOnClickListener(v -> {
            mainViewModel.download();
        });

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
        navView = findViewById(R.id.nav_view);
        rootLayout = findViewById(R.id.container);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        downloadItem = tabLayout.getTabAt(0);
        shareItem = tabLayout.getTabAt(1);
        deleteItem = tabLayout.getTabAt(2);
        moreItem = tabLayout.getTabAt(3);
        navView = findViewById(R.id.nav_view);
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