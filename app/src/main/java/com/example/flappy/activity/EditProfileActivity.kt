package com.example.flappy.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flappy.R
import com.example.flappy.databinding.ActivityEditProfileBinding
import com.example.flappy.mvvm.AppwriteViewModel
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileActivity : AppCompatActivity() {
    lateinit var btnUpdate: Button
    lateinit var etUsername: EditText
    lateinit var etPhone: EditText
    lateinit var etAbout: EditText

    private val appwriteViewModel: AppwriteViewModel by viewModel()
    private val editBinding: ActivityEditProfileBinding by lazy {
        ActivityEditProfileBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(editBinding.root)

        supportActionBar?.title = "Edit Your Profile"
        Log.e("edit profile", "on create")

        btnUpdate = findViewById(R.id.btnUpdate)
        etUsername = findViewById(R.id.etUsername)
        etPhone = findViewById(R.id.etPhone)
        etAbout = findViewById(R.id.etAbout)

        setTextChangeListener(editBinding.etUsername, editBinding.etUsernameTIL)
        setTextChangeListener(editBinding.etPhone, editBinding.etPhoneTIL)
        setTextChangeListener(editBinding.etAbout, editBinding.etAboutTIL)

        etUsername.setText(intent.getStringExtra("username"))
        etPhone.setText(intent.getStringExtra("mobile"))
        etAbout.setText(intent.getStringExtra("about"))


        btnUpdate.setOnClickListener {

            val username = etUsername.text.toString()
            val phone = etPhone.text.toString()
            val about = etAbout.text.toString()

            var proceed = true
            if (etUsername.text.isNullOrEmpty()) {
                editBinding.etUsernameTIL.error = "Username is required"
                editBinding.etUsernameTIL.isErrorEnabled = true
                proceed = false
            }

            if (!isSingleWordValid(etUsername.text.toString(), 11)) {
                editBinding.etUsernameTIL.error = "Invalid Username"
                editBinding.etUsernameTIL.isErrorEnabled = true
                proceed = false
            }

            if (!isSingleWordValid(etPhone.text.toString(), 13)) {
                editBinding.etPhoneTIL.error = "Invalid Phone Number"
                editBinding.etPhoneTIL.isErrorEnabled = true
                proceed = false
            }

            if (!isValidPhoneNumber(etPhone.text.toString())) {
                editBinding.etPhoneTIL.error =
                    "Phone Number should contain country code and Number together"
                editBinding.etPhoneTIL.isErrorEnabled = true
                proceed = false
            }

            if (!isWordCountValid(etAbout.text.toString(), 150)) {
                editBinding.etAboutTIL.error = "About should be less than 150 words"
                editBinding.etAboutTIL.isErrorEnabled = true
                proceed = false
            }

            if (proceed) {
                appwriteViewModel.updateProfileSuccess.removeObservers(this)

                appwriteViewModel.updateProfileInfo(username,
                    phone,
                    about,
                    onSuccess = {
                        appwriteViewModel.updateProfileSuccess.observe(this) { success ->
                            if (success) {
                                Toast.makeText(
                                    this,
                                    "Updated your profile successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    },
                    onError = { e ->
                        Log.e("update profile", e.message.toString())
                    })
            }
        }

    }

    private fun setTextChangeListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled = false
            }

        })
    }

    private fun isSingleWordValid(input: String, maxLetters: Int): Boolean {
        return !input.contains(" ") && input.length <= maxLetters
    }

    private fun isWordCountValid(input: String, maxWords: Int): Boolean {
        val wordCount = input.trim().split("\\s+".toRegex()).size
        return wordCount <= maxWords
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val ignoredValues = listOf("Not Added", "NA", "na", "not added")
        if (phoneNumber in ignoredValues) return true

        val phoneRegex = "^\\+\\d{1,3}\\d{7,15}$".toRegex()
        return phoneNumber.matches(phoneRegex) || phoneNumber.isEmpty()
    }



}