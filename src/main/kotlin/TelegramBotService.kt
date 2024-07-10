import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val TELEGRAM_BOT_API_URL = "https://api.telegram.org/bot"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"


class TelegramBotService(private val botToken: String) {

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TELEGRAM_BOT_API_URL$botToken/getUpdates?offset=$updateId"

        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(text: String, chatId: Int): String {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage?chat_id=$chatId&text=$encoded"

        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Int): String {
        val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "$LEARN_WORDS_CLICKED"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "$STATISTICS_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(chatId: Int, question: Question?): String {
        if (question == null) {
            return sendMessage("Вы выучили все слова!", chatId)
        } else {
            val urlSendMessage = "$TELEGRAM_BOT_API_URL$botToken/sendMessage"
            val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Перевод слова \"${
                question.correctAnswer.enWord.lowercase().replaceFirstChar { it.uppercase() }
            }\"?",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "${
                question.variants[0].ruWord.lowercase().replaceFirstChar { it.uppercase() }
            }",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 1}"
                            },
                            {
                                "text": "${
                question.variants[1].ruWord.lowercase().replaceFirstChar { it.uppercase() }
            }",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 2}"
                            }
                            ],
                            [
                            {
                                "text": "${
                question.variants[2].ruWord.lowercase().replaceFirstChar { it.uppercase() }
            }",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 3}"
                            },
                            {
                                "text": "${
                question.variants[3].ruWord.lowercase().replaceFirstChar { it.uppercase() }
            }",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 4}"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

            val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()
        }


    }

    fun sendStatistics(chatId: Int): String {
        val statistics = LearnWordsTrainer().getUserStatistics()
        return sendMessage(
            "\"Выучено ${statistics.learnedWords} из ${statistics.wordCount} слов. \n Это ${statistics.percentageLearned}% от всех слов в базе данных!\"",
            chatId
        )
    }
}