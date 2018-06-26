package wikipedia

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import wikipedia.WikipediaData.parse
import WikipediaRanking._

class WikipediaSuite extends FlatSpecLike with BeforeAndAfterAll with Matchers with GivenWhenThen {

  def initializeWikipediaRanking(): Boolean =
    try {
      WikipediaRanking
      true
    } catch {
      case ex: Throwable =>
        println(ex.getMessage)
        ex.printStackTrace()
        false
    }

  override def beforeAll(): Unit = {
    assert(initializeWikipediaRanking)
  }

  override def afterAll(): Unit = {
    sc.stop()
  }

  "A Wikipedia page" should "be well parsed" in {
    Given("A valid Wiki page")
    val Page = "<page>" +
      "<title>Compagnie Financiere Edmond de Rothschild</title>" +
      "<text>#REDIRECT [[Compagnie Financière Edmond de Rothschild]] {{R from title without diacritics}}</text>" +
      "</page>"
    val article = WikipediaArticle("Compagnie Financiere Edmond de Rothschild", "#REDIRECT [[Compagnie Financière Edmond de Rothschild]] {{R from title without diacritics}}")
    parse(Page) shouldBe article
  }

  "A Wikipedia page" should "not be well parsed" in {
    Given("A invalid Wiki page")
    val Page = "</page><page>" +
      "<title>Compagnie Financiere Edmond de Rothschild</title>" +
      "<text>#REDIRECT [[Compagnie Financière Edmond de Rothschild]] {{R from title without diacritics}}</text></page>"
    val article = WikipediaArticle("Compagnie Financiere Edmond de Rothschild", "#REDIRECT [[Compagnie Financière Edmond de Rothschild]] {{R from title without diacritics}}")
    parse(Page) shouldNot be theSameInstanceAs article
  }

  "'rankLangs'" should "work for RDD with one element" in {
    Given("A invalid Wiki page")
    val Languages = List("Scala", "Java")
    val articles = List(
        WikipediaArticle("1", "Scala is great !")
    )
    val rdd = sc.parallelize(articles)
    val ranked = rankLangs(Languages, rdd)
    ranked.head._1 shouldBe "Scala"
  }

  "'rankLangs'" should "sort elements in descending order" in {
    Given("a RDD with 3 elements")
    val Languages = List("Scala", "Java", "Haskell")
    val articles =  List(
        WikipediaArticle("1", "Scala is great, Java"),
        WikipediaArticle("2", "Java is OK, but Scala is cooler"),
        WikipediaArticle("3", "Haskell is better than Scala")
    )
    val rdd = sc.parallelize(articles)
    val ranked = rankLangs(Languages, rdd)
    ranked.head._1 shouldBe "Scala"
  }

  "'makeIndex'" should "creates 0 indexes" in {
    Given("Empty entry")
    val Languages = Nil
    val articles = List(
        WikipediaArticle("1", "Groovy is pretty interesting, and so is Erlang"),
        WikipediaArticle("2", "Scala and Java run on the JVM"),
        WikipediaArticle("3", "Scala is not purely functional"),
        WikipediaArticle("3", "Scala Scala Java")
    )
    val rdd = sc.parallelize(articles)
    val index = makeIndex(Languages, rdd)
    index.count shouldBe 0
  }

  "'makeIndex'" should "creates 3 indexes" in {
    Given("Three entries")
    val Languages = List("Scala", "Java", "Erlang")
    val articles = List(
      WikipediaArticle("1", "Groovy is pretty interesting, and so is Erlang"),
      WikipediaArticle("2", "Scala and Java run on the JVM"),
      WikipediaArticle("3", "Scala is not purely functional"),
      WikipediaArticle("4", "Scala Scala Java")
    )
    val rdd = sc.parallelize(articles)
    val index = makeIndex(Languages, rdd)
    index.count shouldBe 3
  }

  "'rankLangsUsingIndex'" should "not work for a simple RDD with no elements" in {
    import WikipediaRanking._
    val Languages = Nil
    val articles = List(
      WikipediaArticle("1","Groovy is pretty interesting, and so is Erlang"),
      WikipediaArticle("2","Scala and Java run on the JVM"),
      WikipediaArticle("3","Scala is not purely functional")
    )
    val rdd = sc.parallelize(articles)
    val index = makeIndex(Languages, rdd)
    val ranked = rankLangsUsingIndex(index)
    ranked shouldBe empty
  }

  "'rankLangsUsingIndex'" should "work for a simple RDD with three elements" in {
     import WikipediaRanking._
     val Languages = List("Scala", "Java")
     val articles = List(
       WikipediaArticle("1","Groovy is pretty interesting, and so is Erlang"),
       WikipediaArticle("2","Scala and Java run on the JVM"),
       WikipediaArticle("3","Scala is not purely functional")
     )
     val rdd = sc.parallelize(articles)
     val index = makeIndex(Languages, rdd)
     val ranked = rankLangsUsingIndex(index)
    ranked should have size  2
     val res = ranked.head._1
     res shouldBe "Scala"
   }

  "'rankLangsReduceByKey'" should "work for a simple RDD with empty list" in {
    import WikipediaRanking._
    val Languages = Nil
    val articles = List(
      WikipediaArticle("1","Groovy is pretty interesting, and so is "),
      WikipediaArticle("2","Scala and Java run on the JVM"),
      WikipediaArticle("3","Scala is not purely functional"),
      WikipediaArticle("4","The cool kids like Haskell more than Java"),
      WikipediaArticle("5","Java is for enterprise developers")
    )
    val rdd = sc.parallelize(articles)
    val ranked = rankLangsReduceByKey(Languages, rdd)
    ranked should have size  0
  }

  "'rankLangsReduceByKey'" should "work for a simple RDD with four elements" in {
    import WikipediaRanking._
    val Languages = List("Scala", "Java", "Groovy", "Haskell", "Erlang")
    val articles = List(
      WikipediaArticle("1","Groovy is pretty interesting, and so is "),
      WikipediaArticle("2","Scala and Java run on the JVM"),
      WikipediaArticle("3","Scala is not purely functional"),
      WikipediaArticle("4","The cool kids like Haskell more than Java"),
      WikipediaArticle("5","Java is for enterprise developers")
    )
    val rdd = sc.parallelize(articles)
    val ranked = rankLangsReduceByKey(Languages, rdd)
    ranked should have size  5
    ranked.head._1 shouldBe "Java"
    ranked.tail shouldBe (List(("Scala",2), ("Groovy",1), ("Haskell",1), ("Erlang",0)))
  }

}