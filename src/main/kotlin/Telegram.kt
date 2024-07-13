import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)


fun main(args: Array<String>) {
    val botToken = args[0]
    var lastUpdateId = 0L

    val json = Json {
        ignoreUnknownKeys = true
    }
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        val telegramBotService = TelegramBotService(botToken)
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        println("это updates: $responseString")

        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdate = response.result.sortedBy { it.updateId }
        sortedUpdate.forEach { handleUpdate(it, json, trainers, telegramBotService) }
        lastUpdateId = sortedUpdate.last().updateId + 1
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    chatId: Long,
    telegramBotService: TelegramBotService,
    json: Json
) {
    val question = trainer.getNextQuestion()
    telegramBotService.sendQuestion(chatId, question, json)
}

fun handleUpdate(
    update: Update,
    json: Json,
    trainers: HashMap<Long, LearnWordsTrainer>,
    telegramBotService: TelegramBotService
) {
    val text = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val chatAnswer = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    when {
        text?.lowercase() == START_CLICKED || chatAnswer == START_CLICKED  -> {
            telegramBotService.sendMenu(chatId, json)
        }

        chatAnswer == STATISTICS_CLICKED -> {
            val statistics = trainer.getUserStatistics()
            telegramBotService.sendMessage(
                "Выучено ${statistics.learnedWords} из ${statistics.wordCount} слов. \n Это ${statistics.percentageLearned}% от всех слов в базе данных!",
                chatId, json
            )
            telegramBotService.sendMenu(chatId, json)
        }

        chatAnswer == LEARN_WORDS_CLICKED -> {
            checkNextQuestionAndSend(trainer, chatId, telegramBotService, json)
        }

        chatAnswer == RESET_CLICKED -> {
            trainer.resetProgress()
            telegramBotService.sendMessage("Прогресс изучения слов сброшен!", chatId, json)
            telegramBotService.sendMenu(chatId, json)
        }

        chatAnswer?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
            val userAnswer = chatAnswer.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(userAnswer)) {
                telegramBotService.sendMessage("Верно!", chatId, json)
            } else {
                telegramBotService.sendMessage(
                    "Не правильно: ${trainer.question?.correctAnswer?.enWord} - " +
                            "${trainer.question?.correctAnswer?.ruWord}", chatId, json
                )
            }
            checkNextQuestionAndSend(trainer, chatId, telegramBotService, json)
        }
    }
}