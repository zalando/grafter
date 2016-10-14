package org.zalando.grafter.env

sealed trait EnvError
final case class EnvNotFound(name: String) extends EnvError
final case class EnvParseError(name: String, raw: String) extends EnvError
final case class FromStrError(message: String) extends EnvError
