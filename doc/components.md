
### Grafter components

Grafter components are very simple: they are just case classes (possibly) implementing interfaces.

 1. components can depend on other components by having them as case class members;
 2. the application itself is the top-level component;
 3. the application configuration is a just a case class (can be read from a file if necessary);
 4. each component can be instantiated from the application configuration;
 5. it is possible to recursively build the full application as a *tree* of components from the application configuration;
 6. singletons can be made by rewriting the tree, effectively making it a graph;
 7. the application can be started bottom-up by starting the components extending the `Start` markup trait;
 8. the application can also be stopped top-down by stopping all the components extending the `Stop` markup trait;
 8. mocking the application for testing can also be done by rewriting the tree.

We will see this on a concrete example in the following sections.
