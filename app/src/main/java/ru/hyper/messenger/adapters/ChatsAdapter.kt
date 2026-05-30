package ru.hyper.messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.hyper.messenger.R
import ru.hyper.messenger.models.ChatPreview
import ru.hyper.messenger.utils.AvatarUtils

class ChatsAdapter(
    private var items: List<ChatPreview>,
    private val onClick: (ChatPreview) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvLast: TextView = v.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = v.findViewById(R.id.tvTime)
        val tvAvatar: TextView = v.findViewById(R.id.tvAvatarLetter)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_chat, p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.tvName.text = item.username
        h.tvLast.text = if (item.lastMessage.isEmpty()) "Нажмите, чтобы открыть чат" else item.lastMessage
        h.tvTime.text = item.time
        AvatarUtils.applyToTextView(h.tvAvatar, item.username, item.avatarColor)
        h.itemView.setOnClickListener { onClick(item) }
    }

    fun updateData(newItems: List<ChatPreview>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun filter(query: String): List<ChatPreview> =
        if (query.isEmpty()) items
        else items.filter { it.username.contains(query, ignoreCase = true) }
}
