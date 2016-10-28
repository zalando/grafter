package org.zalando.grafter

import cats.Eval
import org.bitbucket.inkytonik.kiama.==>
import org.bitbucket.inkytonik.kiama.rewriting.Rewriter._
import org.bitbucket.inkytonik.kiama.rewriting.Strategy

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/**
  * Functions for rewriting parts of an object graph representing services or configuration
  * with different implementations.
  *
  * This is used for "Dependency Injection" to replace some service implementations
  * with other implementations.
  *
  */
trait Rewriter {

  /**
    * Take the first value of a given type (approximated with a ClassTag) and replace it everywhere in the graph
    */
  def singleton[S : ClassTag, G](graph: G): G =
    replaceWithStrategy(singletonStrategy[S], graph)

  /**
    * Replace all values of type S, with the same value
    */
  def replace[S : ClassTag, G](s: S, graph: G): G =
    replaceWithStrategy(replaceStrategy[S](s), graph)

  /**
    * Replace with a given strategy
    */
  def replaceWithStrategy[G](strategy: Strategy, graph: G): G =
    rewrite(everywheretd(strategy))(graph)

  /**
    * Replace with a given partial function
    */
  def replaceWith[G, T](s: T ==> Option[T], graph: G): G =
    replaceWithStrategy(strategy(s), graph)

  def singletonStrategy[S](implicit tag: ClassTag[S]): Strategy = {
    var s: Option[S] = None
    strategy[Any] {
      case tag(v) =>
        s match {
          case Some(singleton) =>
            Some(singleton)
          case None =>
            s = Some(v.asInstanceOf[S])
            Some(v)
        }
      case other => None
    }
  }

  def replaceStrategy[S : ClassTag](s: S): Strategy =
    strategy[Any] {
      case v if Reflect.implements(v) =>
        Some(s)
      case other =>
        None
    }

  /** start components from the bottom up */
  def start[G](graph: G): Eval[List[StartResult]] = Eval.later {
    // this map is there to make sure we don't start a node twice
    // this relies on the assumption that different components have different hashcodes
    var started: Vector[Int] = Vector.empty

    // this stores the results
    val results: ListBuffer[StartResult] = new ListBuffer[StartResult]
    var startError = false

    val startStrategy =
      everywherebu(strategy[Any] {
        case s: Start =>
          if (startError || started.contains(s.hashCode)) Option(s)
          else {
            val result = s.start.value
            results.append(result)
            started = started :+ s.hashCode

            result match {
              case r: StartOk => ()
              case r => startError = true
            }
            Option(s)
          }
      })

    rewrite(startStrategy)(graph)

    results.toList
  }

  /**
   * stop components from the top down
   * we try to stop components even if previous components fail to stop
   */
  def stop[G](graph: G): Eval[List[StopResult]] = Eval.later {
    // this map is there to make sure we don't stop a node twice
    // this relies on the assumption that different components have different hashcodes
    var stopped: Vector[Int] = Vector.empty

    // this stores the results
    val results: ListBuffer[StopResult] = new ListBuffer[StopResult]

    val stopStrategy =
      everywheretd(strategy[Any] {
        case s: Stop =>
          if (stopped.contains(s.hashCode)) Option(s)
          else {
            val result = s.stop.value
            results.append(result)
            stopped = stopped :+ s.hashCode
            Option(s)
          }
      })

    rewrite(stopStrategy)(graph)

    results.toList
  }
}

object Rewriter extends Rewriter with RewriterSyntax

/**
  * Syntactic sugar for rewriting nodes in a graph
  *
  * The methods here allow to chain various replacements:
  *
  *  g.singleton[T].replace[S](s)
  */
trait RewriterSyntax {

  implicit class Rewrite[G](graph: G) {
    def singleton[S : ClassTag]: G =
      Rewriter.singleton[S, G](graph)

    def replace[S : ClassTag](s: S): G =
      Rewriter.replace[S, G](s, graph)

    def replaceWith[T](s: T ==> Option[T]): G =
      Rewriter.replaceWith(s, graph)

    def start: Eval[List[StartResult]] =
      Rewriter.start(graph)

    def stop: Eval[List[StopResult]] =
      Rewriter.stop(graph)
  }
}

object RewriterSyntax extends RewriterSyntax

object Reflect {

  /**
    * @return true if A implements the list of types defined by a given class tag
    */
  def implements(a: Any)(implicit ct: ClassTag[_]): Boolean = {
    val types: List[Class[_]] =
      ct.runtimeClass +: ct.runtimeClass.getInterfaces.toList

    types.forall(t => t.isAssignableFrom(a.getClass))
  }
}
