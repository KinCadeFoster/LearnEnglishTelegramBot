val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
val textRegex = "\"text\":\"([^\"]+)\"".toRegex()
val chatIdRegex = "\"chat\":\\s*\\{\\s*\"id\":\\s*(\\d+)".toRegex()
val callBack = "\"data\":\\s*\"([^\"]+)".toRegex()


fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0

    val trainer = LearnWordsTrainer()

    while (true) {
        val telegramBotService = TelegramBotService(botToken)
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println("это updates: $updates")

        updateId = updateIdRegex.findAll(updates)
            .mapNotNull { it.groups[1]?.value?.toInt() }
            .maxOrNull()?.plus(1) ?: 0

        val matchResult: MatchResult? = textRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value?.lowercase()

        val chatId = getChatId(updates)
        val chatAnswer = getChatAnswer(updates)
        println(chatAnswer)


        when (text) {
            "/start" -> {
                if (chatId != 0) println(telegramBotService.sendMenu(chatId))
            }
        }

        when {
            chatAnswer == STATISTICS_CLICKED -> {
                if (chatId != 0) println(telegramBotService.sendStatistics(chatId))
            }

            chatAnswer == LEARN_WORDS_CLICKED -> {
                checkNextQuestionAndSend(trainer, chatId, telegramBotService)
            }

            chatAnswer.startsWith(CALLBACK_DATA_ANSWER_PREFIX) -> {
                val userAnswer = chatAnswer.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
                if (trainer.checkAnswer(userAnswer.minus(1))) {
                    if (chatId != 0) {
                        println(telegramBotService.sendMessage("Верно", chatId))
                        checkNextQuestionAndSend(trainer, chatId, telegramBotService)
                    }
                } else {
                    println(
                        telegramBotService.sendMessage(
                            "Не правильно: ${trainer.question?.correctAnswer?.enWord} - " +
                                    "${trainer.question?.correctAnswer?.ruWord}", chatId
                        )
                    )
                    checkNextQuestionAndSend(trainer, chatId, telegramBotService)
                }
            }
        }
    }
}


fun getChatId(updates: String): Int {
    val matchResultChatId = chatIdRegex.find(updates)
    val groupsId = matchResultChatId?.groups
    val chatId = groupsId?.get(1)?.value?.toInt() ?: 0
    return chatId
}

fun getChatAnswer(updates: String): String {
    val matchResultAnswer = callBack.find(updates)
    val groupsAnswer = matchResultAnswer?.groups
    val chatAnswer = groupsAnswer?.get(1)?.value ?: "0"
    return chatAnswer
}

fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Int, telegramBotService: TelegramBotService) {
    val question = trainer.getNextQuestion()
    if (chatId != 0) println(telegramBotService.sendQuestion(chatId, question))
}