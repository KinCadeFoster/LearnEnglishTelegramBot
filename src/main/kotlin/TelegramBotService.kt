import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_BOT_API_URL = "https://api.telegram.org/bot"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val RESET_CLICKED = "reset_clicked"
const val START_CLICKED = "/start"

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InLineKeyboard>>,
)

@Serializable
data class InLineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

class TelegramBotService(private val botToken: String) {
    private val client: HttpClient = HttpClient.newBuilder().build()



    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$TELEGRAM_BOT_API_URL$botToken/getUpdates?offset=$updateId"
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(text: String, chatId: Long, json: Json): String {
        val sendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = text,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Long, json: Json): String {
        val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InLineKeyboard(text = "Изучить слова", callbackData = LEARN_WORDS_CLICKED),
                        InLineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED)
                    ),
                    listOf(
                        InLineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CLICKED)
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(chatId: Long, question: Question?, json: Json): String {
        if (question == null) {
            return sendMessage("Вы выучили все слова!", chatId, json)
        } else {
            val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"

            val requestBody = SendMessageRequest(
                chatId = chatId,
                text = "Перевод слова: ${question.correctAnswer.enWord.lowercase().replaceFirstChar { it.uppercase() }}",
                replyMarkup = ReplyMarkup(
                    inlineKeyboard = createInlineKeyboard(question.variants)
                )
            )
            val requestBodyString = json.encodeToString(requestBody)

            val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()
        }
    }

    private fun createInlineKeyboard(variants: List<Word>): List<List<InLineKeyboard>> {
        val inlineKeyboard = mutableListOf<List<InLineKeyboard>>()
        val row = mutableListOf<InLineKeyboard>()
        variants.forEachIndexed { index, variant ->
            row.add(
                InLineKeyboard(
                    text = variant.ruWord.lowercase().replaceFirstChar { it.uppercase() },
                    callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                )
            )
            if (row.size == 2) {
                inlineKeyboard.add(row.toList())
                row.clear()
            }
        }
        if (row.isNotEmpty()) {
            inlineKeyboard.add(row)
        }

        inlineKeyboard.add(
            listOf(
                InLineKeyboard(text = "Вернуться в главное меню", callbackData = START_CLICKED)
            )
        )

        return inlineKeyboard
    }
}