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
        val lineSplit = string.split("|")
        if (lineSplit.size == 3)
            dictionary.add(
                Word(
                    enWord = lineSplit[0].trim(),
                    ruWord = lineSplit[1].trim(),
                    correctCount = lineSplit[2].toIntOrNull() ?: 0
                )
            )
    }
    println(dictionary)
}