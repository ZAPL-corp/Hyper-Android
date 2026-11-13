package ru.hyper.messenger.data

import ru.hyper.messenger.model.Chat
import ru.hyper.messenger.model.ChatKind
import ru.hyper.messenger.model.HyperUser
import ru.hyper.messenger.model.Message

/**
 * Local-only data used by the UI concept. Replace with API repositories once the server is live.
 */
object DemoData {
    const val DEMO_EMAIL = "test@hyper.ru"
    const val DEMO_PASSWORD = "despatch"
    const val DEMO_2FA_CODE = "123456"

    val demoUser = HyperUser(
        id = "demo-account",
        displayName = "Алексей Волков",
        username = "@hyper_test",
        email = DEMO_EMAIL,
        status = "На связи в HYPER ✨",
        isDemo = true,
    )

    val chats: List<Chat> = listOf(
        Chat(
            id = "team",
            title = "Команда HYPER",
            lastMessage = "Аня: Новые макеты уже в Figma 🚀",
            time = "12:48",
            unreadCount = 3,
            pinned = true,
            kind = ChatKind.GROUP,
        ),
        Chat(
            id = "maria",
            title = "Мария Соколова",
            lastMessage = "Скинула макет нового экрана ✨",
            time = "11:02",
            unreadCount = 1,
            online = true,
        ),
        Chat(
            id = "dima",
            title = "Дима",
            lastMessage = "Го тестить Material You?",
            time = "09:47",
        ),
        Chat(
            id = "family",
            title = "Семья",
            lastMessage = "Мама: Не забудь позвонить",
            time = "Вт",
            unreadCount = 3,
            pinned = true,
            kind = ChatKind.GROUP,
        ),
        Chat(
            id = "support",
            title = "HYPER Support",
            lastMessage = "Тикет #4821 закрыт. Спасибо!",
            time = "Вчера",
            kind = ChatKind.CHANNEL,
        ),
        Chat(
            id = "anna",
            title = "Анна Петрова",
            lastMessage = "Спасибо! 🫶",
            time = "Вчера",
        ),
        Chat(
            id = "news",
            title = "HYPER News",
            lastMessage = "Бета 1.0 уже совсем скоро",
            time = "Пн",
            kind = ChatKind.CHANNEL,
        ),
    )

    val unreadTotal: Int
        get() = chats.sumOf { it.unreadCount }

    fun chatById(id: String): Chat? = chats.firstOrNull { it.id == id }

    fun messagesFor(chatId: String): List<Message> = messages[chatId].orEmpty()

    fun suggestedContacts(): List<Chat> = listOfNotNull(
        chatById("maria"),
        chatById("dima"),
        chatById("anna"),
        chatById("team"),
    )

    fun sessionUserFor(login: String): HyperUser {
        val base = login.substringBefore("@").ifBlank { "hyper_user" }
        val displayName = base
            .replace('_', ' ')
            .replace('.', ' ')
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            .ifBlank { "HYPER User" }
        val username = base
            .lowercase()
            .replace(Regex("[^a-z0-9._]"), "")
            .ifBlank { "hyper_user" }
        val email = if (login.contains("@")) login else "$username@hyper.ru"

        return HyperUser(
            id = "local-$username",
            displayName = displayName,
            username = "@$username",
            email = email,
            status = "На связи в HYPER",
            isDemo = false,
        )
    }

    private val messages: Map<String, List<Message>> = mapOf(
        "team" to listOf(
            Message(1, "Всем привет! Собрала экраны для Material You 3.", "11:20", sender = "Аня"),
            Message(2, "Выглядит огонь 🔥", "11:23", sender = "Марк"),
            Message(
                3,
                "Особенно нижняя панель. Очень в стиле iOS, но по-материальному.",
                "11:25",
                isMine = true,
            ),
            Message(
                4,
                "Новые макеты уже в Figma 🚀 Проверьте экран профиля и QR-авторизацию.",
                "12:48",
                sender = "Аня",
            ),
        ),
        "maria" to listOf(
            Message(1, "Привет! Успеваем к демо?", "10:41", isMine = true),
            Message(2, "Да, финализирую цвета и типографику.", "10:43", sender = "Мария"),
            Message(3, "Тогда встречаемся после обеда.", "10:45", isMine = true),
            Message(4, "Скинула макет нового экрана ✨", "11:02", sender = "Мария"),
        ),
        "dima" to listOf(
            Message(1, "Го тестить Material You?", "09:47", sender = "Дима"),
            Message(2, "Уже собираю APK 😎", "09:50", isMine = true),
            Message(3, "Скинь потом скриншоты нижней панели.", "09:51", sender = "Дима"),
        ),
        "family" to listOf(
            Message(1, "Не забудь позвонить бабушке ❤️", "18:12", sender = "Мама"),
            Message(2, "Обязательно! После работы.", "18:20", isMine = true),
            Message(3, "Мама: Не забудь позвонить", "18:42", sender = "Мама"),
        ),
        "support" to listOf(
            Message(
                1,
                "Здравствуйте! Обращение #4821 принято в работу.",
                "Вчера, 14:02",
                sender = "HYPER Support",
            ),
            Message(
                2,
                "Тикет #4821 закрыт. Спасибо, что помогаете делать HYPER лучше!",
                "Вчера, 16:30",
                sender = "HYPER Support",
            ),
        ),
        "anna" to listOf(
            Message(1, "Отправила финальные иллюстрации.", "Вчера, 12:10", sender = "Анна"),
            Message(2, "Они прекрасны. Спасибо! 🫶", "Вчера, 12:14", isMine = true),
            Message(3, "Спасибо! 🫶", "Вчера, 12:15", sender = "Анна"),
        ),
        "news" to listOf(
            Message(
                1,
                "HYPER 1.0 Beta уже совсем скоро. Спасибо, что вы с нами!",
                "Пн, 10:00",
                sender = "HYPER News",
            ),
        ),
    )
}
