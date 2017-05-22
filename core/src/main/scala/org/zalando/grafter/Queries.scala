package org.zalando.grafter

import org.bitbucket.inkytonik.kiama.rewriting.MemoRewriter._

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import Reflect._
import org.bitbucket.inkytonik.kiama.relation._

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

  /** collect the first node (breadth first) of type T in a graph */
  def collectFirst[T : ClassTag, G](graph: G): Option[T] = {
    var found: Option[T] = None

    val collectFirstStrategy =
      breadthfirst(strategy[Any] {
        case t if t.implements[T] =>
          if (found.isEmpty) found = Option(t.asInstanceOf[T])
          Some(t)
        case other => Some(other)
      })

    rewrite(collectFirstStrategy)(graph)
    found
  }

  type Path = List[Product]

  /**
   * collect ancestor paths of a node of type T in a graph and show simple class names
   *  a list of pairs is returned where there can be duplicates in the key because
   *  there can be several instances of the same type
   */
  def ancestorNames[T <: Product : ClassTag, G <: Product](graph: G): List[(String, List[List[String]])] =
    ancestors(graph).toList.map { case (key, value) =>
      (key.getClass.getSimpleName, value.map(_.map(_.getClass.getSimpleName)))
    }

  /** collect all ancestor paths of a node of type T in a graph */
  def ancestors[T <: Product : ClassTag, G <: Product](graph: G): Map[T, List[Path]] = {
    def ancestorsOf(r: Relation[Product, Product])(a: Product): Vector[Vector[Product]] = {
      val image = r(a).distinct
      if (image.isEmpty) Vector(Vector()) else  image.flatMap(i => ancestorsOf(r)(i).map(i +: _).distinct)
    }

    val relation = Query.relation(graph).inverse
    val collected = collect[T, G](graph)
    collected.map(t => (t, ancestorsOf(relation)(t).toList.map(_.toList).distinct)).toMap
  }

  def relation[G <: Product](graph: G, filter: Product => Boolean = (_:Product) => true): Relation[Product, Product] = {
    def makeChildrenRelation(g: Product, r: Relation[Product, Product]): Unit = {
      val children = TreeRelation.treeChildren(g).filter(filter)
      if (children.nonEmpty) {
        children.foreach { c =>
          r.put(g, c)
          makeChildrenRelation(c, r)
        }
      }
    }
    val r = new Relation[Product, Product]
    makeChildrenRelation(graph, r)
    r
  }

}

object Query extends Query with QuerySyntax

/**
 * Syntactic sugar for querying nodes in a graph
 */
trait QuerySyntax {

  implicit class QueryOps[G <: Product](graph: G) {
    def collect[T : ClassTag]: List[T] =
      Query.collect[T, G](graph)

    def collectFirst[T <: Product : ClassTag]: Option[T] =
      Query.collectFirst[T, G](graph)

    def ancestors[T <: Product : ClassTag]: Map[T, List[Query.Path]] =
      Query.ancestors[T, G](graph)

    def ancestorNames[T <: Product : ClassTag]: List[(String, List[List[String]])] =
      Query.ancestorNames[T, G](graph)
  }
}

object QuerySyntax extends QuerySyntax
