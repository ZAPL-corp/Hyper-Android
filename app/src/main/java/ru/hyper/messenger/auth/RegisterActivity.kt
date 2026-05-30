package ru.hyper.messenger.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import ru.hyper.messenger.R
import ru.hyper.messenger.api.RetrofitClient
import ru.hyper.messenger.models.RegisterRequest

class RegisterActivity : AppCompatActivity() {
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvLogin: TextView

    private val api by lazy { RetrofitClient.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tvLogin = findViewById(R.id.tvLogin)

        btnRegister.setOnClickListener { doRegister() }
        tvLogin.setOnClickListener { finish(); overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) }
    }

    private fun doRegister() {
        val username = etUsername.text?.toString()?.trim() ?: ""
        val email = etEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString() ?: ""
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля")
            return
        }
        if (password.length < 6) { showError("Пароль минимум 6 символов"); return }
        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = api.register(RegisterRequest(username, email, password))
                if (resp.isSuccessful && resp.body()?.success == true) {
                    setLoading(false)
                    startActivity(Intent(this@RegisterActivity, VerifyActivity::class.java).apply {
                        putExtra("email", email)
                    })
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    setLoading(false)
                    showError(resp.body()?.message ?: "Ошибка регистрации")
                }
            } catch (e: Exception) {
                setLoading(false)
                showError("Ошибка сети")
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !loading
        tvError.visibility = View.GONE
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
