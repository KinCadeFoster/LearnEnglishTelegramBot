import java.io.File

data class Word(
    val enWord: String,
    val ruWord: String,
    val correctCount: Int,
)


fun main() {
    val dictionary: MutableList<Word> = mutableListOf()
    val wordsFile = File("words.txt")

    for (string in wordsFile.readLines()) {
        try {
            val lineSplit = string.split(" | ")
            dictionary.add(Word(enWord = lineSplit[0], ruWord = lineSplit[1], correctCount = lineSplit[2].toInt()))
        } catch (e: Exception) {
            println("Ошибка чтения строки: \"$string\", строка будет пропущена")
            println(e)
        }
    }
    println(dictionary)
}