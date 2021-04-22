package com.mebk.pan;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.Scene;
import androidx.transition.TransitionManager;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.WorkInfo;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.mebk.pan.dtos.DirectoryDto;
import com.mebk.pan.utils.LogUtil;
import com.mebk.pan.utils.RetrofitClient;
import com.mebk.pan.utils.ToolUtilsKt;
import com.mebk.pan.vm.MainViewModel;

import java.util.Dictionary;
import java.util.List;
import java.util.Observable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //几个代表页面的常量
    RadioButton radioButton_file;
    RadioButton radioButton_img;
    BottomNavigationView bottomNavigationView;
    private ConstraintLayout rootLayout;
    private MainViewModel mainViewModel;
    private TabLayout.Tab downloadItem, shareItem, deleteItem, moreItem;
    private FloatingActionButton menuFab, uploadFab, mkdirFab, shareFab;
    private Group uploadGroup, mkdirGroup, shareGroup;
    private boolean isFabShow = false;
    private int fabWidth;
    private int fabRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        fabRadius = ToolUtilsKt.dp2px(this, 80);
        initView();

        onClick();

        mainViewModel.isFileOperator().observe(this, this::fileOperatorAnimation);

        mainViewModel.getCheckInfo().observe(this, item -> {
        });

        mainViewModel.getDownloadWorkerInfo().observe(this, item -> {
            if (item == null) return;

            Log.e(TAG, "onCreate: " + item.toString());
            if (item.getState().isFinished()) {
                mainViewModel.downloadDone(item.getState());
            }

        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

    }

    private void onClick() {
        downloadItem.view.setOnClickListener(v -> {
            mainViewModel.download();
        });
        deleteItem.view.setOnClickListener(v -> {
            mainViewModel.deleteFile();
        });

        menuFab.setOnClickListener(v -> {
            isFabShow = !isFabShow;
            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator uploadAnimator = setValueAnimator(uploadFab, uploadGroup);
            ValueAnimator shareAnimator = setValueAnimator(shareFab, shareGroup);
            ValueAnimator mkdirAnimator = setValueAnimator(mkdirFab, mkdirGroup);

            animatorSet.playSequentially(uploadAnimator, shareAnimator, mkdirAnimator);
            animatorSet.start();
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
        bottomNavigationView = findViewById(R.id.nav_bottom_view);
        rootLayout = findViewById(R.id.container);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        downloadItem = tabLayout.getTabAt(0);
        shareItem = tabLayout.getTabAt(1);
        deleteItem = tabLayout.getTabAt(2);
        moreItem = tabLayout.getTabAt(3);

        menuFab = findViewById(R.id.fab_menu);
        uploadFab = findViewById(R.id.fab_upload_file);
        shareFab = findViewById(R.id.fab_share);
        mkdirFab = findViewById(R.id.fab_mkdir);

        uploadGroup = findViewById(R.id.gp_upload);
        shareGroup = findViewById(R.id.gp_share);
        mkdirGroup = findViewById(R.id.gp_mkdir);
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

    private ValueAnimator setValueAnimator(FloatingActionButton fab, Group group) {
        ValueAnimator animator;
        if (isFabShow) {
            animator = ValueAnimator.ofFloat(0f, 1f);
        } else {
            animator = ValueAnimator.ofFloat(1f, 0f);
        }
        animator.setDuration(500);
        ConstraintLayout.LayoutParams parms = (ConstraintLayout.LayoutParams) fab.getLayoutParams();

        animator.addUpdateListener(animation -> {
            float v = (float) animation.getAnimatedValue();
            parms.width = (int) (fabWidth * v);

            Log.e(TAG, "setValueAnimator: parmswidth=" + parms.circleRadius);
            parms.height = (int) (fabWidth * v);
            parms.circleRadius = (int) (fabRadius * v);
            fab.setLayoutParams(parms);
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (isFabShow) {
                    group.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isFabShow) {
                    group.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        return animator;
    }

    @Override
    protected void onResume() {
        super.onResume();
        menuFab.post(() -> fabWidth = menuFab.getMeasuredWidth());
    }
}