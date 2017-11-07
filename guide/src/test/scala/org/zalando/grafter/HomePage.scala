package org.zalando.grafter

object HomePage extends UserGuidePage { def is = "Grafter home".title ^ s2"""

Welcome to Grafter!

Grafter is a dependency-injection library aiming at giving simple ways to compose independently defined components
into a full application which can easily evolve for maintenance or testing.

This user guide is divided in 4 parts:

 1. ${"Quick start" ~ QuickStart mute}
    - install grafter
    - your first application
<p/>

 1. How to
    - ${"create singletons" ~ CreateSingletons mute}?
    - ${"start the application" ~ StartApplication mute}?
    - ${"test the application" ~ TestApplication mute}?
<p/>

 1. Understand
    - ${"main concepts" ~ Concepts mute}
<p/>

 1. Discussions
    - how does it compare to xxx?


"""
}
