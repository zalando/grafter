### Interfaces

So far we have seen dependencies between components as dependencies between concrete types. For example the `Application`
directly depends on a `PostgresDatabase`. What if we want to use another database? What will be the impact? 

One way to mitigate the impact of changes in software is to use interfaces between components: 

```scala
@reader
case class Application(httpServer: HttpServer, db: Database)

trait Database

@reader
case class PostgresDatabase(dbConfig: DbConfig) extends Database
```

Now `Application` depends on `Database` which is just an interface. But how do we get an instance of a `Database`? Which 
implementation should be picked by the implicit resolution? There is no magic here, we have to explicitly define the 
required instance:
```scala
object Database {
  implicit def reader: Reader[ApplicationConfig, Database] =
    PostgresDatabase.reader
}
```

This can even be generated with the `@defaultReader` annotation:
```scala
@defaultReader[PostgresDatabase]
trait Database
```

The specific implementation is now isolated behind an interface but a problem still lies ahead. If several components require
a database, the implicit resolution will create one fresh database instance for each dependency. This can be a serious
problem if the `Database` components holds precious resources like a database connexions.

We can fix this by making [singletons](singletons.md).

----
Previous: [In a library](library.md)

Next: [Singletons](singletons.md)
