package org.zalando.conf4s.example

import org.zalando.conf4s.FromConfig


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

  val appconfig = AppConfig(SettingA("sa"), SettingB(0))
  val app: ExampleApp = FromConfig.create[AppConfig, ExampleApp](appconfig)
}
