package com.mebk.pan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.mebk.pan.vm.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)



        loginActivity_login_iv.setOnClickListener {
            if (TextUtils.isEmpty(loginActivity_username_et.text.toString())) {

            }
            if (TextUtils.isEmpty(loginActivity_pwd_et.text.toString())) {

            }


            viewModel.login(loginActivity_username_et.text.toString(),
                    loginActivity_pwd_et.text.toString(),
                    "")
        }

        viewModel.loginInfo.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })


    }

    //登录出错时动画
    fun errAnimation(){

    }
}