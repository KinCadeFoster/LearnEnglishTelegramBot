import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    wordsFile.createNewFile()

    for (string in wordsFile.readLines())
    println(string)
}