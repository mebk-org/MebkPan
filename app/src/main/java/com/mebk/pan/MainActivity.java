package com.mebk.pan;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.TransitionManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.mebk.pan.database.entity.File;
import com.mebk.pan.utils.ToolUtilsKt;
import com.mebk.pan.vm.MainViewModel;

import java.util.ArrayList;

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
    private NavHostFragment navHostFragment;
    private View sharePopupWindowLayout, pwdPopupWindowLayout;
    private PopupWindow sharePopupwindow, pwdPopupwindow;
    private TextView sharePwdTv;
    private SwitchCompat sharePwdSwitch;
    private Button sharePwdSureBtn, getSharePwdCancelBtn;
    private TextInputLayout sharePwdTextTextInputLayout;
    private EditText sharePwdEditText;
    public static final int MOVE_CODE = 1;
    private String sharePwd;
    private final ActivityResultLauncher<Intent> moveResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                mainViewModel.actionDone();
            }
        }
    });

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

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
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

        shareItem.view.setOnClickListener(v -> {
            if (mainViewModel.getCheckList().size() != 1) {
                Toast.makeText(this, "只能分享单个文件", Toast.LENGTH_SHORT).show();
            } else {
                sharePopupwindow.showAtLocation(rootLayout, Gravity.BOTTOM, 0, 0);

//                File file = mainViewModel.getCheckList().get(0);
//                String id = file.getId();
//                boolean isDir = !file.getType().equals("file");
//                mainViewModel.shareFile(id, isDir, "", -1, 86400, true, 0);
            }
        });

        moreItem.view.setOnClickListener(v -> {
            Intent i = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("src_dir", mainViewModel.getCheckPath());
            ArrayList<String> fileIds, dirIds;
            fileIds = new ArrayList<>();
            dirIds = new ArrayList<>();
            for (File file : mainViewModel.getCheckList()) {
                if (file.getType().equals("file")) {
                    fileIds.add(file.getId());
                } else {
                    dirIds.add(file.getId());
                }
            }
            Log.e(TAG, "onClick: " + mainViewModel.getCheckList().toString());
            bundle.putStringArrayList("fileList", fileIds);
            bundle.putStringArrayList("dirList", dirIds);
            i.putExtra("bundle", bundle);
            i.setClass(this, DirActivity.class);
            moveResult.launch(i);
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

        sharePwdSwitch.setOnClickListener(v -> {
            sharePwd();
        });

        sharePwdTv.setOnClickListener(v -> {
            sharePwd();
        });

        sharePwdSureBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(sharePwdEditText.getText().toString())) {
                sharePwdTextTextInputLayout.setError("请设置分享密码");
            } else {
                sharePwdTextTextInputLayout.setError(null);
                sharePwd = sharePwdEditText.getText().toString();
                pwdPopupwindow.dismiss();
                Log.e(TAG, "onClick: sharePwd=" + sharePwd);
            }
        });

    }

    private void sharePwd() {
        pwdPopupwindow.setFocusable(true);
        pwdPopupwindow.update();
        pwdPopupwindow.showAtLocation(rootLayout, Gravity.BOTTOM, 0, 0);
    }


    private void initView() {
        bottomNavigationView = findViewById(R.id.nav_bottom_view);
        rootLayout = findViewById(R.id.container);

        sharePopupWindowLayout = LayoutInflater.from(this).inflate(R.layout.popupwindow_share, null);
        pwdPopupWindowLayout = LayoutInflater.from(this).inflate(R.layout.popupwindow_share_pwd, null);

        sharePwdTv = sharePopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_tv);
        sharePwdSwitch = sharePopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_switch);

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

        sharePopupwindow = new PopupWindow(sharePopupWindowLayout, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
        pwdPopupwindow = new PopupWindow(pwdPopupWindowLayout, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);

        sharePwdSureBtn = pwdPopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_sure_btn);
        getSharePwdCancelBtn = pwdPopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_cancel_btn);

        sharePwdTextTextInputLayout = pwdPopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_textInputLayout);
        sharePwdEditText = pwdPopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_et);
    }


    /**
     * 文件选择动画
     *
     * @param isFileOperator 是否进入文件选择页面
     */
    private void fileOperatorAnimation(boolean isFileOperator) {
        Log.e(TAG, "fileOperatorAnimation: " + isFileOperator);
        ConstraintSet constraintSet = new ConstraintSet();
        if (isFileOperator) {
            constraintSet.load(this, R.layout.layout_file_opreator);
        } else {
            constraintSet.load(this, R.layout.activity_main);
        }
        TransitionManager.beginDelayedTransition(rootLayout);
        constraintSet.applyTo(rootLayout);
    }

    /**
     * 上传fab动画
     *
     * @param fab   需要执行动画的fab
     * @param group 控制当前fab可见性的group
     * @return ValueAnimator 动画
     */
    private ValueAnimator setValueAnimator(FloatingActionButton fab, Group group) {
        ValueAnimator animator;
        if (isFabShow) {
            animator = ValueAnimator.ofFloat(0f, 1f);
        } else {
            animator = ValueAnimator.ofFloat(1f, 0f);
        }
        animator.setDuration(300);
        ConstraintLayout.LayoutParams parms = (ConstraintLayout.LayoutParams) fab.getLayoutParams();

        animator.addUpdateListener(animation -> {
            float v = (float) animation.getAnimatedValue();
            parms.width = (int) (fabWidth * v);

            Log.e(TAG, "setValueAnimator: parmswidth=" + parms.circleRadius);
            parms.height = (int) (fabWidth * v);
            parms.circleRadius = (int) (fabRadius * v);
            fab.setLayoutParams(parms);
        });
        animator.setInterpolator(new AccelerateInterpolator());
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