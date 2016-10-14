package org.zalando.conf4s

import org.specs2.Specification

class FromConfigSpec extends Specification { def is = s2"""
    FromConfig can be derived genericly $initApplication
"""

  def initApplication = {

    val settingA = SettingA("A")
    val settingB = SettingB(0)
    val settingC = SettingC("C")

    val compA = ComponentA(settingA)
    val compC = ComponentC(settingC)
    val compB = ComponentB(settingB, compC)

    val appconfig = AppConfig(settingA, settingB, settingC)
    val app = FromConfig[AppConfig, ExampleApp](appconfig)

    val haveComponentA = app.compA === compA
    val haveComponentB = app.compB === compB

    haveComponentA and haveComponentB
  }

  // Helpers

  case class AppConfig(settingA: SettingA, settingB: SettingB, settingC: SettingC)
  case class SettingA(str: String)
  case class SettingB(int: Int)
  case class SettingC(str: String)

  case class ComponentA(setting: SettingA)
  object ComponentA {
    implicit def fromConfig: FromConfig[AppConfig, ComponentA] = FromConfig.embed(conf => ComponentA(conf.settingA))
  }

  case class ComponentB(setting: SettingB, componentC: ComponentC)
  object ComponentB {
    implicit def fromConfig(implicit
      cFromConfig: FromConfig[AppConfig, ComponentC]): FromConfig[AppConfig, ComponentB] =
      FromConfig.embed(conf => ComponentB(conf.settingB, cFromConfig(conf)))
  }

  case class ComponentC(setting: SettingC)
  object ComponentC {
    implicit def fromConfig: FromConfig[AppConfig, ComponentC] = FromConfig.embed(conf => ComponentC(conf.settingC))
  }

  case class ExampleApp(compA: ComponentA, compB: ComponentB)
}
