
### Introduction

Grafter adds to constructor injection just the necessary support to:

 - instantiate a component-based application from configuration;
 - fine-tune the wiring (create singletons and replace specific components);
 - test the application by replacing components;
 - start / stop the application.

Grafter is targeting every possible application because it focuses on associating just 3 ideas:

 - case classes and interfaces for components;
 - `Reader` instances and implicit resolution for wiring components;
 - [tree rewriting](http://www.program-transformation.org/Transform/TreeRewriting) and [kiama](https://bitbucket.org/inkytonik/kiama) for everything else.

Please try it and report your experience:

 - how is it better / worse than another library?
 - is the core model more approachable than other libraries?
 - what could be improved?

----
Previous: [Installation](installation.md)

Next: [Grafter components](components.md)
