### Test the application

For integration testing you generally need to replace components which are
at the frontier of your system and deeply embedded in your application.

This is can be done once again with `Rewriter` and the `replace` method:

```tut
import org.zalando.grafter._, Rewriter._

object mockDb extends Database {
  // mock the database operations
}

// you can also rewrite the prod configuration!
val testConfiguration =
  Application.prod.
    replace[HttpConfig](HttpConfig("localhost", 8080))

// create the application
// from the test config
// and mock the database
val application: Application =
  Application.reader.apply(testConfiguration).
    singleton[Database].
    replace[Database](mockDb)
```

*Note*: due to a limitation with the rewriting, final case classes with one arguments *cannot* be replaced!

It is also very important to test that the [application itself is properly configured](inspect-app-graph.md).

----
Previous: [Start and stop](start-stop.md)

Next: [Inspect the graph](inspect-app-graph.md)
