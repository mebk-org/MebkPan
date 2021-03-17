package com.mebk.pan

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.lifecycle.Observer
import com.mebk.pan.vm.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel>()
    private var force: SpringForce? = null
    private var animation: SpringAnimation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        loginActivity_username_et.addTextChangedListener {
            animation?.cancel()
            loginActivity_username_textInputLayout.error = null
        }
        loginActivity_pwd_et.addTextChangedListener {
            animation?.cancel()
            loginActivity_pwd_textInputLayout.error = null
        }

        loginActivity_login_iv.setOnClickListener {
            if (TextUtils.isEmpty(loginActivity_username_et.text.toString())) {
                loginActivity_username_textInputLayout.error = "请输入邮箱"
                errAnimation(loginActivity_username_textInputLayout)
            } else if (TextUtils.isEmpty(loginActivity_pwd_et.text.toString())) {
                loginActivity_pwd_textInputLayout.error = "请输入密码"
                errAnimation(loginActivity_pwd_textInputLayout)
            } else {
                //通过viewModel执行登录
                viewModel.login(loginActivity_username_et.text.toString(),
                        loginActivity_pwd_et.text.toString(),
                        "")
            }
        }

        //使用vm观察是否登录成功
        viewModel.loginInfo.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })


    }

    //登录出错时动画
    private fun errAnimation(view: View) {
        force = SpringForce(0f).apply {
            //设置阻尼，阻尼越大，表示弹簧振动衰减越快
            dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
            //设置物体刚度，相当于弹簧常量
            stiffness = SpringForce.STIFFNESS_LOW
        }
        //2d旋转动画
        animation = SpringAnimation(view, SpringAnimation.ROTATION).setSpring(force)

        //设置控件的起始值，这里为起始角度
        animation!!.setStartValue(10f)
        animation!!.start()
    }
}