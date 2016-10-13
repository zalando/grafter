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
    val app = FromConfig.create[AppConfig, ExampleApp](appconfig)

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
    implicit def fromConfig: FromConfig[AppConfig, ComponentA] = new FromConfig[AppConfig, ComponentA] {
      def apply(conf: AppConfig): ComponentA = ComponentA(conf.settingA)
    }
  }

  case class ComponentB(setting: SettingB, componentC: ComponentC)
  object ComponentB {
    implicit def fromConfig(implicit
      cFromConfig: FromConfig[AppConfig, ComponentC]): FromConfig[AppConfig, ComponentB] =
      new FromConfig[AppConfig, ComponentB] {

        def apply(conf: AppConfig): ComponentB = ComponentB(conf.settingB, cFromConfig(conf))
      }
  }

  case class ComponentC(setting: SettingC)
  object ComponentC {
    implicit def fromConfig: FromConfig[AppConfig, ComponentC] = new FromConfig[AppConfig, ComponentC] {
      def apply(conf: AppConfig): ComponentC = ComponentC(conf.settingC)
    }
  }

  case class ExampleApp(compA: ComponentA, compB: ComponentB)
}
