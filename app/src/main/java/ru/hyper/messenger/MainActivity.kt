package ru.hyper.messenger

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.hyper.messenger.api.SocketManager
import ru.hyper.messenger.auth.LoginActivity
import ru.hyper.messenger.fragments.ChatsFragment
import ru.hyper.messenger.fragments.FriendsFragment
import ru.hyper.messenger.fragments.ProfileFragment
import ru.hyper.messenger.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var currentTabIndex = 0

    private val chatsFragment by lazy { ChatsFragment() }
    private val friendsFragment by lazy { FriendsFragment() }
    private val profileFragment by lazy { ProfileFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)
        bottomNav = findViewById(R.id.bottomNav)

        // Connect socket
        val username = SessionManager.getUsername(this)
        val cookie = SessionManager.getAuthCookie(this)
        SocketManager.connect(username, cookie)

        // Load initial fragment
        showFragment(chatsFragment, 0)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chats -> { showFragment(chatsFragment, 0); true }
                R.id.nav_friends -> { showFragment(friendsFragment, 1); true }
                R.id.nav_profile -> { showFragment(profileFragment, 2); true }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment, tabIndex: Int) {
        val (enterAnim, exitAnim) = when {
            tabIndex > currentTabIndex -> Pair(R.anim.slide_in_right, R.anim.slide_out_left)
            tabIndex < currentTabIndex -> Pair(R.anim.slide_in_left, R.anim.slide_out_right)
            else -> Pair(R.anim.fade_in, R.anim.fade_out)
        }
        currentTabIndex = tabIndex

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(enterAnim, exitAnim)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun navigateToChats() {
        bottomNav.selectedItemId = R.id.nav_chats
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.disconnect()
    }
}
