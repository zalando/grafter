package org.zalando.grafter

import scala.util.matching.Regex
import org.bitbucket.inkytonik.kiama.relation.{Relation, Tree}

trait Visualize {

  type HashCode = Int

  /**
   * Generate a representation of the graph induced by the root component
   * in GraphViz DOT format (http://www.graphviz.org)
   *
   * A filter can be used to filter out components
   */
  def asDotString[T <: Product](root:       T,
                                filter:     Product => Boolean = _ => true,
                                nodeFormat: String = "[shape=box]"): String = {

    val relation = Relation.fromOneStep[Product](root, p => Tree.treeChildren(p).filter(filter))
    val nodes = (relation.domain ++ relation.range).map(p => Node(p)).distinct
    val indexes: Map[HashCode, (Int, Int)] = indexByIdentityHashCode(nodes)

    val indexedNodes = nodes.map(_.setIndexes(indexes))
    val arcs = relation.graph.map { case (s, t) => (Node(s, indexes), Node(t, indexes)) }.distinct

    dotSpecification(indexedNodes, arcs, nodeFormat)
  }

  /**
   * When a component is not a singleton, we want to distinguish between different instances of the same class
   * in the .dot file / the drawn graph by appending a number to the nodes' names, e.g., "MyComponent # 1". This
   * method returns a map that contains a mapping from identityHashCode to such an index for each component that
   * is not a singleton.
   */
  private def indexByIdentityHashCode(nodes: Vector[Node]): Map[HashCode, (Int, Int)] =
    nodes.groupBy(_.p.getClass.getName).flatMap { case (_, vs) =>
      vs.zipWithIndex.map { case (v, i) => (v.hashCode, (i + 1, vs.size)) }
    }

  private case class Node(p: Product, indexes: Map[HashCode, (Int, Int)] = Map()) {
    override def toString: String = {
      val name = p.getClass.getSimpleName.split("\\$").head
      s""""$name$showIndex""""
    }

    def setIndexes(indexes: Map[HashCode, (Int, Int)]): Node =
      copy(indexes = indexes)

    override def hashCode: Int =
      p.identityHashCode

    private def showIndex: String =
      indexes.get(p.identityHashCode).filter(_._2 > 1)
        .map { case (i, total) => s" # $i/$total" }.getOrElse("")
  }

  private def dotSpecification(nodes: Vector[Node], arcs: Vector[(Node, Node)], nodeFormat: String): String = {
    val nodesString =
      nodes.sortBy(_.toString).map(node => s"$node $nodeFormat").mkString("  ", ";\n  ", ";")

    val arcsString =
      arcs.sortBy(_._1.toString).map(arc => s"${arc._1} -> ${arc._2}").mkString("  ", "\n  ", "")

    s"""|strict digraph {
        |$nodesString
        |$arcsString
        |}""".stripMargin
  }

  private implicit class AnyOps(a: Any) {
    def identityHashCode: Int =
      System.identityHashCode(a)
  }
}

object Visualize extends Visualize {

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

}

/**
 * Syntactic sugar for visualizing a graph
 */
trait VisualizeSyntax {

  implicit class VisualizeSyntaxOps[G <: Product](graph: G) {
    def asDotString: String =
      graph.asDotString()

    def asDotString(filter:     Product => Boolean = Visualize.packageFilter(),
                    nodeFormat: String = "[shape=box]"): String =
      Visualize.asDotString(graph, filter, nodeFormat)
  }
}

object VisualizeSyntax extends VisualizeSyntax

