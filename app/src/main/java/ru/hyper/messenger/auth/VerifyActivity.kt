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
import ru.hyper.messenger.MainActivity
import ru.hyper.messenger.R
import ru.hyper.messenger.api.RetrofitClient
import ru.hyper.messenger.models.VerifyRequest
import ru.hyper.messenger.utils.SessionManager

class VerifyActivity : AppCompatActivity() {
    private val api by lazy { RetrofitClient.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)
        val etCode = findViewById<TextInputEditText>(R.id.etCode)
        val btnVerify = findViewById<Button>(R.id.btnVerify)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvError = findViewById<TextView>(R.id.tvError)

        btnVerify.setOnClickListener {
            val code = etCode.text?.toString()?.trim() ?: ""
            if (code.length != 6) { tvError.text = "Введите 6-значный код"; tvError.visibility = View.VISIBLE; return@setOnClickListener }
            progressBar.visibility = View.VISIBLE
            btnVerify.isEnabled = false
            tvError.visibility = View.GONE
            lifecycleScope.launch {
                try {
                    val resp = api.verify(VerifyRequest(code))
                    if (resp.isSuccessful && resp.body()?.success == true) {
                        try { val me = api.getMe(); if (me.isSuccessful && me.body() != null) SessionManager.saveSession(this@VerifyActivity, me.body()!!) } catch (e: Exception) {}
                        progressBar.visibility = View.GONE
                        startActivity(Intent(this@VerifyActivity, MainActivity::class.java))
                        finish()
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    } else {
                        progressBar.visibility = View.GONE
                        btnVerify.isEnabled = true
                        tvError.text = resp.body()?.message ?: "Неверный код"
                        tvError.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    btnVerify.isEnabled = true
                    tvError.text = "Ошибка сети"
                    tvError.visibility = View.VISIBLE
                }
            }
        }
    }
}
