import java.io.File

fun main() {
    val wordsFile = File("words.txt")

    for (string in wordsFile.readLines())
    println(string)
}