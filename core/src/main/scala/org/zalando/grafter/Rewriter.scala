package org.zalando.grafter

import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicBoolean

import cats.Eval
import org.bitbucket.inkytonik.kiama.rewriting.{CallbackRewriter, MemoRewriter, Rewritable, Strategy}

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import Reflect._

/**
  * Functions for rewriting parts of an object graph representing services or configuration
  * with different implementations.
  *
  * This is used for "Dependency Injection" to replace some service implementations
  * with other implementations.
  *
  */
trait Rewriter {
  import GrafterMemoRewriter._

  /**
    * Take the first value of a given type (approximated with a ClassTag) and replace it everywhere in the graph
    */
  def singleton[S : ClassTag, G](graph: G): G =
    rewriteWithStrategy(singletonStrategy, graph)

  /**
    * Make singletons of all components
    */
  def singletons[G](graph: G): G =
    singletons[G]((_: Any) => true)(graph)

  /**
   * Make singletons of all components, except the ones
   * not accepted by the predicate
   */
  def singletons[G](predicate: Any => Boolean)(graph: G): G =
    singletonsBy {
      case a if predicate(a) => a.getClass.getName
      case other             => System.identityHashCode(other)
    }(graph)

  /**
    * Make singletons of all components, based
    * on the class name of the component by default and
    * on the class name + the result of the by function
    */
  def singletonsBy[G](by: PartialFunction[Any, Any], bys: PartialFunction[Any, Any]*)(graph: G): G =
    rewrite(everywherebu(singletonsByStrategy(by, bys:_*)))(graph)

  /**
    * Replace all values of type S with the same value
    */
  def replace[S : ClassTag, G](s: S, graph: G): G =
    rewriteWithStrategy(replaceStrategy[S](s), graph)

  /**
    * Replace all values of type S with the same value, returning None if nothing was replaced,
    * or the rewritten graph otherwise.
    */
  def replaceOrFail[S : ClassTag, G](s: S, graph: G): Option[G] = {
    val rewriter = new ReportSuccessRewriter
    val strat = rewriter.strategy[Any] {
      case v if v.implements[S] =>
        Some(s)
    }

    val newGraph = rewriteWithStrategy(strat, graph)

    if (rewriter.successfullyReplaced.get) Some(newGraph) else None
  }

  /**
    * Replace the first value of type S (topdown, with another value
    */
  def replaceFirst[S : ClassTag, G](s: S, graph: G): G =
    rewriteFirstWithStrategy(replaceStrategy[S](s), graph)

  /**
    * Replace with a given strategy (top down)
    */
  def rewriteWithStrategy[G](strategy: Strategy, graph: G): G =
    rewrite(everywheretd(strategy))(graph)
  /**
    * Replace with a given strategy (breadth first)
    */
  def rewriteFirstWithStrategy[G](strategy: Strategy, graph: G): G =
    rewrite(topBreadthfirst(strategy))(graph)

  /**
    * Replace with a given partial function
    */
  def replaceWith[G, T](s: PartialFunction[T, Option[T]], graph: G): G =
    rewriteWithStrategy(strategy(s), graph)

  /**
    * Modify with a given function
    */
  def modify[G, T : ClassTag](f: T => T, graph: G): G =
    rewriteWithStrategy(strategy[T] {
      case t if t.implements[T] => Some(f(t))
    }, graph)

  /**
   * Modify with a given Partial function
   */
  def modifyWith[G, T : ClassTag](f: PartialFunction[T, T], graph: G): G =
    rewriteWithStrategy(strategy[T] {
      case t if t.implements[T] => Some(f.applyOrElse(t, (t1: T) => t1))
    }, graph)

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
    }
  }

  def singletonsByStrategy(by: PartialFunction[Any, Any], bys: PartialFunction[Any, Any]*): Strategy = {
    val singletons: scala.collection.mutable.HashMap[Any, Any] =
      new scala.collection.mutable.HashMap[Any, Any]

    val discriminate = bys.foldLeft(by)(_ orElse _).lift

    strategy[Any] {
      case v if canBeSingleton(v) =>
        val className = v.getClass.getName
        val key =
          discriminate(v) match {
            case Some(discriminant) => (className, discriminant)
            case None               => className
          }

        singletons.get(key) match {
          case Some(s) =>
            Some(s)

          case None =>
            singletons.put(key, v)
            Some(v)
        }
    }
  }

  def replaceStrategy[S : ClassTag](s: S): Strategy =
    strategy[Any] {
      case v if v.implements[S] =>
        Some(s)
    }

  /** start components from the bottom up */
  def start[G](graph: G): Eval[List[StartResult]] = Eval.later {
    // this map is there to make sure we don't start a node twice
    // this is useful when there are singletons
    var started: Vector[Int] = Vector.empty

    // this stores the results
    val results: ListBuffer[StartResult] = new ListBuffer[StartResult]
    var startError = false

    val startStrategy =
      everywherebu(strategy[Any] {
        case s: Start =>
          if (startError || started.contains(System.identityHashCode(s))) Option(s)
          else {
            val result = s.start.value
            results.append(result)
            started = started :+ System.identityHashCode(s)

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
    // this is useful when there are singletons
    var stopped: Vector[Int] = Vector.empty

    // this stores the results
    val results: ListBuffer[StopResult] = new ListBuffer[StopResult]

    val stopStrategy =
      everywheretd(strategy[Any] {
        case s: Stop =>
          if (stopped.contains(System.identityHashCode(s))) Option(s)
          else {
            val result = s.stop.value
            results.append(result)
            stopped = stopped :+ System.identityHashCode(s)
            Option(s)
          }
      })

    rewrite(stopStrategy)(graph)

    results.toList
  }

  def topBreadthfirst(s: Strategy): Strategy = {
    def bf(s1: =>Strategy): Strategy =
      one(s1) <+ one(bf(s1))

    s <+ bf(s)
  }

  private def canBeSingleton(v: Any): Boolean = v match {
    case _ : Rewritable => !Modifier.isFinal(v.getClass.getModifiers)
    case _ : Product    => !Modifier.isFinal(v.getClass.getModifiers)
    case _              => false
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

    def singletons: G =
      Rewriter.singletons[G](graph)

    def singletons(predicate: Any => Boolean): G =
      Rewriter.singletons(predicate)(graph)

    def singletonsBy(by: PartialFunction[Any, Any], bys: PartialFunction[Any, Any]*): G =
      Rewriter.singletonsBy(by, bys:_*)(graph)

    def replace[S : ClassTag](s: S): G =
      Rewriter.replace[S, G](s, graph)

    def replaceOrFail[S : ClassTag](s: S): Option[G] =
      Rewriter.replaceOrFail[S, G](s, graph)

    def replaceFirst[S : ClassTag](s: S): G =
      Rewriter.replaceFirst[S, G](s, graph)

    def replaceWith[T](s: PartialFunction[T, Option[T]]): G =
      Rewriter.replaceWith(s, graph)

    def modify[T : ClassTag](f: T => T): G =
      Rewriter.modify(f, graph)

    def modifyWith[T : ClassTag](f: PartialFunction[T, T]): G =
      Rewriter.modifyWith(f, graph)

    def start: Eval[List[StartResult]] =
      Rewriter.start(graph)

    def stop: Eval[List[StopResult]] =
      Rewriter.stop(graph)
  }
}

object RewriterSyntax extends RewriterSyntax

private object GrafterMemoRewriter extends MemoRewriter {
  override def dup[T <: Product](t : T, children : Array[AnyRef]) : T =
    try super.dup(t, children map unbox)
    catch { case e: Throwable => t }

  /** value classes must be unboxed before duplication */
  def unbox(s: AnyRef): AnyRef = {
    val klass = s.getClass
    s match {
      case p: Product if Modifier.isFinal(klass.getModifiers) && p.productArity == 1 =>
        p.productElement(0).asInstanceOf[AnyRef]
      case _ =>
        s
    }
  }

}

private class ReportSuccessRewriter extends CallbackRewriter {
  val successfullyReplaced = new AtomicBoolean(false)

  override def rewriting[T](oldTerm: T, newTerm: T): T = {
    successfullyReplaced.set(true)
    newTerm
  }
}
