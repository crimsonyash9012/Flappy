package com.example.twitter.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.twitter.databinding.ActivitySignupBinding
import com.example.twitter.mvvm.AppwriteViewModel
import com.example.twitter.utils.Constant
import com.example.twitter.utils.User
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignupActivity : AppCompatActivity() {

    private val appwriteViewModel: AppwriteViewModel by viewModel()

    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val signupBinding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(signupBinding.root)

        setTextChangeListener(signupBinding.emailET, signupBinding.emailTIL)
        setTextChangeListener(signupBinding.passwordET, signupBinding.passwordTIL)

        signupBinding.buttonSignup.setOnClickListener {

            var proceed = true
            if (signupBinding.usernameET.text.isNullOrEmpty()) {
                signupBinding.usernameTIL.error = "Username is required"
                signupBinding.usernameTIL.isErrorEnabled = true
                proceed = false
            }
            if (signupBinding.emailET.text.isNullOrEmpty()) {
                signupBinding.emailTIL.error = "Email is required"
                signupBinding.emailTIL.isErrorEnabled = true
                proceed = false
            }
            if (signupBinding.passwordET.text.isNullOrEmpty()) {
                signupBinding.passwordTIL.error = "Password is required"
                signupBinding.passwordTIL.isErrorEnabled = true
                proceed = false
            }

            if (proceed) {
                val name = signupBinding.usernameET.text.toString()
                val email = signupBinding.emailET.text.toString()
                val password = signupBinding.passwordET.text.toString()
                val curUser = User(
                    user_Id = appwriteViewModel.userId,
                    email = email,
                    name = name,
                    imageUrl = "",
                    followHashtags = arrayOf(),
                    followUsers = arrayOf()
                )
                appwriteViewModel.createAccount(email,
                    password,
                    name,
                    onSuccess = {
                        appwriteViewModel.addUser(curUser)
                    },
                    onError = {e ->
                        appwriteViewModel.login_error.observe(this) { error ->
                            if (error!!.contains(Constant.ERROR_INVALID_EMAIL, ignoreCase = true)) {
                                signupBinding.emailTIL.error = "Please enter a valid email"
                                signupBinding.emailTIL.isErrorEnabled = true
                            } else if (error!!.contains(Constant.ERROR_INVALID_PASS, ignoreCase = true)) {
                                signupBinding.passwordTIL.error =
                                    "Password must be between 8 and 256 characters long"
                                signupBinding.passwordTIL.isErrorEnabled = true
                            } else if (error!!.contains(Constant.ERROR_INVALID_CRED, ignoreCase = true)) {
                                signupBinding.emailTIL.error = "Invalid credentials"
                                signupBinding.emailTIL.isErrorEnabled = true
                                signupBinding.passwordTIL.error = "Invalid credentials"
                                signupBinding.passwordTIL.isErrorEnabled = true
                            }
                            else if (error!!.contains(Constant.ERROR_SAME_CRED, ignoreCase = true)) {
                                signupBinding.emailTIL.error = "A user with the same email already exists"
                                signupBinding.emailTIL.isErrorEnabled = true
                            }
                            else if (error!!.contains(Constant.ERROR_TRY_AGAIN, ignoreCase = true)) {
                                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                finish()
                                Toast.makeText(
                                    this,
                                    Constant.ERROR_TRY_AGAIN,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    })
            }
        }

        appwriteViewModel.user.observe(this) {
            if (it != null) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
                Toast.makeText(applicationContext, "Account created", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Account not created", Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun goToLogin(v: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }

    private fun setTextChangeListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled = false
            }

        })
    }

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(applicationContext, "Press back again to exit", Toast.LENGTH_SHORT)
                .show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}