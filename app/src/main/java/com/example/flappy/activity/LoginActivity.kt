package com.example.flappy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappy.databinding.ActivityLoginBinding
import com.example.flappy.mvvm.AppwriteViewModel
import com.example.flappy.utils.Constant
import com.google.android.material.textfield.TextInputLayout

import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private val appwriteViewModel: AppwriteViewModel by viewModel()
    private lateinit var loginBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        loginBinding.loginProgressLayout.setOnTouchListener { _, _ -> true }

        setTextChangeListener(loginBinding.emailET, loginBinding.emailTIL)
        setTextChangeListener(loginBinding.passwordET, loginBinding.passwordTIL)

        loginBinding.buttonLogin.setOnClickListener {
            if (validateInputs()) {
                val email = loginBinding.emailET.text.toString()
                val password = loginBinding.passwordET.text.toString()

                showLoading(true)

                appwriteViewModel.login(email, password,
                    onError = { e ->
                        showLoading(false)
                        handleLoginError(appwriteViewModel.login_error.value ?: "")
                    }
                )
            }
        }

        observeSession()
    }

    private fun validateInputs(): Boolean {
        var valid = true
        val email = loginBinding.emailET.text.toString()
        val password = loginBinding.passwordET.text.toString()

        if (email.isEmpty()) {
            loginBinding.emailTIL.error = "Email is required"
            valid = false
        }

        if (password.isEmpty()) {
            loginBinding.passwordTIL.error = "Password is required"
            valid = false
        }

        return valid
    }

    private fun handleLoginError(error: String) {
        when {
            error.contains(Constant.ERROR_INVALID_EMAIL, ignoreCase = true) -> {
                loginBinding.emailTIL.error = "Please enter a valid email"
            }
            error.contains(Constant.ERROR_INVALID_PASS, ignoreCase = true) -> {
                loginBinding.passwordTIL.error = "Password must be between 8 and 256 characters long"
            }
            error.contains(Constant.ERROR_INVALID_CRED, ignoreCase = true) -> {
                loginBinding.emailTIL.error = "Invalid credentials"
                loginBinding.passwordTIL.error = "Invalid credentials"
            }
            else -> {
                Toast.makeText(this, "Login failed: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeSession() {
        appwriteViewModel.session.observe(this) { session ->
            if (session != null) {
                appwriteViewModel.getAccount()
                appwriteViewModel.getUser("")
                appwriteViewModel.user.observe(this) { user ->
                    showLoading(false)
                    if (user != null) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        loginBinding.loginProgressLayout.visibility = if (loading) View.VISIBLE else View.GONE
        loginBinding.buttonLogin.isEnabled = !loading
    }

    fun goToSignup(v: View) {
        startActivity(Intent(this, SignupActivity::class.java))
        finish()
    }

    private fun setTextChangeListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled = false
            }
        })
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}
