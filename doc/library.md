### In a library

The `HttpServer` presented earlier statically depends on the `ApplicationConfig`:

```scala
object HttpServer {
  implicit def reader: Reader[ApplicationConfig, HttpServer] =
    genericReader
}
```

If you are creating a library, you will probably want to avoid this. To do it, lets parameterize
the `reader` function with some config of type `A`:

```scala
object HttpServer {
  implicit def dependentReader[A](implicit httpConfigReader: Reader[A, HttpConfig]): Reader[A, HttpServer] = 
    genericReader
}
```

This allows us to put the `HttpServer` into a reusable module and build it independently
from the `ApplicationConfig`. The `dependentReader` code boilerplate can also be removed with a `dependentReader` annotation:
```scala
import org.zalando.grafter.macros._

@dependentReader
case HttpServer(config: HttpConfig)
```

We now have a nice way to create an application as a set of components, possibly coming from external libraries. However a
crucial piece is missing for component-based systems: interfaces.

#### Alternative

Here is another nice possibility. In the library define:
```scala
import org.zalando.grafter.macros._

@reader[HttpConfig]
case HttpServer(config: HttpConfig)
```

And in your application define:
```scala

import org.zalando.grafter.macros._
import org.zalando.grafter.GenericReader.composeReaders

@readers
case class ApplicationConfig(httpConfig: HttpConfig)

object ApplicationConfig {
  implicit def compose[A]: Reader[ApplicationConfig, A] =
    composeReaders
}
```

What does that all mean?

 1. the `@reader` annotation on `HttpServer` creates an implicit `Reader[HttpConfig, HttpServer]`.
 2. the `@readers` annotation creates an implicit `Reader[ApplicationConfig, HttpConfig]`
 3. now we need a `Reader[ApplicationConfig, HttpServer]` in order to use the `HttpServer` in our application
    we can get one by composing the first and second readers. This is what `compose` does


----
Previous: [Remove boilerplate](boilerplate.md)

Next: [Interfaces](interfaces.md)



