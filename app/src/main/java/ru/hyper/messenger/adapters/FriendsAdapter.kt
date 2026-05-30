package ru.hyper.messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.hyper.messenger.R
import ru.hyper.messenger.models.Friend
import ru.hyper.messenger.utils.AvatarUtils

class FriendsAdapter(
    private var items: List<Friend>,
    private val showActions: Boolean = false,
    private val onChat: (Friend) -> Unit,
    private val onAccept: ((Friend) -> Unit)? = null,
    private val onReject: ((Friend) -> Unit)? = null
) : RecyclerView.Adapter<FriendsAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvUsername: TextView = v.findViewById(R.id.tvUsername)
        val tvAvatar: TextView = v.findViewById(R.id.tvAvatarLetter)
        val actionButtons: View = v.findViewById(R.id.actionButtons)
        val btnAccept: Button = v.findViewById(R.id.btnAccept)
        val btnReject: Button = v.findViewById(R.id.btnReject)
        val ivMessage: View = v.findViewById(R.id.ivMessage)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_friend, p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.tvUsername.text = item.username
        AvatarUtils.applyToTextView(h.tvAvatar, item.username, item.avatarColor)
        if (showActions) {
            h.actionButtons.visibility = View.VISIBLE
            h.ivMessage.visibility = View.GONE
            h.btnAccept.setOnClickListener { onAccept?.invoke(item) }
            h.btnReject.setOnClickListener { onReject?.invoke(item) }
        } else {
            h.actionButtons.visibility = View.GONE
            h.ivMessage.visibility = View.VISIBLE
            h.ivMessage.setOnClickListener { onChat(item) }
        }
        h.itemView.setOnClickListener { if (!showActions) onChat(item) }
    }

    fun updateData(newItems: List<Friend>) {
        items = newItems
        notifyDataSetChanged()
    }
}
