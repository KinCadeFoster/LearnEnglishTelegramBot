import java.io.File

const val WORD_LEARNED_LIMIT = 3
const val TOTAL_ANSWER_CHOICES = 4
const val NEEDED_WRONG_ANSWERS = TOTAL_ANSWER_CHOICES - 1

data class Word(
    val enWord: String,
    val ruWord: String,
    var correctCount: Int,
)

data class Statistics(
    val wordCount: Int,
    val learnedWords: Int,
    val percentageLearned: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {
    private var question: Question? = null
    private val dictionary = getDictionaryFromFile()

    fun getUserStatistics(): Statistics {
        val wordCount = dictionary.size
        val learnedWords = dictionary.count { it.correctCount >= WORD_LEARNED_LIMIT }
        val percentageLearned = ((learnedWords.toDouble() / wordCount) * 100).toInt()
        return Statistics(wordCount, learnedWords, percentageLearned)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWords = getUnlearnedWords(dictionary)
        if (unlearnedWords.isEmpty()) return null
        val learningWord = takeLearningWord(unlearnedWords)
        val questionWords = takeShuffleElements(
            dictionary,
            unlearnedWords,
            value = NEEDED_WRONG_ANSWERS,
            learningWord
        )
        question = Question(
            variants = questionWords,
            correctAnswer = learningWord
        )
        return question
    }

    fun checkAnswer(userAnswer: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswer) {
                it.correctAnswer.correctCount++
                updateWordsFile(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun getUnlearnedWords(dictionary: List<Word>): List<Word> {
        return dictionary.filter { it.correctCount < WORD_LEARNED_LIMIT }
    }

    private fun takeLearningWord(unLearningWord: List<Word>): Word {
        return unLearningWord.shuffled().take(1).first()
    }

    private fun getAnswerChoices(dictionary: List<Word>, learningWord: Word, value: Int): List<Word> {
        val tempDictionary = dictionary.filter { word -> word != learningWord }
        val answers = tempDictionary.shuffled().take(value) + learningWord
        return answers.shuffled()
    }

    private fun takeShuffleElements(
        dictionary: List<Word> = emptyList(),
        unLearningWord: List<Word>,
        value: Int,
        learningWord: Word,
    ): List<Word> {
        return if (unLearningWord.size - 1 < value) {
            getAnswerChoices(dictionary, learningWord, value)
        } else {
            getAnswerChoices(unLearningWord, learningWord, value)
        }
    }

    private fun getDictionaryFromFile(): List<Word> {
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

    private fun updateWordsFile(dictionary: List<Word>) {
        val file = File("words.txt")
        file.printWriter().use { writer ->
            dictionary.forEach { word ->
                writer.println("${word.enWord}|${word.ruWord}|${word.correctCount}")
            }
        }
    }
}


