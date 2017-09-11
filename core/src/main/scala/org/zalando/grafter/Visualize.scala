package org.zalando.grafter

import java.lang.reflect.Modifier
import org.zalando.grafter.Visualize._
import scala.util.matching.Regex

trait Visualize {

  type HashCode = Int

  /**
   * Generate a representation of the graph induced by the root component
   * in GraphViz DOT format (http://www.graphviz.org)
   *
   * included can be used to filter out components but keep their dependencies
   * excluded can be used to filter out components and their dependencies (by default only AnyVal nodes are removed)
   */
  def asDotString[T <: Product](root:             T,
                                included:  Product => Boolean,
                                excluded:  Any => Boolean,
                                display:   Option[NodeDisplay]): String = {

    val relation = Query.relation(root, included, excluded)
    val nodes = (relation.domain ++ relation.range).map(p => Node(p)).distinct
    val indexes: Map[HashCode, (Int, Int)] = indexByIdentityHashCode(nodes)

    val indexedNodes = nodes.map(_.setIndexes(indexes))
    val edges = relation.pairs.map { case (s, t) => (Node(s, indexes), Node(t, indexes)) }.distinct.sorted

    dotSpecification(indexedNodes, edges, display)
  }

  /**
   * When a component is not a singleton, we want to distinguish between different instances of the same class
   * in the .dot file / the drawn graph by appending a number to the nodes' names, e.g., "MyComponent # 1". This
   * method returns a map that contains a mapping from identityHashCode to such an index for each component that
   * is not a singleton.
   *
   * The pair (Int, Int) represents (instance number, total number of instances)
   */
  private def indexByIdentityHashCode(nodes: Vector[Node]): Map[HashCode, (Int, Int)] =
    nodes.groupBy(_.p.getClass.getName).flatMap { case (_, vs) =>
      vs.zipWithIndex.map { case (v, i) => (v.hashCode, (i + 1, vs.size)) }
    }

  case class Node(p: Product, indexes: Map[HashCode, (Int, Int)] = Map()) {
    override def toString: String =
      s""""$unquoted""""

    def unquoted: String = {
      val name = p.getClass.getSimpleName.split("\\$").head
      s"""$name$showIndex"""
    }

    def setIndexes(indexes: Map[HashCode, (Int, Int)]): Node =
      copy(indexes = indexes)

    override def hashCode: Int =
      System.identityHashCode(p)

    override def equals(a: Any): Boolean =
      a.hashCode == hashCode

    private def showIndex: String =
      indexes.get(p.identityHashCode).filter(_._2 > 1)
        .map { case (i, total) => s" # $i/$total" }.getOrElse("")
  }

  object Node {
    implicit def nodeOrdering: Ordering[Node] = new Ordering[Node] {
      def compare(x: Node, y: Node): Int =
        implicitly[Ordering[String]].compare(x.toString, y.toString)
    }
  }

  private def dotSpecification(nodes: Vector[Node], arcs: Vector[(Node, Node)], display: Option[NodeDisplay]): String = {
    val nodeFormat =
      if (display.isDefined) "[shape=record]" else "[shape=box]"

    val nodesString =
      nodes.sortBy(_.toString).map(node => makeNode(node, display)).mkString("  ", ";\n  ", ";")

    val arcsString =
      arcs.sortBy(_._1.toString).map(arc => s"${arc._1} -> ${arc._2}").mkString("  ", ";\n  ", ";")

    s"""|strict digraph {
        |  node $nodeFormat;
        |$nodesString
        |$arcsString
        |}""".stripMargin
  }

  private def makeNode(node: Node, display: Option[NodeDisplay]): String =
    display match {
      case Some(NodeDisplay(summary, filter)) =>
        val names = node.p.getClass.getDeclaredFields.toList.map(_.getName)
        val values = node.p.productIterator.toList

        val attributes = summary(node.p) match {
          case Some(s) => List(s)
          case _ =>
            (names zip values).flatMap {
              case (n, v) => filter(v).map(r => s"+ $n=$r")
            }
        }

        if (attributes.isEmpty) s"$node"
        else s"""$node [label = "{${node.unquoted}${attributes.mkString("|", "\\l", "\\l")}}"]"""

      case None =>
        s"$node"
    }

  private implicit class AnyOps(a: Any) {
    def identityHashCode: Int =
      System.identityHashCode(a)
  }
}

object Visualize extends Visualize {

  type AttributesFilter = Any => Option[Any]

  case class NodeDisplay(summary: Product => Option[String], attributes: AttributesFilter)

  /**
   * This filter keeps a component if its package is included and not excluded by
   * the provided regular expressions.
   */
  def packageFilter(includePackages: Regex = ".*".r,
                    excludePackages: Option[Regex] = None): Product => Boolean = (c: Product) => {
    val packageName = c.getClass.getPackage.getName
    val matches  = (r: Regex) => r.findFirstIn(packageName).isDefined
    val included = matches(includePackages)
    val excluded = excludePackages.exists(matches)

    included && !excluded
  }

  val attributesFilter: AttributesFilter = {
    case a: Product if Modifier.isFinal(a.getClass.getModifiers) && a.productArity == 1 =>
      attributesFilter(a.productElement(0))

    case a: Boolean   => Some(a)
    case a: Character => Some(a)
    case a: Byte      => Some(a)
    case a: Short     => Some(a)
    case a: Integer   => Some(a)
    case a: Long      => Some(a)
    case a: Float     => Some(a)
    case a: Double    => Some(a)
    case a: Void      => Some(a)
    case a: String    => Some(a)
    case _            => None
  }

  val nodeDisplay: NodeDisplay =
    NodeDisplay(summary = (p: Product) => None, attributesFilter)

}

/**
 * Syntactic sugar for visualizing a graph
 */
trait VisualizeSyntax {

  implicit class VisualizeSyntaxOps[G <: Product](graph: G) {
    def asDotString: String =
      graph.asDotString()

    def asDotString(included: Product => Boolean  = Visualize.packageFilter(),
                    excluded: Any => Boolean      = Query.isAnyVal,
                    display:  Option[NodeDisplay] = Some(Visualize.nodeDisplay)): String =
      Visualize.asDotString(graph, included, excluded, display)
  }
}

object VisualizeSyntax extends VisualizeSyntax

