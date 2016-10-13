package org.zalando.conf4s

import cats.Eval
import cats.data.{Xor, XorT}

package object env {

  type EnvIO[A] = XorT[Eval, EnvError, A]

  def readEnv[A](name: String)(implicit aFromStr: FromStr[A]): EnvIO[A] = XorT {
    Eval.always {
      for {
        raw <- Xor.fromOption(sys.env.get(name), EnvNotFound(name))
        v   <- aFromStr(raw)
      } yield v
    }
  }
}
