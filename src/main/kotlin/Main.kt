import java.io.File

const val WORD_LEARNED_LIMIT = 3
const val TOTAL_ANSWER_CHOICES = 4
const val NEEDED_WRONG_ANSWERS = TOTAL_ANSWER_CHOICES - 1

data class Word(
    val enWord: String,
    val ruWord: String,
    var correctCount: Int,
)

fun getUserStatistics(dictionary: List<Word>) {
    val wordCount = dictionary.size
    if (wordCount > 0) {
        val learnedWords = dictionary.count { it.correctCount >= WORD_LEARNED_LIMIT }
        val percentageLearned = ((learnedWords.toDouble() / wordCount) * 100).toInt()
        println("Выучено $learnedWords из $wordCount слов | $percentageLearned%")
    } else {
        println("Словарь пустой!")
    }
}

fun getDictionary(): List<Word> {
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

fun updateWordsFile(dictionary: List<Word>) {
    val file = File("words.txt")
    file.printWriter().use { writer ->
        dictionary.forEach { word ->
            writer.println("${word.enWord}|${word.ruWord}|${word.correctCount}")
        }
    }
}

fun learnWords(dictionary: List<Word>) {
    while (true) {
        val unlearnedWords = getUnlearnedWords(dictionary)
        if (unlearnedWords.isEmpty()) {
            println("Вы выучили все слова!")
            break
        }
        val learningWord = takeShuffleElements(unLearningWord = unlearnedWords)
        println("Перевод слова <${learningWord[0].enWord.lowercase().replaceFirstChar { it.uppercase() }}>")

        val answers = takeShuffleElements(dictionary, unlearnedWords, value = NEEDED_WRONG_ANSWERS, learningWord)
        answers.forEach { it ->
            println(
                "${answers.indexOf(it) + 1}. ${it.ruWord.lowercase().replaceFirstChar { it.uppercase() }}"
            )
        }
        println("0. Выход")

        print("Введите ваш ответ: ")
        val userAnswer = readlnOrNull()?.toInt() ?: 404
        if (userAnswer == 0) break
        if (userAnswer !in 1..TOTAL_ANSWER_CHOICES) {
            println("Вы ввели неверное значение введите ответ от 1 до 4")
        } else {
            if (answers[userAnswer - 1].ruWord == learningWord[0].ruWord) {
                updateCorrectCount(learningWord)
                updateWordsFile(dictionary)
            } else {
                println(
                    "Не верно, перевод слова <" +
                            "${learningWord[0].enWord.lowercase().replaceFirstChar { it.uppercase() }}> " +
                            "это <${learningWord[0].ruWord.lowercase().replaceFirstChar { it.uppercase() }}>"
                )
            }
        }
    }
}

fun updateCorrectCount(learningWord: List<Word>) {
    learningWord[0].correctCount++
}

fun takeShuffleElements(
    dictionary: List<Word> = emptyList(),
    unLearningWord: List<Word>,
    value: Int = 0,
    learningWord: List<Word> = emptyList(),
): List<Word> {
    return if (value == 0) {
        unLearningWord.shuffled().take(1)
    } else if (unLearningWord.size - 1 < value) {
        getAnswerChoices(dictionary, learningWord, value)
    } else {
        getAnswerChoices(unLearningWord, learningWord, value)
    }
}

fun getAnswerChoices(dictionary: List<Word>, learningWord: List<Word>, value: Int): List<Word> {
    val tempDictionary = dictionary.filter { word -> !learningWord.contains(word) }
    val answers = tempDictionary.shuffled().take(value) + learningWord
    return answers.shuffled()
}

fun getUnlearnedWords(dictionary: List<Word>): List<Word> {
    return dictionary.filter { it.correctCount < WORD_LEARNED_LIMIT }
}


fun main() {
    val dictionary = getDictionary()
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
                learnWords(dictionary)
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