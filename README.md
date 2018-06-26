                                ==================================================
                                ==================================================
                                =========Wikipedia rankings using Spark 2=========
                                ==================================================
                                ==================================================

**Run 'WikiRanking.scala' for displaying the output speed of each ranking in the console**
**Run 'WikipediaSuite.scala' for launching unitary tests**

This Kata tests three ways for ranking a dataset by checking each time the latency execution using Spark.
I have placed in the folder 'src/main/resources/' the .dat file which contains 542 Wikipedia pages, there is one page
per line.
First step is the implementation of a method parse in the object WikipediaData object that parses a line of the
dataset and turns it into a WikipediaArticle.
For the sake of simplicity i check that it least one word (delimited by spaces) of the article text is equal
to the given language.

Rank languages attempt #1 : rankLangs :

I Started by implementing methods 'textContains' and 'occurrencesOfLang' so we can compute the number of articles
in an RDD of type RDD[WikipediaArticles] that mention the given language at least once.


Rank languages attempt #2 : rankLangsUsingIndex :

An inverted index is an index data structure storing a mapping from content, such as words or numbers,
to a set of documents.
In our use-case, an inverted index would be useful for mapping from the names of programming
languages to the collection of Wikipedia articles that mention the name at least once.
For instance, the framework Elasticsearch uses a similare structure which is designed to allow very fast full-text searches.
The makeIndex method implemented in the previous part to implement a faster method for computing the language ranking.

'rankLangsUsingIndex' should compute a list of pairs where the second component of the pair
is the number of articles that mention the language (the first component of the pair is the name of the language).

Again, the list should be sorted in descending order. That is, according to this ranking,
the pair with the highest second component (the count) should be the first element of the list.

Rank languages attempt #3: rankLangsReduceByKey

In the case where the inverted index from above is only used for computing the ranking and for no other task
(full-text search, say), it is more efficient to use the reduceByKey method to compute the ranking directly,
without first computing an inverted index.

