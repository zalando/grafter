package org.zalando.grafter

object CreateSingletons extends UserGuidePage { def is = "Singletons".title ^ s2"""

### Make singletons

When an application is instantiated with a `Reader` instance, it gets created as a tree of components with distinct
instances for the same type:
```
                            +-------+
                            |  App  |
                            +-------+
                                |
                         +--------------+
              +----------|  HttpServer  |-------+
              |          +--------------+       |
              |                                 |
     +-------------------+              +----------------+
     | GetCustomersRoute |              | GetOrdersRoute |
     +-------------------+              +----------------+
               |                           |            |
  +-----------------+        +-----------------+    +--------------+
  | CustomerService |        | CustomerService |    | PriceService |
  +-----------------+        +-----------------+    +--------------+
       |                            |                   |
  +------------+             +------------+          +------------+
  | HttpClient |             | HttpClient |          | HttpClient |
  +------------+             +------------+          +------------+
       |                             |                  |
  +------------------+       +------------------+   +------------------+
  | HttpClientConfig |       | HttpClientConfig |   | HttpClientConfig |
  +------------------+       +------------------+   +------------------+

```

The trouble with this setup is that some components might hold onto precious resources, like a thread-pool. In that situation
You don't want them to be duplicated. You can make all the components in your tree become singletons with the `singletons`
method: ${snippet{
// 8<--
trait Application
trait Config
object Config extends Config { def prod = this }
object Application extends Application {
  def reader(config: Config) = this
}
// 8<--
import org.zalando.grafter.syntax.rewriter._

val app: Application =
  Application.reader(Config.prod).singletons
}}

This call to `singletons` is going to *rewrite* your application tree into a graph with singletons:
```
                            +-------+
                            |  App  |
                            +-------+
                                |
                         +--------------+
              +----------|  HttpServer  |------+
              |          +--------------+      |
              |                                |
     +-------------------+              +----------------+
     | GetCustomersRoute |              | GetOrdersRoute |
     +-------------------+              +----------------+
              |                              |
  +-----------------+             +--------------+
  | CustomerService |             | PriceService |
  +-----------------+             +--------------+
                 |                       |
            +------------+               |
            | HttpClient |---------------+
            +------------+
                 |
            +------------------+
            | HttpClientConfig |
            +------------------+

```

### Make singletons based on values

#### Modify components

The previous rewrite uses the type of components to make singletons. In reality we might want a finer-grained strategy,
based on components *values*. This means that we might first want to set specific values on specific components.

This can be done with the `modifyWith` method. For example let's say `HttpClientConfig` contains `Uri` parameter pointing to a
service we want to access. We want the `CustomerService` and the `PriceService` to get different configurations: ${snippet{
// 8<--
trait Application
trait Config {
  def customersUri: String = ""
  def pricesUri: String = ""
}
object Config extends Config { def prod = this }
object Application extends Application {
  def reader(config: Config) = this
}
trait CustomerService
trait PriceService
// 8<--
import org.zalando.grafter.syntax.rewriter._

val app: Application =
  Application.reader(Config.prod).
    modifyWith[Any] {
      case c: CustomerService => c.replace(Config.prod.customersUri)
      case c: PriceService    => c.replace(Config.prod.pricesUri)
    }
}}

This is the resulting graph:
```
                            +-------+
                            |  App  |
                            +-------+
                                |
                         +--------------+
              +----------|  HttpServer  |-------+
              |          +--------------+       |
              |                                 |
     +-------------------+              +----------------+
     | GetCustomersRoute |              | GetOrdersRoute |
     +-------------------+              +----------------+
               |                           |            |
  +-----------------+        +-----------------+    +--------------+
  | CustomerService |        | CustomerService |    | PriceService |
  +-----------------+        +-----------------+    +--------------+
       |                            |                   |
  +------------+             +------------+          +------------+
  | HttpClient |             | HttpClient |          | HttpClient |
  +------------+             +------------+          +------------+
       |                             |                  |
  +------------------+       +------------------+   +------------------+
  | HttpClientConfig |       | HttpClientConfig |   | HttpClientConfig |
  +------------------+       +------------------+   +------------------+
  | uri = "http://c" |       | uri = "http://c" |   | uri = "http://p" |
  +------------------+       +------------------+   +------------------+
```


#### Make the singletons

The next step is to make singletons across the whole application except for the components having a specific configuration
which we want to preserve! This can be done with the `singletonsBy` method taking partial functions to make
singletons based on values: ${snippet{
// 8<--
trait Application
trait Config {
  def customersUri: String = ""
  def pricesUri: String = ""
}
object Config extends Config { def prod = this }
object Application extends Application {
  def reader(config: Config) = this
}
trait CustomerService
trait PriceService
trait HttpClient { def config: HttpClientConfig }
trait HttpClientConfig
// 8<--
import org.zalando.grafter.syntax.rewriter._

lazy val app: Application =
  Application.reader(Config.prod).
    modifyWith[Any] {
    case c: CustomerService => c.replace(Config.prod.customersUri)
    case c: PriceService    => c.replace(Config.prod.pricesUri)
  }.singletonsBy(httpClientSingletons)

lazy val httpClientSingletons: PartialFunction[Any, Any] = {
  case c: HttpClient => c.config
  case c: HttpClientConfig => c
}
}}

This leads to the final application graph:
```
                            +-------+
                            |  App  |
                            +-------+
                                |
                         +--------------+
              +----------|  HttpServer  |-------+
              |          +--------------+       |
              |                                 |
     +-------------------+              +----------------+
     | GetCustomersRoute |              | GetOrdersRoute |
     +-------------------+              +----------------+
               |                           |        |
  +-----------------+                      |  +--------------+
  | CustomerService |----------------------+  | PriceService |
  +-----------------+                         +--------------+
          |                                         |
  +------------+                               +------------+
  | HttpClient |                               | HttpClient |
  +------------+                               +------------+
          |                                         |
  +------------------+                        +------------------+
  | HttpClientConfig |                        | HttpClientConfig |
  +------------------+                        +------------------+
  | uri = "http://c" |                        | uri = "http://p" |
  +------------------+                        +------------------+
```
"""
}
