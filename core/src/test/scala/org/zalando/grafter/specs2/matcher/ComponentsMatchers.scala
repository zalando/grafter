package org.zalando.grafter.specs2.matcher

import org.bitbucket.inkytonik.kiama.rewriting.Rewriter
import org.specs2.matcher._
import cats.instances.tuple._
import cats.syntax.bifunctor._

trait ComponentsMatchers {

  /**
   * Check that a given component graph contains
   * the expected number of instances per classes
   */
  def containInstances[T <: Product](expected: (Class[_], Int)*): Matcher[T] =
    ContainsInstancesMatcher(expected.toList)
}

case class ContainsInstancesMatcher[T <: Product](expected: List[(Class[_], Int)]) extends Matcher[T] {

  private val expectedCountSimpleNames: List[(String, Int)] =
    expected.map(_.leftMap(_.getSimpleName))

  private val expectedCount: List[(String, Int)] =
    expectedCountSimpleNames.sortBy(_._1)

  def apply[S <: T](expectable: Expectable[S]): MatchResult[S] = {
    val components = expectable.value

    val componentNames = Rewriter.collectall { case component: Any =>
      Vector(System.identityHashCode(component) -> component.getClass.getSimpleName)
    }.apply(components)

    val actualCount: Map[String, Int] =
      countByName(componentNames.toMap.values)

    val actualCountNames: List[String] =
      actualCount.keys.toList

    val missing =
      expectedCount.collect { case (name, count) if !actualCountNames.contains(name) => s"$name -> $count"}

    val existing =
      expectedCount.collect { case (name, count) => actualCount.get(name).map(c => (name, c, count)) }.flatten

    val different =
      existing.collect { case (n, a, e) if a != e => s"$n -> actual: $a, expected: $e" }

    val successMessage =
      s"""|${components.getClass.getSimpleName}
          |contains
          | ${actualCount.mkString("\n  ")}""".stripMargin

    val failureMessage =
      s"""|${components.getClass.getSimpleName}
          |${if (missing.nonEmpty)   missing.mkString("is missing\n  ", "\n  ", "") else ""}
          |${if (different.nonEmpty) different.mkString("doesn't have the right number of components for\n  ", "\n  ", "") else ""}""".stripMargin


    result(missing.isEmpty && different.isEmpty,
           successMessage, failureMessage, expectable)
  }

  private def countByName(names: Iterable[String]): Map[String, Int] =
    names.groupBy(identity).mapValues(_.size)

}

object ComponentsMatchers extends ComponentsMatchers
