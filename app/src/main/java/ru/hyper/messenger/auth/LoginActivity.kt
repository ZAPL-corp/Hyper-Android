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
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import ru.hyper.messenger.MainActivity
import ru.hyper.messenger.R
import ru.hyper.messenger.api.RetrofitClient
import ru.hyper.messenger.models.LoginRequest
import ru.hyper.messenger.models.Login2faRequest

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvRegister: TextView

    private val api by lazy { RetrofitClient.create(this) }
    private var pendingEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener { doLogin() }
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        etPassword.setOnEditorActionListener { _, _, _ -> doLogin(); true }
    }

    private fun doLogin() {
        val email = etEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString() ?: ""
        if (email.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля")
            return
        }
        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = api.login(LoginRequest(email, password))
                if (resp.isSuccessful) {
                    val body = resp.body()!!
                    if (body.require2fa) {
                        pendingEmail = body.email
                        setLoading(false)
                        show2faDialog(body.email)
                    } else if (body.success) {
                        loadMeAndGo()
                    } else {
                        setLoading(false)
                        showError(body.message.ifEmpty { "Ошибка входа" })
                    }
                } else {
                    setLoading(false)
                    showError("Неверный email или пароль")
                }
            } catch (e: Exception) {
                setLoading(false)
                showError("Ошибка сети: ${e.message}")
            }
        }
    }

    private fun show2faDialog(email: String) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Двухфакторная аутентификация")
            .setMessage("Введите 6-значный код из приложения аутентификатора")
            .create()
        val et = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Код"
            setPadding(60, 40, 60, 20)
        }
        dialog.setView(et)
        dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Подтвердить") { _, _ ->
            setLoading(true)
            lifecycleScope.launch {
                try {
                    val resp = api.login2fa(Login2faRequest(email, et.text.toString()))
                    if (resp.isSuccessful && resp.body()?.success == true) {
                        loadMeAndGo()
                    } else {
                        setLoading(false)
                        showError("Неверный код 2FA")
                    }
                } catch (e: Exception) {
                    setLoading(false)
                    showError("Ошибка: ${e.message}")
                }
            }
        }
        dialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Отмена") { d, _ -> d.dismiss(); setLoading(false) }
        dialog.show()
    }

    private suspend fun loadMeAndGo() {
        try {
            val meResp = api.getMe()
            if (meResp.isSuccessful && meResp.body() != null) {
                ru.hyper.messenger.utils.SessionManager.saveSession(this, meResp.body()!!)
            }
        } catch (e: Exception) { /* ignore */ }
        setLoading(false)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
        tvError.visibility = View.GONE
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
        tvError.animate().alpha(1f).setDuration(300).start()
    }
}
