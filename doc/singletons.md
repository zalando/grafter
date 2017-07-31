### Singletons

Here we will make sure that however deep our application graph is, we will
always use, in this case, one database, even if two different components
depend on it. This is done with the `Rewriter` object, whose operations are
enabled with the following `import`:

```tut:silent:fail
import org.zalando.grafter.syntax.rewriter._
```

We can then call the `singletons` method on our application object to
effectively rewrite the component tree and remove the duplicated objects.
The following code shows three different ways of doing this:

```tut:silent:fail
val app1: Application =
  application.singleton[Database]
  
// make several singletons at once, based on a predicate
val app2: Application =
  application.singletons(_.getClass.getName.startsWith("org.acme"))

// make a singleton for each component of the application
val app3: Application =
  application.singletons
```

Note that `grafter` will only try to make a singleton for classes which are instances of
`scala.Product` or which implement Kiama's `org.bitbucket.inkytonik.kiama.rewriting.Rewritable`
trait with the `singletons` method ([Kiama](https://bitbucket.org/inkytonik/kiama) is the library powering the rewriting functionality of grafter). 
It will also _not_ make singletons for `case class`es that extend `AnyVal` or that are marked as `final`. 
This allows `case class`es representing `String` or `Int` parameters (i.e. _value classes_) to _not_ be turned into singletons.

```tut:silent
// instances of these classes will fortunately not
// be made singletons (otherwise everything will have the same port!)
case class Port(value: Int) extends AnyVal
case class DbUrl(value: String) extends AnyVal
```

***Very important***

Singletons are made based on the class name of a component, not its full type. This means that you
could have runtime exceptions if you had parametrized components

```tut:silent:nofail
case class C[T](t: T)

case class App(c1: C[String], c2: C[Int])

// BOOM!
App(C(""), C(1)).singletons
```

In the example above making a singleton for `C` will take the first instance found, `c1` and assign it 
to `c2` which would be incorrect.

### Re-duplication

Most components in a application should be made singletons but not all. For example you might want to have 2 execution
contexts with different configurations. Tree rewriting can also be used to that effect with the `modifyWith` and `replace` methods:
```tut:silent:fail
import org.zalando.grafter.syntax.rewriter._

val app: Application = 
  application.singletons
    .modifyWith { case s: HttpServer     => s.replace[ExecutionContextConfig](cpuExecutionContextConfig)}
    .modifyWith { case s: BackendService => s.replace[ExecutionContextConfig](ioExecutionContextConfig)}
```

The application has the exact desired shape and we can now [start it](start-stop.md)

----
Previous: [Interfaces](interfaces.md)

Next: [Start and stop](start-stop.md)
