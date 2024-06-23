fun main() {
    val trainer = LearnWordsTrainer()

    while (true) {
        println(
            """
            Выберите пункт меню:
            1. Учить слова
            2. Статистика
            0. Выход
        """.trimIndent()
        )

        val userInput = readlnOrNull()

        when (userInput) {
            "1" -> {
                while (true) {
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Вы выучили все слова!")
                        break
                    } else {
                        println(
                            "Перевод слова <${
                                question.correctAnswer.enWord.lowercase().replaceFirstChar { it.uppercase() }
                            }>"
                        )
                        question.variants.forEach { it ->
                            println(
                                "${question.variants.indexOf(it) + 1}. ${
                                    it.ruWord.lowercase().replaceFirstChar { it.uppercase() }
                                }"
                            )

                        }
                        println("0. Выход")
                    }

                    print("Введите ваш ответ: ")
                    val userAnswer = readln().toIntOrNull()
                    if (userAnswer == 0) break

                    if (trainer.checkAnswer(userAnswer?.minus(1))) {
                        println("Верно!")
                    } else {
                        println(
                            "Не верно, перевод слова <" +
                                    "${
                                        question.correctAnswer.enWord.lowercase().replaceFirstChar { it.uppercase() }
                                    }> " +
                                    "это <${
                                        question.correctAnswer.ruWord.lowercase().replaceFirstChar { it.uppercase() }
                                    }>"
                        )
                    }
                }
            }

            "2" -> {
                val statistics = trainer.getUserStatistics()
                println("Выучено ${statistics.learnedWords} из ${statistics.wordCount} слов | ${statistics.percentageLearned}%")
            }

            "0" -> {
                println("Выход из программы...")
                break
            }

            else -> println("Неверный ввод. Введите доступные значения: 1, 2 или 0.")
        }
    }
}
