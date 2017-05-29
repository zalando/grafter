### Start the application

Now the application can be started, using the `Rewriter` again which is
going to traverse the application graph and start each component implementing
`Start` from the bottom up. If you scroll up you will see that `PostgresDatabase`
is such a component and must implement a `start` method returning a `StartResult`.

```scala
import cats._

val started: Eval[List[StartResult]] =
  Rewriter.start(app)
```

The `List[StartResult]` can be used to diagnose the start up and
produce a nice error message if something went wrong.

### Stop the application

The application can also be stopped using the `Rewriter`. It will stop
each component implementing `Stop` from the top down.

```scala
import cats._

val stop: Eval[List[StopResult]] =
  Rewriter.stop(app)
```

The major difference between the start and the stop strategies is that
*all* the components will try to be stopped regardless of failures.

The `List[StopResult]` can be used to diagnose the shutdown and
produce a nice error message if something went wrong.


Wiring the application and starting is done, let's push to production. Or, should we do a bit of [testing](testing.md) first :-)?

----
Previous: [Singletons](singletons.md)

Next: [Testing](testing.md)
