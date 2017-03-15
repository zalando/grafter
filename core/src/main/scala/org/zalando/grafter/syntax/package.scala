package org.zalando.grafter

package object syntax {

  object all extends
    RewriterSyntax with
    QuerySyntax    with
    VisualizeSyntax

  object rewriter  extends RewriterSyntax
  object query     extends QuerySyntax
  object visualize extends VisualizeSyntax

}
