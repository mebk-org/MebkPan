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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Group;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.transition.TransitionManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.mebk.pan.database.entity.FileEntity;
import com.mebk.pan.utils.ConfigureKt;
import com.mebk.pan.utils.ToolUtilsKt;
import com.mebk.pan.vm.MainViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private CoordinatorLayout coordinatorLayout;
    private ConstraintLayout rootLayout;
    private MainViewModel mainViewModel;
    private TabLayout.Tab downloadItem, shareItem, deleteItem, moreItem;
    private FloatingActionButton menuFab, uploadFab, mkdirFab, shareFab;
    private Group uploadGroup, mkdirGroup, shareGroup;
    private PopupWindow sharePopupwindow, pwdPopupwindow, timePopupwindow;
    private TextView sharePwdTv, shareTimeTv, sharePreviewTv;
    private SwitchCompat sharePwdSwitch, shareTimeSwitch, sharePreviewSwitch;
    private Button sharePwdSureBtn, sharePwdCancelBtn, shareTimeSureBtn, shareTimeCancelBtn, shareSureBtn, shareCancelBtn;
    private TextInputLayout sharePwdTextTextInputLayout;
    private EditText sharePwdEditText;
    private ImageView sharePwdRandomIv;
    private String sharePwd = "";
    private Spinner timePopupwindowDownloadSpinner, timePopupwindowExpireSpinner;
    private ConstraintLayout shareRoot, pwdRoot, timeRoot;

    private int[] shareTimeDownloadArr, shareTimeExpireArr;
    private int shareTimeDownload, shareTimeExpire;
    private boolean isFabShow = false, isPreview = false;
    private int fabWidth;
    private int fabRadius;

    private OnBackPressedCallback callback;
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

        callback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                Log.e(TAG, "handleOnBackPressed: ");
                switch (mainViewModel.back()) {
                    case MainViewModel.POPUPWINDOW_TIME:
                        timePopupwindow.dismiss();
                        break;
                    case MainViewModel.POPUPWINDOW_SHARE:
                        sharePopupwindow.dismiss();
                        break;
                    case MainViewModel.POPUPWINDOW_PWD:
                        pwdPopupwindow.dismiss();
                        break;
                }
            }
        };

        mainViewModel.isFileOperator().observe(this, this::fileOperatorAnimation);

        mainViewModel.getCheckInfo().observe(this, item -> {
        });

        mainViewModel.getPopupwindowInfo().observe(this, item -> {
            callback.setEnabled(item != 0);
        });

        mainViewModel.getDownloadWorkerInfo().observe(this, item -> {
            if (item == null) return;

            Log.e(TAG, "onCreate: " + item.toString());
            if (item.getState().isFinished()) {
                mainViewModel.downloadDone(item.getState());
            }

        });

        mainViewModel.getShareInfo().observe(this, item -> {
            if (ConfigureKt.REQUEST_SUCCESS.equals(item.getFirst())) {
                sharePopupwindow.dismiss();
                mainViewModel.back();
                mainViewModel.actionDone();
                String res = item.getSecond() + "提取密码" + sharePwd;
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "分享成功", 10000);
                snackbar.setAction(getResources().getString(R.string.app_name), v -> {

                });
                snackbar.show();

            } else {
                Toast.makeText(this, item.getSecond(), Toast.LENGTH_SHORT).show();
            }
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);


    }

    private void onClick() {
        downloadItem.view.setOnClickListener(v -> mainViewModel.download());
        deleteItem.view.setOnClickListener(v -> mainViewModel.deleteFile());

        shareItem.view.setOnClickListener(v -> {
            if (mainViewModel.getCheckList().size() != 1) {
                Toast.makeText(this, "只能分享单个文件", Toast.LENGTH_SHORT).show();
            } else {
                sharePopupwindow.showAtLocation(rootLayout, Gravity.BOTTOM, 0, 0);
                mainViewModel.openPopupWindow(MainViewModel.POPUPWINDOW_SHARE);
                getOnBackPressedDispatcher().addCallback(this, callback);
            }
        });

        moreItem.view.setOnClickListener(v -> {
            Intent i = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("src_dir", mainViewModel.getCheckPath());
            ArrayList<String> fileIds, dirIds;
            fileIds = new ArrayList<>();
            dirIds = new ArrayList<>();
            for (FileEntity file : mainViewModel.getCheckList()) {
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

        sharePwdSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !pwdPopupwindow.isShowing()) {
                sharePwd();
            } else if (!isChecked) {
                sharePwd = "";
                sharePwdEditText.setText("");
                sharePwdTextTextInputLayout.setError(null);
            }
        });

        sharePwdTv.setOnClickListener(v -> sharePwd());

        shareTimeTv.setOnClickListener(v -> shareTime());

        shareTimeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !timePopupwindow.isShowing()) {
                shareTime();
            } else if (!isChecked) {
                shareTimeDownload = -1;
                shareTimeExpire = -1;
                timePopupwindowDownloadSpinner.setSelection(0);
                timePopupwindowExpireSpinner.setSelection(0);
            }
        });

        sharePwdSureBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(sharePwdEditText.getText().toString())) {
                sharePwdTextTextInputLayout.setError("请设置分享密码");
            } else {
                sharePwdTextTextInputLayout.setError(null);
                sharePwd = sharePwdEditText.getText().toString();
                sharePwd = "";
                sharePwdSwitch.setChecked(true);
                pwdPopupwindow.dismiss();
                mainViewModel.back();
            }
        });

        sharePwdCancelBtn.setOnClickListener(v -> {
            sharePwd = "";
            sharePwdEditText.setText("");
            sharePwdSwitch.setChecked(false);
            pwdPopupwindow.dismiss();
            mainViewModel.back();
        });

        sharePwdRandomIv.setOnClickListener(v -> {
            String pwd = ToolUtilsKt.sharePwdGenerator();
            sharePwdEditText.setText(pwd);
        });

        timePopupwindowExpireSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shareTimeExpire = shareTimeExpireArr[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                shareTimeExpire = shareTimeExpireArr[0];
            }
        });

        timePopupwindowDownloadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shareTimeDownload = shareTimeDownloadArr[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                shareTimeDownload = shareTimeDownloadArr[0];
            }
        });

        shareTimeSureBtn.setOnClickListener(v -> {
            shareTimeSwitch.setChecked(true);
            timePopupwindow.dismiss();
            mainViewModel.back();
        });

        sharePreviewSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> isPreview = isChecked);

        shareSureBtn.setOnClickListener(v -> {
            FileEntity file = mainViewModel.getCheckList().get(0);
            String id = file.getId();
            boolean isDir = !file.getType().equals("file");
            mainViewModel.shareFile(id, isDir, sharePwd, shareTimeDownload, shareTimeExpire, isPreview, 0);
        });

        shareRoot.setOnClickListener(v -> {
            sharePopupwindow.dismiss();
            mainViewModel.back();
        });

        timeRoot.setOnClickListener(v -> {
            timePopupwindow.dismiss();
            mainViewModel.back();
        });

        pwdRoot.setOnClickListener(v -> {
            pwdPopupwindow.dismiss();
            mainViewModel.back();
        });


        shareTimeCancelBtn.setOnClickListener(v -> {
            shareTimeDownload = -1;
            shareTimeExpire = -1;
            shareTimeSwitch.setChecked(false);
            timePopupwindow.dismiss();
            mainViewModel.back();
        });

        shareCancelBtn.setOnClickListener(v -> {

            shareTimeSwitch.setChecked(false);
            sharePwdSwitch.setChecked(false);
            sharePreviewSwitch.setChecked(false);

            sharePopupwindow.dismiss();
            mainViewModel.back();
        });
    }

    private void sharePwd() {
        pwdPopupwindow.setFocusable(true);
        pwdPopupwindow.update();
        pwdPopupwindow.showAtLocation(rootLayout, Gravity.BOTTOM, 0, 0);
        mainViewModel.openPopupWindow(MainViewModel.POPUPWINDOW_PWD);
    }

    public void shareTime() {
        timePopupwindow.showAtLocation(rootLayout, Gravity.BOTTOM, 0, 0);
        mainViewModel.openPopupWindow(MainViewModel.POPUPWINDOW_TIME);
    }

    private void initView() {
        coordinatorLayout = findViewById(R.id.coordinator);
        shareTimeDownloadArr = new int[]{-1, 1, 5, 10, 30, 50, 100};
        shareTimeExpireArr = new int[]{-1, 300, 3600, 43200, 86400, 604800, 1296000, 2592000};

        bottomNavigationView = findViewById(R.id.nav_bottom_view);
        rootLayout = findViewById(R.id.container);

        View sharePopupWindowLayout = LayoutInflater.from(this).inflate(R.layout.popupwindow_share, null);
        View pwdPopupWindowLayout = LayoutInflater.from(this).inflate(R.layout.popupwindow_share_pwd, null);
        View timePopupwindowLayout = LayoutInflater.from(this).inflate(R.layout.popupwindow_share_time, null);


        shareRoot = sharePopupWindowLayout.findViewById(R.id.popupwindow_share_root);
        sharePwdTv = sharePopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_tv);
        sharePwdSwitch = sharePopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_switch);
        shareTimeTv = sharePopupWindowLayout.findViewById(R.id.popupwindow_share_time_tv);
        shareTimeSwitch = sharePopupWindowLayout.findViewById(R.id.popupwindow_share_time_switch);
        sharePreviewSwitch = sharePopupWindowLayout.findViewById(R.id.popupwindow_share_preview_switch);
        shareSureBtn = sharePopupWindowLayout.findViewById(R.id.popupwindow_share_sure_btn);
        shareCancelBtn = sharePopupWindowLayout.findViewById(R.id.popupwindow_share_cancel_btn);

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
        timePopupwindow = new PopupWindow(timePopupwindowLayout, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);

        pwdRoot = pwdPopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_root);
        sharePwdSureBtn = pwdPopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_sure_btn);
        sharePwdCancelBtn = pwdPopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_cancel_btn);

        timeRoot = timePopupwindowLayout.findViewById(R.id.popupwindow_share_time_root);
        shareTimeCancelBtn = timePopupwindowLayout.findViewById(R.id.popupwindow_share_time_cancel_btn);
        shareTimeSureBtn = timePopupwindowLayout.findViewById(R.id.popupwindow_share_time_sure_btn);

        sharePwdTextTextInputLayout = pwdPopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_textInputLayout);
        sharePwdEditText = pwdPopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_et);
        sharePwdRandomIv = pwdPopupWindowLayout.findViewById(R.id.popupwindow_share_pwd_random_iv);

        timePopupwindowDownloadSpinner = timePopupwindowLayout.findViewById(R.id.popupwindow_share_time_download_spinner);
        timePopupwindowExpireSpinner = timePopupwindowLayout.findViewById(R.id.popupwindow_share_time_expire_spinner);
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