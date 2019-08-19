package superposition

import scala.util.Random

object RandomStringifier {
  def main(args: Array[String]): Unit = {
    for (i <- LazyList.from(0)) {
      if (i % 1000000 == 0) {
        println(i)
      }
      val word = randomWord()
      if (word == "public" || word == "closed") {
        println(word)
      }
    }
  }

  private def randomWord(): String = Random.alphanumeric.filter(c => c.isLower).take(6).mkString("")
}
