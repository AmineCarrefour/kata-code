package wikipedia

import java.io.File

object  WikipediaData extends App{

  private[wikipedia] def filePath = {
    new File(this.getClass.getClassLoader.getResource("wikipedia.dat").toURI).getPath
  }

  private[wikipedia] def parse(line: String): WikipediaArticle = {
    val EndTitle = "</title><text>"
    val indexTitle = line.indexOf(EndTitle)
    val indexPage = 13
    val indexEnd = 14
    val title = line.substring(indexPage, indexTitle)
    val text  = line.substring(indexTitle + EndTitle.length, line.length-indexEnd)
    WikipediaArticle(title, text)
  }

}
