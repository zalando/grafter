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
  implicit def reader[A](implicit httpConfigReader: Reader[A, HttpConfig]): Reader[A, HttpServer] =
    genericReader
}
```

This allows us to put the `HttpServer` into a reusable module and build it independently
from the `ApplicationConfig`. The `reader` code boilerplate can also be removed with a `@reader` annotation:
```scala
import org.zalando.grafter.macros._

@reader
case class HttpServer(config: HttpConfig)
```

We now have a nice way to create an application as a set of components, possibly coming from external libraries. However a
crucial piece is missing for component-based systems: interfaces.

----
Previous: [Remove boilerplate](boilerplate.md)

Next: [Interfaces](interfaces.md)



