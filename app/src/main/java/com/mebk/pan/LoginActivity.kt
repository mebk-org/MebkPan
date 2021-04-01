package com.mebk.pan

import android.content.Intent
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


        force = SpringForce(0f).apply {
            //设置阻尼，阻尼越大，表示弹簧振动衰减越快
            dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
            //设置物体刚度，相当于弹簧常量
            stiffness = SpringForce.STIFFNESS_LOW
        }

        loginActivity_username_et.addTextChangedListener {
            animation?.cancel()
            loginActivity_username_textInputLayout.error = null
        }
        loginActivity_pwd_et.addTextChangedListener {
            animation?.cancel()
            loginActivity_pwd_textInputLayout.error = null
        }

        loginActivity_login_btn.setOnClickListener {

            when {
                TextUtils.isEmpty(loginActivity_username_et.text.toString()) -> {
                    loginActivity_username_textInputLayout.error = "请输入邮箱"
                    errAnimation(loginActivity_username_textInputLayout)
                }
                TextUtils.isEmpty(loginActivity_pwd_et.text.toString()) -> {
                    loginActivity_pwd_textInputLayout.error = "请输入密码"
                    errAnimation(loginActivity_pwd_textInputLayout)
                }
                else -> {
                    loginActivity_login_btn.setBackgroundColor(resources.getColor(R.color.communism_clink, null))
                    loginActivity_login_btn.isClickable = false
                    loginActivity_login_btn.text = "登录中，请稍后"

                    //通过viewModel执行登录
                    viewModel.login(loginActivity_username_et.text.toString(),
                            loginActivity_pwd_et.text.toString(),
                            "")
                }
            }
        }

        //使用vm观察是否登录成功
        viewModel.loginInfo.observe(this, Observer {
            when (it) {
                LoginViewModel.LOGIN_SUCCESS -> {
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                    //TODO 传递用户信息
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else -> {
                    loginActivity_login_btn.isClickable = true
                    loginActivity_login_btn.setBackgroundColor(resources.getColor(R.color.communism, null))
                    loginActivity_login_btn.text = resources.getString(R.string.login)

                    Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    loginActivity_username_textInputLayout.error = it

                    errAnimation(loginActivity_username_textInputLayout)
                }
            }
        })


    }

    //登录出错时动画
    private fun errAnimation(view: View) {

        //2d旋转动画
        animation = SpringAnimation(view, SpringAnimation.ROTATION).setSpring(force)

        //设置控件的起始值，这里为起始角度
        animation!!.setStartValue(10f)
        animation!!.start()
    }
}