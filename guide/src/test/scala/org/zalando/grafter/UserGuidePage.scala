package org.zalando.grafter

import org.specs2.Specification
import org.specs2.main.Arguments
import org.specs2.specification.Snippets
import org.specs2.specification.core.SpecStructure

import scala.io.Source

/**
 * Base class for creating user guide pages
 */
abstract class UserGuidePage extends Specification with Snippets {

  override def map(structure: SpecStructure) =
    super.map(structure.setArguments(Arguments("html.nostats")).map(_.compact))


  def version: String =
    Source.fromFile("../version.sbt").getLines.toList.head.trim.split(" ").last.replace("\"", "")
}
