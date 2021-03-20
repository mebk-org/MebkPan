package com.mebk.pan.application

import android.app.Application
import com.mebk.pan.net.Repository

class MyApplication: Application() {
    val repository by lazy { Repository(applicationContext ) }
}