# Grafter

[![Join the chat at https://gitter.im/zalando/grafter](https://badges.gitter.im/zalando/grafter.svg)](https://gitter.im/zalando/grafter?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/zalando/grafter.svg?branch=master)](https://travis-ci.org/zalando/grafter)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/grafter_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/grafter_2.12)
[![Codecove](https://codecov.io/github/zalando/grafter/coverage.svg?precision=2)

![grafting](https://autonomyacres.files.wordpress.com/2015/04/crown-cleft-grafting-fruit-trees.jpg?w=300&h=284)

## What's wrong with constructor injection again?

There are [many](https://github.com/adamw/macwire) [libraries](https://github.com/google/guice) or [approaches](http://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth) for doing [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) in Scala.
Grafter goes back to the fundamentals of dependency injection by *just using constructor injection*: no reflection, no xml, no implementation inheritance or self-types.

 - [Installation](doc/installation.md)
 - [Introduction](doc/introduction.md)
 - [Grafter Components](doc/components.md)
 - [Creating your first components](doc/creating.md)
 - [In a library](doc/library.md)
 - [Remove boilerplate](doc/boilerplate.md)
 - [Use interfaces](doc/interfaces.md)
 - [Make singletons](doc/singletons.md)
 - [Start and Stop an application](doc/start-stop.md)
 - [Testing](doc/testing.md)
 - [Inspect the application graph](doc/inspect-app-graph.md)
 - [A full application example](doc/fullapp.md)

---

### Contributing

Please read our [contributor guidelines](CONTRIBUTING.md) for more details. 
And please check these [open issues](http://github.com/zalando/grafter/issues) for specific tasks.

----

### License

The MIT License (MIT) Copyright © [2017] Zalando SE, https://tech.zalando.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
