package org.zalando.conf4s.example

import cats.Eval
import cats.data.{XorT, Xor}
import org.zalando.conf4s.{FromConfig, ConfigRewriter}
import ConfigRewriter._
import org.zalando.conf4s.env._
import org.bitbucket.inkytonik.kiama.==>


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

  val loadSettingBFromEnv: EnvIO[Int] = readEnv[Int]("SETTINGB_SB")

  val appconfig = AppConfig(SettingA("sa"), SettingB(0))
  val app: ExampleApp = FromConfig.create[AppConfig, ExampleApp](appconfig)

  val replacedConfig = loadSettingBFromEnv.map(sb => {
    val replaceSb: SettingB ==> Option[SettingB] = { case settingb: SettingB => Some(settingb.copy(sb = sb)) }
    appconfig.replaceWith(replaceSb)
  })
  val envApp: EnvIO[ExampleApp] = replacedConfig.map(FromConfig.create[AppConfig, ExampleApp])
}
