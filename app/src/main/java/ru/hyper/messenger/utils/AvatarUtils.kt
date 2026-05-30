package ru.hyper.messenger.utils

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView

object AvatarUtils {
    private val defaultColors = listOf(
        "#00adb5", "#ff5722", "#e91e63", "#9c27b0",
        "#4caf50", "#3f51b5", "#ff9800", "#795548"
    )

    fun getLetter(username: String): String = username.firstOrNull()?.uppercase() ?: "?"

    fun getColorForUsername(username: String, storedColor: String?): Int {
        if (!storedColor.isNullOrEmpty() && storedColor.startsWith("#")) {
            return try { Color.parseColor(storedColor) } catch (e: Exception) { getDefaultColor(username) }
        }
        return getDefaultColor(username)
    }

    private fun getDefaultColor(username: String): Int {
        val idx = Math.abs(username.hashCode()) % defaultColors.size
        return Color.parseColor(defaultColors[idx])
    }

    fun applyToTextView(tv: TextView, username: String, color: String?) {
        val c = getColorForUsername(username, color)
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(c)
        }
        tv.background = drawable
        tv.text = getLetter(username)
    }
}
