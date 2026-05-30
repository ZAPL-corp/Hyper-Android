package ru.hyper.messenger.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch
import ru.hyper.messenger.R
import ru.hyper.messenger.adapters.ChatsAdapter
import ru.hyper.messenger.api.RetrofitClient
import ru.hyper.messenger.chat.ChatActivity
import ru.hyper.messenger.models.ChatPreview
import ru.hyper.messenger.models.Friend
import ru.hyper.messenger.utils.SessionManager

class ChatsFragment : Fragment() {

    private lateinit var rvChats: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var etSearch: EditText
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: ChatsAdapter

    private val api by lazy { RetrofitClient.create(requireContext()) }
    private var allChats: List<ChatPreview> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvChats = view.findViewById(R.id.rvChats)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        etSearch = view.findViewById(R.id.etSearch)
        emptyState = view.findViewById(R.id.emptyState)

        adapter = ChatsAdapter(emptyList()) { chat ->
            openChat(chat)
        }
        rvChats.layoutManager = LinearLayoutManager(requireContext())
        rvChats.adapter = adapter

        swipeRefresh.setColorSchemeColors(android.graphics.Color.parseColor("#00C8D4"))
        swipeRefresh.setOnRefreshListener { loadChats() }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filtered = adapter.filter(s?.toString() ?: "")
                adapter.updateData(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadChats()
    }

    private fun loadChats() {
        lifecycleScope.launch {
            try {
                swipeRefresh.isRefreshing = true
                val resp = api.getFriends()
                swipeRefresh.isRefreshing = false
                if (resp.isSuccessful) {
                    val friends = resp.body()?.friends ?: emptyList()
                    allChats = friends.map { f ->
                        ChatPreview(
                            username = f.username,
                            avatarColor = f.avatarColor,
                            customAvatar = f.customAvatar,
                            lastMessage = "Нажмите, чтобы открыть чат",
                            time = ""
                        )
                    }
                    adapter.updateData(allChats)
                    showEmpty(allChats.isEmpty())
                }
            } catch (e: Exception) {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun openChat(chat: ChatPreview) {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_USERNAME, chat.username)
            putExtra(ChatActivity.EXTRA_AVATAR_COLOR, chat.avatarColor)
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun showEmpty(empty: Boolean) {
        rvChats.visibility = if (empty) View.GONE else View.VISIBLE
        emptyState.visibility = if (empty) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadChats()
    }
}
