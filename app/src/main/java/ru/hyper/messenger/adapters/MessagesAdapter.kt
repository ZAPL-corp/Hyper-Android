package ru.hyper.messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.hyper.messenger.R
import ru.hyper.messenger.models.Message
import ru.hyper.messenger.utils.AvatarUtils

class MessagesAdapter(
    private val items: MutableList<Message>,
    private val myUsername: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_OUT = 0
        const val TYPE_IN = 1
    }

    inner class OutVH(v: View) : RecyclerView.ViewHolder(v) {
        val tvMessage: TextView = v.findViewById(R.id.tvMessage)
        val tvTime: TextView = v.findViewById(R.id.tvTime)
    }

    inner class InVH(v: View) : RecyclerView.ViewHolder(v) {
        val tvMessage: TextView = v.findViewById(R.id.tvMessage)
        val tvTime: TextView = v.findViewById(R.id.tvTime)
        val tvSender: TextView = v.findViewById(R.id.tvSender)
        val tvAvatar: TextView = v.findViewById(R.id.tvAvatarLetter)
    }

    override fun getItemViewType(pos: Int) = if (items[pos].sender == myUsername || items[pos].sender == "Вы") TYPE_OUT else TYPE_IN

    override fun onCreateViewHolder(p: ViewGroup, type: Int): RecyclerView.ViewHolder {
        return if (type == TYPE_OUT)
            OutVH(LayoutInflater.from(p.context).inflate(R.layout.item_message_out, p, false))
        else
            InVH(LayoutInflater.from(p.context).inflate(R.layout.item_message_in, p, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
        val msg = items[pos]
        val displayText = when {
            msg.message.startsWith("[IMG]") -> "📷 Фото"
            msg.message.startsWith("[FILE:") -> "📎 Файл"
            else -> msg.message
        }
        val time = if (msg.timestamp.length >= 16) msg.timestamp.substring(11, 16) else ""
        when (h) {
            is OutVH -> {
                h.tvMessage.text = displayText
                h.tvTime.text = time
            }
            is InVH -> {
                h.tvMessage.text = displayText
                h.tvTime.text = time
                h.tvSender.text = msg.sender
                AvatarUtils.applyToTextView(h.tvAvatar, msg.sender, null)
            }
        }
    }

    fun addMessage(msg: Message) {
        items.add(msg)
        notifyItemInserted(items.size - 1)
    }

    fun setMessages(msgs: List<Message>) {
        items.clear()
        items.addAll(msgs)
        notifyDataSetChanged()
    }
}
