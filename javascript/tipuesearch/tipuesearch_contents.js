
var tipuesearch = {"pages": [{"title":"Quick Start", "text":" InstallationTo use Grafter you need add it as a dependency in your sbt build settings:scalalibraryDependencies += \"org.zalando\" %% \"grafter\" % \"2.4.0\"// you also need the macros plugin for the grafter annotationsaddCompilerPlugin(\"org.scalamacros\" % \"paradise\" % \"2.1.1\" cross CrossVersion.full) Your first applicationHere is a minimal example of an application using grafter import org.zalando.grafter.macros.{readers, reader, defaultReader}import org.zalando.grafter.{Start, StartResult}import org.zalando.grafter.syntax.rewriter._import cats.Eval// CONFIGURATION@readerscase class ApplicationConfig( http: HttpConfig, db:   DbConfig)case class HttpConfig(host: String, port: Int)case class DbConfig(url: String)// COMPONENTS@readercase class HttpServer(config: HttpConfig) extends Start {  def start: Eval[StartResult] =    StartResult.eval(\"starting the http server\")(println(\"http server started\"))}@defaultReader[PostgresDatabase]trait Database {  def count(query: String): Int}@readercase class PostgresDatabase(config: DbConfig) extends Database with Start {  def count(query: String): Int = 0  def start: Eval[StartResult] =    StartResult.eval(\"starting the database\")(println(\"db started\"))}// TOP-LEVEL APPLICATION@readercase class Application(httpServer: HttpServer, database: Database)// MAIN METHODdef main(args: Array[String]): Unit = {  val config: ApplicationConfig =    ApplicationConfig(HttpConfig(\"localhost\", 8080), DbConfig(\"jdbc://postgres\"))  val application: Application =    Application.reader[ApplicationConfig].apply(config).singletons  val started = application.startAll.value  if (started.forall(_.success))    println(\"application started successfully\")  else    println(started.mkString(\"\n\"))}As you can see in the main method, building the application from its configuration and starting it is just 2 lines of code.Read the page on  to understand how it all works.", "tags":"", "loc":"org.zalando.grafter.QuickStart.html"},
{"title":"Singletons", "text":" Make singletonsWhen an application is instantiated with a `Reader` instance, it gets created as a tree of components with distinctinstances for the same type:                            +-------+                            |  App  |                            +-------+                                |                         +--------------+              +----------|  HttpServer  |-------+              |          +--------------+       |              |                                 |     +-------------------+              +----------------+     | GetCustomersRoute |              | GetOrdersRoute |     +-------------------+              +----------------+               |                           |            |  +-----------------+        +-----------------+    +--------------+  | CustomerService |        | CustomerService |    | PriceService |  +-----------------+        +-----------------+    +--------------+       |                            |                   |  +------------+             +------------+          +------------+  | HttpClient |             | HttpClient |          | HttpClient |  +------------+             +------------+          +------------+       |                             |                  |  +------------------+       +------------------+   +------------------+  | HttpClientConfig |       | HttpClientConfig |   | HttpClientConfig |  +------------------+       +------------------+   +------------------+The trouble with this setup is that some components might hold onto precious resources, like a thread-pool. In that situationYou don't want them to be duplicated. You can make all the components in your tree become singletons with the `singletons`method: import org.zalando.grafter.syntax.rewriter._val app: Application =  Application.reader(Config.prod).singletonsThis call to `singletons` is going to *rewrite* your application tree into a graph with singletons:                            +-------+                            |  App  |                            +-------+                                |                         +--------------+              +----------|  HttpServer  |------+              |          +--------------+      |              |                                |     +-------------------+              +----------------+     | GetCustomersRoute |              | GetOrdersRoute |     +-------------------+              +----------------+              |                              |  +-----------------+             +--------------+  | CustomerService |             | PriceService |  +-----------------+             +--------------+                 |                       |            +------------+               |            | HttpClient |---------------+            +------------+                 |            +------------------+            | HttpClientConfig |            +------------------+ Make singletons based on values Modify componentsThe previous rewrite uses the type of components to make singletons. In reality we might want a finer-grained strategy,based on components *values*. This means that we might first want to set specific values on specific components.This can be done with the `modifyWith` method. For example let's say `HttpClientConfig` contains `Uri` parameter pointing to aservice we want to access. We want the `CustomerService` and the `PriceService` to get different configurations: import org.zalando.grafter.syntax.rewriter._val app: Application =  Application.reader(Config.prod).    modifyWith[Any] {      case c: CustomerService => c.replace(Config.prod.customersUri)      case c: PriceService    => c.replace(Config.prod.pricesUri)    }This is the resulting graph:                            +-------+                            |  App  |                            +-------+                                |                         +--------------+              +----------|  HttpServer  |-------+              |          +--------------+       |              |                                 |     +-------------------+              +----------------+     | GetCustomersRoute |              | GetOrdersRoute |     +-------------------+              +----------------+               |                           |            |  +-----------------+        +-----------------+    +--------------+  | CustomerService |        | CustomerService |    | PriceService |  +-----------------+        +-----------------+    +--------------+       |                            |                   |  +------------+             +------------+          +------------+  | HttpClient |             | HttpClient |          | HttpClient |  +------------+             +------------+          +------------+       |                             |                  |  +------------------+       +------------------+   +------------------+  | HttpClientConfig |       | HttpClientConfig |   | HttpClientConfig |  +------------------+       +------------------+   +------------------+  | uri = \"http://c\" |       | uri = \"http://c\" |   | uri = \"http://p\" |  +------------------+       +------------------+   +------------------+ Make the singletonsThe next step is to make singletons across the whole application except for the components having a specific configurationwhich we want to preserve! This can be done with the `singletonsBy` method taking partial functions to makesingletons based on values: import org.zalando.grafter.syntax.rewriter._lazy val app: Application =  Application.reader(Config.prod).    modifyWith[Any] {    case c: CustomerService => c.replace(Config.prod.customersUri)    case c: PriceService    => c.replace(Config.prod.pricesUri)  }.singletonsBy(httpClientSingletons)lazy val httpClientSingletons: PartialFunction[Any, Any] = {  case c: HttpClient => c.config  case c: HttpClientConfig => c}This leads to the final application graph:                            +-------+                            |  App  |                            +-------+                                |                         +--------------+              +----------|  HttpServer  |-------+              |          +--------------+       |              |                                 |     +-------------------+              +----------------+     | GetCustomersRoute |              | GetOrdersRoute |     +-------------------+              +----------------+               |                           |        |  +-----------------+                      |  +--------------+  | CustomerService |----------------------+  | PriceService |  +-----------------+                         +--------------+          |                                         |  +------------+                               +------------+  | HttpClient |                               | HttpClient |  +------------+                               +------------+          |                                         |  +------------------+                        +------------------+  | HttpClientConfig |                        | HttpClientConfig |  +------------------+                        +------------------+  | uri = \"http://c\" |                        | uri = \"http://p\" |  +------------------+                        +------------------+", "tags":"", "loc":"org.zalando.grafter.CreateSingletons.html"},
{"title":"Start the application", "text":"An application which is  and has  can now be started.The components which can be \"started\" need to implement the `Start` interface and define a `start` method: import org.zalando.grafter.{Start, StartResult}import cats.Evalcase class DoobieDatabase(config: DatabaseConfig) extends Start {  def start: Eval[StartResult] =    StartResult.eval(\"starting the database\") {      println(\"start the database here\")    }}Then starting the whole application is just a matter of calling `startAll`:import org.zalando.grafter.syntax.rewriter._val start: Eval[List[StartResult]] =  application.startAllval results = start.valueif (results.forall(_.success))  println(\"ok\")else  println(\"Something went wrong \"+results.mkString(\"\n\"))`startAll` is going to recursively, **bottom up**, call all the components with a `Start` interface and collect the`StartResults`. Stop the applicationThe application can also be stopped in the same manner. `stopAll` will stop each component implementing `Stop` from the top down.The major difference between the `startAll` and `stopAll` is that *all* the components will try to be stopped regardless of failures.", "tags":"", "loc":"org.zalando.grafter.StartApplication.html"},
{"title":"Test the application", "text":"For integration testing you generally need to replace components which are at the frontier of your system anddeeply embedded in your application.This is can be done with the `replace` method:import org.zalando.grafter.syntax.rewriter._trait Databaseobject mockDatabase extends Database {  // mock the database operations}// you can also replace the production configuration!val testConfiguration =  ApplicationConfig.prod.    replace[HttpConfig](HttpConfig(\"localhost\", 8080))// create the application from the test configuration// and mock the databaseval application: Application =  Application.reader.apply(testConfiguration).    singletons.    replace[Database](mockDatabase)application.startAll*Note*: due to a limitation with the rewriting, final case classes with one arguments *cannot* be replaced!It is also particularly important to test that the .", "tags":"", "loc":"org.zalando.grafter.TestApplication.html"},
{"title":"Test the configuration", "text":"In the page about  we see that we can modify the configuration of some componentswith `modifyWith` and create a limited number of instances for some components.It is highly recommended that you add tests to check that the modification you intend to make on your graph reallyhappen as you wish. Collecting ancestorsFor example we can collect all the components using a specific component type: import org.zalando.grafter.syntax.query._val usersOfHttpClient: Map[HttpClient, List[List[Any]]] =  application.ancestors[HttpClient]If we take the example shown in , we can then make sure that we end up with a`Map` containing one `HttpClient` used by the `CustomerService` and a different `HttpClient` used by the `PriceService`. Visual inspectionA quick and useful way to check the state of your application is to create the corresponding graph: import org.zalando.grafter.syntax.visualize._val application = Application.prodapplication.asDotString ====  s\"\"\"  |strict digraph {  |  node [shape=record]  |  \"A\";  |  \"B # 1/2\";  |  \"B # 2/2\";  |  \"C # 1/2\";  |  \"C # 2/2\";  |  \"D\";  |  \"B # 1/2\" -> \"A\";  |  \"B # 2/2\" -> \"A\";  |  \"C # 1/2\" -> \"A\";  |  \"C # 1/2\" -> \"B # 1/2\";  |  \"C # 1/2\" -> \"B # 2/2\";  |  \"C # 2/2\" -> \"A\";  |  \"C # 2/2\" -> \"B # 1/2\";  |  \"C # 2/2\" -> \"B # 2/2\";  |  \"D\" -> \"C # 1/2\";  |  \"D\" -> \"C # 2/2\";  |}\"\"\".stripMargin`asDotString` produces a `.dot` graph which you can visualize with [webgraphviz](http://www.webgraphviz.com) or similar tools.![](images/webgraphviz-example.png) ConfigurationYou can configure the generation of the `dot` graph by passing to the `asDotString` method: - `included: Product => Boolean` to describe which nodes should be kept, those nodes will be kept even if their parents are being filtered out - `excluded: Any => Boolean` to describe which nodes should be excluded including their children - `display: NodeDisplay(summary, attributesFilter)` to show more details for a given node     - `summary: Product => Option[String]`. This function can be used to return a \"summary\" of a node to be displayed in a        box below the node name. The default is `_ => None`.     - `attributesFilter: Any => Option[Any]`. If `summary` doesn't return a result, this function is called for every        of the product attributes. By default only \"primitive\" values (`String`, `Int`, `AnyVal`,...) values are being shown<p/> With specs2If you use [**specs2**](http://specs2.org) you can use the `org.zalando.grafter.specs2.matcher.ComponentsMatchers` traitto check the number of components of a given type in your application:import org.zalando.grafter.specs2.matcher._import org.specs2.Specificationclass ApplicationSpec extends Specification with ComponentsMatchers { def is = s2\"\"\"The application contains the right number of components $checkApplication\"\"\"  val application = Application()  def checkApplication = {    application must containInstances(      classOf[Service1] -> 1,      classOf[Service2] -> 1,      classOf[Service3] -> 2    )  }}case class Application(service1: Service1 = Service1(), service2: Service2 = Service2())case class Service1(service3: Service3 = Service3())case class Service2(service3: Service3 = Service3())case class Service3()", "tags":"", "loc":"org.zalando.grafter.TestConfiguration.html"},
{"title":"Finally tagless", "text":"The [\"finally tagless\"](http://okmij.org/ftp/tagless-final/index.html) style for organising software functionalities ismore and more popular. Grafter supports this approach by allowing you to specify a type constructor as the type parameterof a component: import org.zalando.grafter.macros.{defaultReader, reader}import cats.Monad@defaultReader[DatabaseUserOperations]trait UserOperations[F[_]] {  def getUser(name: String): F[User]  def createUser(name: String): F[User]}@readercase class DatabaseUserOperations[F[_]](config: DbConfig)(implicit val m: Monad[F]) extends UserOperations[F] {  def getUser(name: String): F[User] = ???  def createUser(name: String): F[User] = ???}As you can see there is a minor drawback because you would probably like to write `case class DatabaseUserOperations[F[_] : Monad]`.This is not currently possible because of [this Scala bug](https://github.com/scala/bug/issues/10589). Please vote for itor even better propose a PR on the Scala project!", "tags":"", "loc":"org.zalando.grafter.FinallyTagless.html"},
{"title":"Main concepts", "text":" Readers all the way downAn application such as the one shown in  is merely a case class having its dependenciesmodeled as attributes.Similarly the application configuration is a case class containing every piece of information needed to build the `Application`.How do we connect the 2?This is the purpose of the `@reader` annotation on `Application`. The `@reader` annotationgenerates a `reader` method in the companion object of `Application`: import cats.data.Readerobject Application {  implicit def reader[A](implicit r1: Reader[A, HttpServer], r2: Reader[A, Database]): Reader[A, Application] =    Reader(a => Application(r1(a), r2(a)))}The declaration above states that: - if there is a implicit `Reader` instance to create an `HttpServer` from any object of type `A` - if there is a implicit `Reader` instance to create a `Database` from any object of type `A` - *then* there is an implicit `Reader` instance to create an `Application` from any object of type `A`<p/>This means that we can instantiate the `Application` from an `ApplicationConfig`, provided that we get a way to instantiatean `HttpServer` and a `Database` from an `ApplicationConfig`. The `@reader` annotation on `HttpServer` gives us: import cats.data.Readerobject HttpServer {  implicit def reader[A](implicit r1: Reader[A, HttpConfig]): Reader[A, HttpServer] =    Reader(a => HttpServer(r1(a)))}How can we build an `HttpConfig` from the `ApplicationConfig`? This is the purpose of the`@readers` annotation on `ApplicationConfig`. This will generate concrete readers for each member of `ApplicationConfig`: object ApplicationConfig {  implicit def httpConfigReader: Reader[ApplicationConfig, HttpConfig] =    Reader(_.httpConfig)  implicit def dbConfigReader: Reader[ApplicationConfig, DbConfig] =    Reader(_.dbConfig)}From there the magic of implicit resolution will give us a valid `Application.reader[ApplicationConfig]`. Well, almost.You might have noticed that `Database` is an interface, not a case class. How can we instantiate such an interface fromthe application configuration? Deal with interfacesFor interfaces we need a special annotation which will specify a concrete implementation to instantiate, the `@defaultReader`annotation. It generates the following reader: object Database {  implicit def reader[A]: Reader[A, PostgresDatabase] =    PostgresDatabase.reader[A]}This `Reader` instance is simply delegating to the `Reader` instance of `PostgresDatabase` and we now know that we havesuch an instance because there is a `@reader` annotation on `PostgresDatabase`. Warning![warning](images/icon_failure_sml.gif) ** All the components must be totally side-effects free when instantiated! **They must not start a database connection or a http server or even do some logging!Indeed, when using `Readers` to create components, the same `Database` component can be instantiated from different pathsin the application graph and become \"duplicated\" at that stage (see  to fix this).So it is particularly important that the \"start\" of an application is done in a very controlled way: . SummaryIn summary, to wire an application with Grafter you need to annotate: - components with `@reader` - interfaces with `@defaultReader` - the configuration with `@readers`<p/>There are a few more things you might need to know: - how to ? - how to ? - how to ?", "tags":"", "loc":"org.zalando.grafter.Concepts.html"},
{"title":"Comparisons", "text":"There are many libraries or approaches for doing [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) in Scala.Libraries:  - [MacWire](https://github.com/adamw/macwire)  - [Guice](https://github.com/google/guice)  - [Scaladi](https://github.com/scaldi/scaldi)Design Patterns:  - [Cake pattern](http://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth)<p/>The main differences between Grafter and other libraries are the following: - Grafter components directly have `@reader` annotation which provide a way to \"wire\" them with their dependencies once an application configuration is available. Other libraries generally require a separate \"module\" to declare those \"bindings\" - in Grafter each interface needs to have at least a default implementation. This is not required with other libraries - there is no \"interceptors\" or \"scoping\" in Grafter. The only thing you can do is to `modify` or `replace` components - the \"lifecycle\" part (starting components) is completely separated from the \"instantiation/configuration\" part", "tags":"", "loc":"org.zalando.grafter.Comparisons.html"},
{"title":"Welcome to Grafter!", "text":" <img style=\"width:10cm\" src=\"./images/grafter.png\"/>Grafter is a dependency-injection library aiming at giving simple ways to compose independently defined componentsinto a full application which can easily evolve for maintenance or testing.This user guide is divided in 4 parts: 1.     - Install grafter    - Your first application<p/> 1. How to    -     -     -     -     - <p/> 1. Understand    - <p/> 1. Discussions    - ", "tags":"", "loc":"index.html"}]};
     