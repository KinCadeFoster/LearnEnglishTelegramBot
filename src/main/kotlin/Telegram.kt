fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0

    val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
    val textRegex = "\"text\":\"([^\"]+)\"".toRegex()
    val chatIdRegex = "\"chat\":\\s*\\{\\s*\"id\":\\s*(\\d+)".toRegex()

    while (true) {
        val telegramBotService = TelegramBotService(botToken)
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        updateId = updateIdRegex.findAll(updates)
            .mapNotNull { it.groups[1]?.value?.toInt() }
            .maxOrNull()?.plus(1) ?: 0

        val matchResult: MatchResult? = textRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value?.lowercase()

        if (text == "hello") {
            val matchResultChatId = chatIdRegex.find(updates)
            val groupsId = matchResultChatId?.groups
            val chatId = groupsId?.get(1)?.value?.toInt() ?: 0
            if (chatId != 0) println(telegramBotService.sendMessage("Hello", chatId))
        }
    }
}


