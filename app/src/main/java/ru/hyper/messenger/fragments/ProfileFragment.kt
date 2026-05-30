package ru.hyper.messenger.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import org.json.JSONObject
import ru.hyper.messenger.R
import ru.hyper.messenger.api.RetrofitClient
import ru.hyper.messenger.auth.LoginActivity
import ru.hyper.messenger.utils.AvatarUtils
import ru.hyper.messenger.utils.SessionManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileFragment : Fragment() {

    private val api by lazy { RetrofitClient.create(requireContext()) }

    private val qrScanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            handleQrLogin(result.contents)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvUsername = view.findViewById<TextView>(R.id.tvProfileUsername)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val tvAvatarLetter = view.findViewById<TextView>(R.id.tvProfileAvatarLetter)
        val cardQrLogin = view.findViewById<LinearLayout>(R.id.cardQrLogin)
        val cardLogout = view.findViewById<LinearLayout>(R.id.cardLogout)
        val switchDark = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val cardTwoFa = view.findViewById<LinearLayout>(R.id.cardTwoFa)

        // Load profile info
        val username = SessionManager.getUsername(requireContext())
        val email = SessionManager.getEmail(requireContext())
        val avatarColor = SessionManager.getAvatarColor(requireContext())

        tvUsername.text = username
        tvEmail.text = email
        AvatarUtils.applyToTextView(tvAvatarLetter, username, avatarColor)

        // QR Login
        cardQrLogin.setOnClickListener {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Направьте камеру на QR-код на сайте HYPER")
                setBeepEnabled(true)
                setOrientationLocked(false)
            }
            qrScanLauncher.launch(options)
        }

        // Theme toggle (visual only, system follows app theme)
        switchDark.isChecked = true
        switchDark.setOnCheckedChangeListener { _, _ ->
            android.widget.Toast.makeText(requireContext(), "Тема обновится при перезапуске", android.widget.Toast.LENGTH_SHORT).show()
        }

        // 2FA info
        cardTwoFa.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.Theme_HYPER)
                .setTitle("Двухфакторная аутентификация")
                .setMessage("Для управления 2FA используйте ПК-версию HYPER Messenger (hyper-messenger.ru).\n\nТам можно включить Google Authenticator через настройки.")
                .setPositiveButton("Понятно") { d, _ -> d.dismiss() }
                .show()
        }

        // Logout
        cardLogout.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.Theme_HYPER)
                .setTitle("Выход из аккаунта")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Выйти") { _, _ ->
                    lifecycleScope.launch {
                        try { api.logout() } catch (e: Exception) {}
                        SessionManager.clearSession(requireContext())
                        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                }
                .setNegativeButton("Отмена") { d, _ -> d.dismiss() }
                .show()
        }

        // Load fresh data
        loadProfile(tvUsername, tvEmail, tvAvatarLetter)
    }

    private fun loadProfile(tvUsername: TextView, tvEmail: TextView, tvAvatar: TextView) {
        lifecycleScope.launch {
            try {
                val resp = api.getMe()
                if (resp.isSuccessful && resp.body() != null) {
                    val user = resp.body()!!
                    SessionManager.saveSession(requireContext(), user)
                    tvUsername.text = user.username
                    tvEmail.text = user.email
                    AvatarUtils.applyToTextView(tvAvatar, user.username, user.avatarColor)
                }
            } catch (e: Exception) {}
        }
    }

    private fun handleQrLogin(qrContent: String) {
        // QR content от HYPER сайта должен содержать сессионный токен или URL
        // Формат ожидаемый: "hyper://qr-login?token=XXXXX" или просто токен
        try {
            val token = when {
                qrContent.startsWith("hyper://") -> {
                    val uri = android.net.Uri.parse(qrContent)
                    uri.getQueryParameter("token") ?: qrContent
                }
                else -> qrContent
            }
            lifecycleScope.launch {
                try {
                    val client = OkHttpClient()
                    val json = JSONObject().apply {
                        put("token", token)
                        put("username", SessionManager.getUsername(requireContext()))
                        put("cookie", SessionManager.getAuthCookie(requireContext()))
                    }
                    val body = json.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url("https://hyper-messenger.ru/api/qr-confirm")
                        .post(body)
                        .addHeader("Cookie", SessionManager.getAuthCookie(requireContext()))
                        .build()
                    val resp = client.newCall(request).execute()
                    val successMsg = if (resp.isSuccessful) "✅ Успешно авторизован на ПК!" else "❌ Ошибка авторизации"
                    activity?.runOnUiThread {
                        AlertDialog.Builder(requireContext(), R.style.Theme_HYPER)
                            .setTitle("QR-авторизация")
                            .setMessage(successMsg)
                            .setPositiveButton("OK") { d, _ -> d.dismiss() }
                            .show()
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        android.widget.Toast.makeText(requireContext(), "Ошибка: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "Неверный QR-код", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
