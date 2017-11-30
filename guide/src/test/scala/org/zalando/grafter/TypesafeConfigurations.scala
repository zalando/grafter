package org.zalando.grafter

import org.zalando.grafter.macros.readers

import scala.concurrent.duration._

object TypesafeConfigurations extends UserGuidePage { def is = "Typesafe configurations".title ^ s2"""

In some projects there can be several configurations for different "environments", say for "Production" and "Development".
For example: ${snippet{
@readers
case class Config(
  threadsNb:      Int,
  defaultTimeout: FiniteDuration,
  paymentsUri:    Uri,
  ordersUri:      Uri)

val productionConfig: Config =
  Config(
    threadsNb = 8,
    defaultTimeout = 3.seconds,
    paymentsUri = Uri("http://acme.org/payments"),
    ordersUri   = Uri("http://acme.org/orders"))

val developmentConfig: Config =
  Config(
    threadsNb = 8,
    defaultTimeout = 3.seconds,
    paymentsUri = Uri("http://acme-test.org/payments"),
    ordersUri   = Uri("http://acme-test.org/orders"))
}}

Since `Config` is just a case class which can have lots of fields it can be very tempting to derive the development config
from the production config: ${snippet{
// 8<--
@readers
case class Config(
  threadsNb:      Int,
  defaultTimeout: FiniteDuration,
  paymentsUri:    Uri,
  ordersUri:      Uri)
// 8<--

val productionConfig: Config =
  Config(
    threadsNb = 8,
    defaultTimeout = 3.seconds,
    paymentsUri = Uri("http://acme.org/payments"),
    ordersUri   = Uri("http://acme.org/orders"))

val developmentConfig: Config =
  productionConfig.copy(
    paymentsUri = Uri("http://acme-test.org/payments"),
    ordersUri   = Uri("http://acme-test.org/orders")
  )
}}

The good thing here is that we are removing some duplication in our declarations because `developmentConfig` sort of
inherits from `productionConfig`. This is slightly dangerous though because if you forget to override one of the production
fields, like `paymentsUri` you might accidentally run some code against production while executing your tests!

The opposite situation, where you define `productionConfig` in terms of `developmentConfig` is not enviable either
because you might run your production application against tests services and some data might be simply lost.

### Making it typesafe

It is possible to make the configuration typesafe with one simple type parameter:${snippet{

trait Prod
object Prod extends Prod

trait Dev
object Dev extends Dev

type ->[A, B] = (A, B)

@readers
case class Config[C](
  threadsNb:      Int,
  defaultTimeout: FiniteDuration,
  paymentsUri:    Uri -> C,
  ordersUri:      Uri -> C)

val prodPaymentsUri: Uri -> Prod =
  Uri("http://acme.org/payments") -> Prod

val prodOrdersUri: Uri -> Prod =
  Uri("http://acme-test.org/orders") -> Prod

val devPaymentsUri: Uri -> Dev =
  Uri("http://acme-test.org/payments") -> Dev

val devOrdersUri: Uri -> Dev =
  Uri("http://acme-test.org/orders") -> Dev

val productionConfig: Config[Prod] =
  Config(
    threadsNb = 8,
    defaultTimeout = 3.seconds,
    paymentsUri = prodPaymentsUri,
    ordersUri   = prodOrdersUri)

val developmentConfig: Config[Dev] =
  productionConfig.copy(
    paymentsUri = devPaymentsUri,
    ordersUri   = devOrdersUri
  )

// Now it is a lot harder to mix-up configurations
// The following 2 tentatives of mixing-up production and development
// configurations do not compile
/*

val accidentalProdOverride: Config[Prod] =
  productionConfig.copy(paymentsUri = devPaymentsUri)

val accidentalDevOverride: Config[Dev] =
  developmentConfig.copy(paymentsUri = prodPaymentsUri)
*/

// But this compiles ok
val sandboxPaymentsUri: Uri -> Dev =
  Uri("http://acme-sandbox.org/payments") -> Dev

val okDevOverride: Config[Dev] =
  developmentConfig.copy(paymentsUri = sandboxPaymentsUri)
}}

### Readers

What about the `@readers` annotation? What does it generate for fields annotated with `Prod` or `Dev`?

The `readers` annotation is smart enough to recognize those fields and create a `Reader` instance stripping out the
environment annotation, for example:
```
implicit def paymentsUriReader[A1]: Reader[Config[A1], Uri] =
  Reader(_.paymentsUri._1)
```
"""

  case class Uri(value: String)

}
