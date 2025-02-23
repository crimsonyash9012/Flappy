package com.example.twitter.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.twitter.databinding.ActivityLoginBinding
import com.example.twitter.mvvm.AppwriteViewModel
import com.example.twitter.utils.Constant
import com.google.android.material.textfield.TextInputLayout

import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private val appwriteViewModel: AppwriteViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loginBinding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(loginBinding.root)

        loginBinding.loginProgressLayout.setOnTouchListener { view, motionEvent -> true }

        setTextChangeListener(loginBinding.emailET, loginBinding.emailTIL)
        setTextChangeListener(loginBinding.passwordET, loginBinding.passwordTIL)

        loginBinding.buttonLogin.setOnClickListener {
            var proceed = true
            if (loginBinding.emailET.text.isNullOrEmpty()) {
                loginBinding.emailTIL.error = "Email is required"
                loginBinding.emailTIL.isErrorEnabled = true
                proceed = false
            }
            if (loginBinding.passwordET.text.isNullOrEmpty()) {
                loginBinding.passwordTIL.error = "Password is required"
                loginBinding.passwordTIL.isErrorEnabled = true
                proceed = false

            }

            if (proceed) {
                loginBinding.loginProgressLayout.visibility = View.VISIBLE
                val email = loginBinding.emailET.text.toString()
                val password = loginBinding.passwordET.text.toString()

                try {
                    appwriteViewModel.login(email, password,
                        onError = {e ->
                            appwriteViewModel.login_error.observe(this){error ->
                                if(error!=null) {
                                    if (error!!.contains(Constant.ERROR_INVALID_EMAIL, ignoreCase = true)) {
                                        loginBinding.emailTIL.error = "Please enter a valid email"
                                        loginBinding.emailTIL.isErrorEnabled = true
                                    } else if (error!!.contains(
                                            Constant.ERROR_INVALID_PASS,
                                            ignoreCase = true
                                        )
                                    ) {
                                        loginBinding.passwordTIL.error =
                                            "Password must be between 8 and 256 characters long"
                                        loginBinding.passwordTIL.isErrorEnabled = true
                                    } else if (error!!.contains(
                                            Constant.ERROR_INVALID_CRED,
                                            ignoreCase = true
                                        )
                                    ) {
                                        loginBinding.emailTIL.error = "Invalid credentials"
                                        loginBinding.emailTIL.isErrorEnabled = true
                                        loginBinding.passwordTIL.error = "Invalid credentials"
                                        loginBinding.passwordTIL.isErrorEnabled = true
                                    }
                                }
                            }
                        })
                    loginBinding.loginProgressLayout.visibility = View.GONE
                } catch (e: Exception) {

                    Log.e("login", e.message.toString())
                    loginBinding.loginProgressLayout.visibility = View.GONE
                }
            }
        }

        appwriteViewModel.session.observe(this) {
            if (it == null) {
                if (loginBinding.emailET.text.toString()
                        .isNotEmpty() || loginBinding.passwordET.text.toString().isNotEmpty()
                ) {
                    Toast.makeText(applicationContext, "Login failed", Toast.LENGTH_SHORT).show()
                    return@observe
                }
            } else {

                val session = it.userId

                appwriteViewModel.getAccount()
                appwriteViewModel.user.observe(this) { user ->
                    if (user != null) {
                        Toast.makeText(applicationContext, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Fetching user data failed", Toast.LENGTH_SHORT).show()
                    }
                }
                /*
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                 */
            }
        }

        /*
        appwriteViewModel.user.observe(this) {
            if (it == null) {
                Log.e("login", "user is null")
                return@observe
            } else {
                Log.e("login", "user is not null")
                appwriteViewModel.getAccount()
                finish()
            }
        }
         */
    }


    fun goToSignup(v: View) {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
        finish()
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
    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}