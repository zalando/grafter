package org.zalando.grafter

import org.specs2.Specification
import org.specs2.specification.Snippets

import scala.io.Source

/**
 * Base class for creating user guide pages
 */
abstract class UserGuidePage extends Specification with Snippets {

  def version: String =
    Source.fromFile("../version.sbt").getLines.toList.head.trim.split(" ").last.replace("\"", "")

}
