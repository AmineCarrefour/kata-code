package wikipedia

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

import org.apache.spark.rdd.RDD

case class WikipediaArticle(title: String, text: String) {

  def mentionsLanguage(lang: String): Boolean = text.split(' ').contains(lang)

}

object WikipediaRanking {

  /*
   * Spark basic configuration
   */
  val conf = new SparkConf().setMaster("local[3]").setAppName("Wikipedia")
  val sc = new SparkContext(conf)
  val wikiRdd = sc.textFile(WikipediaData.filePath).map(WikipediaData.parse)

  val Langs = List(
    "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
    "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")

  /*
   * (1) Use `occurrencesOfLang` to compute the ranking of the languages
   */
  def textContains(text: String, allText: String) = allText.split(" ").contains(text)

  def occurrencesOfLang(lang: String, rdd: RDD[WikipediaArticle]) = rdd.filter(article => textContains(lang, article.text)).count.toInt

  def rankLangs(langs: List[String], rdd: RDD[WikipediaArticle]) = {
    langs
      .map((lang: String) => (lang, occurrencesOfLang(lang, rdd)))
      .sortBy((occurence: (String, Int)) => -occurence._2)
  }

  /*
   * (2) Compute the language ranking using the inverted index
   */
  def makeIndex(langs: List[String], rdd: RDD[WikipediaArticle]): RDD[(String, Iterable[WikipediaArticle])] = {
    rdd
      .flatMap(
        (articles: WikipediaArticle) => langs.filter((lang: String) => articles.mentionsLanguage(lang))
          .map((article: String) => (article, articles))
      ).groupByKey
  }

  def rankLangsUsingIndex(index: RDD[(String, Iterable[WikipediaArticle])]): List[(String, Int)] =
    index
      .mapValues((iterations: Iterable[WikipediaArticle]) => iterations.size)
      .sortBy((occurences) => -occurences._2)
      .collect
      .toList

  /*
   * (3) Using `reduceByKey` so that the computation of the index and the ranking are combined.
   */
  def rankLangsReduceByKey(langs: List[String], rdd: RDD[WikipediaArticle]): List[(String, Int)] =
    rdd
      .flatMap(
        (articles: WikipediaArticle) => langs.map((lang: String) => (lang, if (articles.mentionsLanguage(lang)) 1 else 0))
      )
      .reduceByKey((c1: Int, c2: Int) => c1 + c2)
      .sortBy((occurence) => -occurence._2)
      .collect
      .toList

  def main(args: Array[String]) {

    timed("\tPart 1: naive ranking", rankLangs(Langs, wikiRdd))

    def index = makeIndex(Langs, wikiRdd)
    timed("\tPart 2: ranking using inverted index", rankLangsUsingIndex(index))

    timed("\tPart 3: ranking using reduceByKey", rankLangsReduceByKey(Langs, wikiRdd))

    println("Output the speed of each ranking : \n" + timing)

    sc.stop()

  }

  val timing = new StringBuffer

  def timed[T](label: String, code: => T): T = {
    val start = System.currentTimeMillis
    val result: T = code
    val stop = System.currentTimeMillis
    timing.append(s"Processing $label took ${stop - start} ms.\n")
    result
  }
}