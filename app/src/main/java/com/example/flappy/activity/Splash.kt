package com.example.flappy.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.flappy.R
import com.example.flappy.mvvm.AppwriteViewModel
import com.example.flappy.mvvm.appwriteModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.startKoin

class Splash : AppCompatActivity() {

    private val appwriteViewModel: AppwriteViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startKoin {
            androidContext(this@Splash)
            modules(listOf(appwriteModule))
        }

        appwriteViewModel.getSession()
        appwriteViewModel.session.observe(this) {
            if (it == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                appwriteViewModel.getAccount()
            }
        }
        appwriteViewModel.user.observe(this) {
            if (it != null) {
                startActivity(Intent(this, HomeActivity::class.java))
                Log.e("splash", "User id: ${it.id}")
                finish()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}