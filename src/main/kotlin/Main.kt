import java.io.File

data class Word(
    val enWord: String,
    val ruWord: String,
    val correctCount: Int,
)

fun getUserStatistics(dictionary: MutableList<Word>) {
    val wordCount = dictionary.size
    val learnedWords = dictionary.count { it.correctCount > 2 }
    if (wordCount > 0) {
        val percentageLearned = ((learnedWords.toDouble() / wordCount) * 100).toInt()
        println("Выучено $learnedWords из $wordCount слов | $percentageLearned%")
    } else {
        println("Словарь пустой!")
    }
}

fun getDictionary(): MutableList<Word> {
    val dictionary: MutableList<Word> = mutableListOf()
    val wordsFile = File("words.txt")
    for (string in wordsFile.readLines()) {
        val lineSplit = string.split("|")
        if (lineSplit.size == 3)
            dictionary.add(
                Word(
                    enWord = lineSplit[0].trim(),
                    ruWord = lineSplit[1].trim(),
                    correctCount = lineSplit[2].trim().toIntOrNull() ?: 0
                )
            )
    }
    return dictionary
}


fun main() {
    val dictionary = getDictionary()

    while (true) {
        println("Выберите пункт меню:")
        println("1. Учить слова")
        println("2. Статистика")
        println("0. Выход")

        val userInput = readlnOrNull()

        when (userInput) {
            "1" -> {
            }

            "2" -> {
                getUserStatistics(dictionary)
            }

            "0" -> {
                println("Выход из программы...")
                break
            }

            else -> {
                println("Неверный ввод. Введите доступные значения: 1, 2 или 0.")
            }
        }
    }
}