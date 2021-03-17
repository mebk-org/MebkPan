package com.mebk.pan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.mebk.pan.vm.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var loginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginBtn = findViewById(R.id.loginActivity_login_btn)

        loginBtn.setOnClickListener {
            viewModel.login("503779938@qq.com", "13591725936", "")
        }

        viewModel.loginInfo.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })


    }
}