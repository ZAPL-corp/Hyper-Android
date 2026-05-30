package ru.hyper.messenger.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.hyper.messenger.MainActivity
import ru.hyper.messenger.R
import ru.hyper.messenger.utils.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val content = findViewById<android.view.View>(R.id.splashContent)
        content.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).withEndAction {
            android.os.Handler(mainLooper).postDelayed({
                val dest = if (SessionManager.isLoggedIn(this)) {
                    Intent(this, MainActivity::class.java)
                } else {
                    Intent(this, LoginActivity::class.java)
                }
                startActivity(dest)
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }, 800)
        }.start()
    }
}
