package ru.hyper.messenger.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import ru.hyper.messenger.R
import ru.hyper.messenger.adapters.FriendsAdapter
import ru.hyper.messenger.api.RetrofitClient
import ru.hyper.messenger.chat.ChatActivity
import ru.hyper.messenger.models.Friend
import ru.hyper.messenger.models.FriendRequestBody

class FriendsFragment : Fragment() {

    private lateinit var rvFriends: RecyclerView
    private lateinit var rvIncoming: RecyclerView
    private lateinit var tvIncomingLabel: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var btnAddFriend: ImageView
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var incomingAdapter: FriendsAdapter

    private val api by lazy { RetrofitClient.create(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_friends, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvFriends = view.findViewById(R.id.rvFriends)
        rvIncoming = view.findViewById(R.id.rvIncoming)
        tvIncomingLabel = view.findViewById(R.id.tvIncomingLabel)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        btnAddFriend = view.findViewById(R.id.btnAddFriend)

        friendsAdapter = FriendsAdapter(
            items = emptyList(),
            showActions = false,
            onChat = { f -> openChat(f) }
        )
        incomingAdapter = FriendsAdapter(
            items = emptyList(),
            showActions = true,
            onChat = { f -> openChat(f) },
            onAccept = { f -> acceptFriend(f) },
            onReject = { f -> rejectFriend(f) }
        )

        rvFriends.layoutManager = LinearLayoutManager(requireContext())
        rvFriends.adapter = friendsAdapter
        rvIncoming.layoutManager = LinearLayoutManager(requireContext())
        rvIncoming.adapter = incomingAdapter

        swipeRefresh.setColorSchemeColors(android.graphics.Color.parseColor("#00C8D4"))
        swipeRefresh.setOnRefreshListener { loadFriends() }

        btnAddFriend.setOnClickListener { showAddFriendDialog() }

        loadFriends()
    }

    private fun loadFriends() {
        lifecycleScope.launch {
            try {
                swipeRefresh.isRefreshing = true
                val resp = api.getFriends()
                swipeRefresh.isRefreshing = false
                if (resp.isSuccessful) {
                    val body = resp.body()!!
                    friendsAdapter.updateData(body.friends)
                    incomingAdapter.updateData(body.incoming)
                    val hasIncoming = body.incoming.isNotEmpty()
                    tvIncomingLabel.visibility = if (hasIncoming) View.VISIBLE else View.GONE
                    rvIncoming.visibility = if (hasIncoming) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun openChat(friend: Friend) {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_USERNAME, friend.username)
            putExtra(ChatActivity.EXTRA_AVATAR_COLOR, friend.avatarColor)
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun acceptFriend(friend: Friend) {
        lifecycleScope.launch {
            try {
                api.acceptFriend(FriendRequestBody(friend.username))
                loadFriends()
                showToast("✅ ${friend.username} теперь ваш друг!")
            } catch (e: Exception) {}
        }
    }

    private fun rejectFriend(friend: Friend) {
        lifecycleScope.launch {
            try {
                api.rejectFriend(FriendRequestBody(friend.username))
                loadFriends()
            } catch (e: Exception) {}
        }
    }

    private fun showAddFriendDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_friend, null)
        val tilUsername = dialogView.findViewById<TextInputLayout>(R.id.tilUsername)
        val etUsername = dialogView.findViewById<TextInputEditText>(R.id.etUsername)
        val tvResult = dialogView.findViewById<TextView>(R.id.tvResult)

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_HYPER)
            .setView(dialogView)
            .setPositiveButton("Отправить запрос", null)
            .setNegativeButton("Отмена") { d, _ -> d.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                setTextColor(android.graphics.Color.parseColor("#00C8D4"))
                setOnClickListener {
                    val username = etUsername.text?.toString()?.trim() ?: ""
                    if (username.isEmpty()) { tilUsername.error = "Введите имя"; return@setOnClickListener }
                    lifecycleScope.launch {
                        try {
                            val resp = api.sendFriendRequest(FriendRequestBody(username))
                            val body = resp.body()
                            tvResult.visibility = View.VISIBLE
                            if (resp.isSuccessful && body?.success == true) {
                                tvResult.setTextColor(android.graphics.Color.parseColor("#23A55A"))
                                tvResult.text = "✅ Запрос отправлен пользователю $username!"
                            } else {
                                tvResult.setTextColor(android.graphics.Color.parseColor("#FF4D4D"))
                                tvResult.text = body?.message ?: "Ошибка"
                            }
                        } catch (e: Exception) {
                            tvResult.visibility = View.VISIBLE
                            tvResult.setTextColor(android.graphics.Color.parseColor("#FF4D4D"))
                            tvResult.text = "Ошибка сети"
                        }
                    }
                }
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(android.graphics.Color.parseColor("#8B949E"))
        }
        dialog.show()
    }

    private fun showToast(msg: String) {
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadFriends()
    }
}
