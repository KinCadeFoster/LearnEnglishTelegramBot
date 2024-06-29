fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0

    while (true) {
        val telegramBotService = TelegramBotService()
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(botToken, updateId)
        println(updates)

        val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
        updateId = updateIdRegex.findAll(updates)
            .mapNotNull { it.groups[1]?.value?.toInt() }
            .maxOrNull()?.plus(1) ?: 0

        val textRegex = "\"text\":\"([^\"]+)\"".toRegex()
        val matchResult: MatchResult? = textRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value?.lowercase()

        if (text == "hello") {
            val chatIdRegex = "\"chat\":\\s*\\{\\s*\"id\":\\s*(\\d+)".toRegex()
            val matchResultChatId = chatIdRegex.find(updates)
            val groupsId = matchResultChatId?.groups
            val chatId = groupsId?.get(1)?.value?.toInt() ?: 0
            if (chatId != 0) println(telegramBotService.sendMessage("Hello", chatId, botToken))
        }
    }
}


