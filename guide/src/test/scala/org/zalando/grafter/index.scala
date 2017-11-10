import org.specs2.io._

import org.zalando.grafter._

/**
 * to publish the site:
 *
 * sbt> cd guide
 * sbt> ~testOnly *index* -- html html.search html.toc html.nostats all
 * sbt> makeSite
 * sbt> ghPagesPushSite
 */
object index extends UserGuidePage { def is = "Welcome to Grafter!".title ^ s2"""
 ${step(copyResources)}

<img style="width:10cm" src="images/grafter.png"/>


Grafter is a dependency-injection library aiming at giving simple ways to compose independently defined components
into a full application which can easily evolve for maintenance or testing.

This user guide is divided in 4 parts:

 1. ${"Quick start" ~ QuickStart mute}
    - install grafter
    - your first application
<p/>

 1. How to
    - ${"create singletons?" ~ CreateSingletons mute}
    - ${"start the application?" ~ StartApplication mute}
    - ${"test the application?" ~ TestApplication mute}
    - ${"test the configuration?" ~ TestConfiguration mute}
    - ${"use the \"finally tagless style\"?" ~ FinallyTagless mute}
<p/>

 1. Understand
    - ${"main concepts" ~ Concepts mute}
<p/>

 1. Discussions
    - ${"how does it compare to other approaches?" ~ Comparisons mute}


"""

  def copyResources = {
    FileSystem.copyDir("src" / "test" / "resources", "target" / "specs2-reports" / "guide").runOption
  }
}
