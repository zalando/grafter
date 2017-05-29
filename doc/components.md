
### Grafter components

Grafter components are very simple: they are just case classes implementing interfaces.

##### Dependencies

 1. components can depend on other components by having them as case class members
 
 1. the application configuration is just another component
 
 1. each component can be instantiated from the application configuration
 
 1. the application itself is the top-level component
 
 1. the full application is recursively built as a tree of components from the application configuration
 
##### Singletons 
 
Singletons can be made by rewriting the tree, effectively making it a graph.
 
##### Start / Stop 
 
 1. the application can be started bottom-up by starting the components extending the `Start` markup trait
 
 1. the application can also be stopped top-down by stopping all the components extending the `Stop` markup trait
 
##### Testing 
 
 1. mocking the application for testing can also be done by rewriting the tree

Now let's see how to create a full application step by step.

----
Previous: [Installation](installation.md)

Next: [Your first component](creating.md)
