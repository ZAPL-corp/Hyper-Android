package ru.hyper.messenger.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import ru.hyper.messenger.R
import ru.hyper.messenger.adapters.MessagesAdapter
import ru.hyper.messenger.api.SocketManager
import ru.hyper.messenger.models.Message
import ru.hyper.messenger.utils.AvatarUtils
import ru.hyper.messenger.utils.SessionManager

class ChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USERNAME = "username"
        const val EXTRA_AVATAR_COLOR = "avatar_color"
    }

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var tvChatName: TextView
    private lateinit var tvTypingStatus: TextView
    private lateinit var tvAvatarLetter: TextView
    private lateinit var adapter: MessagesAdapter

    private lateinit var chatWith: String
    private lateinit var myUsername: String
    private var avatarColor: String = "#00C8D4"
    private val handler = Handler(Looper.getMainLooper())
    private var typingHideRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatWith = intent.getStringExtra(EXTRA_USERNAME) ?: ""
        avatarColor = intent.getStringExtra(EXTRA_AVATAR_COLOR) ?: "#00C8D4"
        myUsername = SessionManager.getUsername(this)

        // Views
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        tvChatName = findViewById(R.id.tvChatName)
        tvTypingStatus = findViewById(R.id.tvTypingStatus)
        tvAvatarLetter = findViewById(R.id.tvAvatarLetter)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Setup
        tvChatName.text = chatWith
        AvatarUtils.applyToTextView(tvAvatarLetter, chatWith, avatarColor)
        tvTypingStatus.text = "онлайн"

        adapter = MessagesAdapter(mutableListOf(), myUsername)
        rvMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvMessages.adapter = adapter

        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        btnSend.setOnClickListener { sendMessage() }
        etMessage.setOnEditorActionListener { _, _, _ -> sendMessage(); true }

        // Typing detection
        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                SocketManager.emitTyping(chatWith, false)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Socket events
        SocketManager.onMessage = { from, message, color, avatar ->
            if (from == chatWith) {
                runOnUiThread {
                    adapter.addMessage(Message(sender = from, message = message, receiver = myUsername))
                    rvMessages.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }
        SocketManager.onTyping = { typingUser ->
            if (typingUser == chatWith) {
                runOnUiThread {
                    tvTypingStatus.text = "печатает…"
                    typingHideRunnable?.let { handler.removeCallbacks(it) }
                    typingHideRunnable = Runnable { tvTypingStatus.text = "онлайн" }
                    handler.postDelayed(typingHideRunnable!!, 3000)
                }
            }
        }

        // Load history
        SocketManager.getChatHistory(chatWith) { history ->
            val messages = history.map { obj ->
                Message(
                    id = obj.optInt("id"),
                    sender = obj.optString("sender"),
                    receiver = obj.optString("receiver"),
                    message = obj.optString("message"),
                    timestamp = obj.optString("timestamp")
                )
            }
            runOnUiThread {
                adapter.setMessages(messages)
                if (messages.isNotEmpty()) rvMessages.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun sendMessage() {
        val text = etMessage.text?.toString()?.trim() ?: ""
        if (text.isEmpty()) return
        etMessage.setText("")
        val msg = Message(sender = myUsername, receiver = chatWith, message = text)
        adapter.addMessage(msg)
        rvMessages.scrollToPosition(adapter.itemCount - 1)
        SocketManager.sendPrivateMessage(chatWith, text,
            SessionManager.getAvatarColor(this),
            SessionManager.getCustomAvatar(this))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.onMessage = null
        SocketManager.onTyping = null
    }
}
