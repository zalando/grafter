package org.zalando.grafter

package object syntax {

  object all extends RewriterSyntax with QuerySyntax

  object rewriter extends RewriterSyntax
  object query extends QuerySyntax

}
