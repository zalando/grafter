package org.zalando.grafter

import org.bitbucket.inkytonik.kiama.rewriting.MemoRewriter._

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import Reflect._

trait Query {
  
  /** collect all distinct nodes (based on reference equality) of type T in a graph */
  def collect[T : ClassTag, G](graph: G): List[T] = {
    var seen: Vector[Int] = Vector.empty

    // this stores the results
    val results: ListBuffer[T] = new ListBuffer[T]

    val collectStrategy =
      everywheretd(strategy[Any] {
        case t if t.implements[T] =>
          if (seen.contains(System.identityHashCode(t))) Option(t)
          else {
            results.append(t.asInstanceOf[T])
            seen = seen :+ System.identityHashCode(t)
            Option(t)
          }
      })

    rewrite(collectStrategy)(graph)

    results.toList
  }

}

object Query extends Query with QuerySyntax

/**
 * Syntactic sugar for querying nodes in a graph
 */
trait QuerySyntax {

  implicit class QueryOps[G](graph: G) {
    def collect[T : ClassTag]: List[T] =
      Query.collect[T, G](graph)
  }
}

object QuerySyntax extends QuerySyntax


