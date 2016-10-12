package org.zalando.conf4s.example

import cats.Eval
import cats.data.{XorT, Xor}
import org.zalando.conf4s.{FromConfig, ConfigRewriter}


case class AppConfig(settingA: SettingA, settingB: SettingB)
case class SettingA(sa: String)
case class SettingB(sb: Int)

case class CompA(setting: SettingA)
object CompA {
  implicit def fromConfig = new FromConfig[AppConfig, CompA] {
    def apply(conf: AppConfig): CompA = CompA(conf.settingA)
  }
}

case class CompB(setting: SettingB)
object CompB {
  implicit def fromConfig = new FromConfig[AppConfig, CompB] {
    def apply(conf: AppConfig): CompB = CompB(conf.settingB)
  }
}

case class ExampleApp(compA: CompA, compB: CompB)

object Example  {

  case class EnvError(message: String)
  type EnvIO[A] = XorT[Eval, EnvError, A]

  val loadSettingBFromEnv: EnvIO[Int] = XorT {
    Eval.always {
      for {
        raw <- Xor.fromOption(sys.env.get("SETTING_B"), EnvError("no value found for env SETTING_B"))
        v   <- Xor.catchNonFatal(raw.toInt).leftMap(_ => EnvError(s"can not convert $raw to Int"))
      } yield v
    }
  }

  val appconfig = AppConfig(SettingA("sa"), SettingB(0))
  val app: ExampleApp = FromConfig.create[AppConfig, ExampleApp](appconfig)

  val replacedConfig = ConfigRewriter.replaceF(loadSettingBFromEnv, appconfig)
  val envApp: EnvIO[ExampleApp] = replacedConfig.map(FromConfig.create[AppConfig, ExampleApp])
}
