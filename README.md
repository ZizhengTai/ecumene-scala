# What is ecumene-scala?
This is the Scala client/worker library for the Ecumene RPC protocol.

#Features
* **Fast:** Ecumene is based on ZeroMQ and MessagePack, without the overhead of HTTP.
* **Natural:** Ecumene is designed with the interoperability with native function calls in mind, and tries to abstract away the difference between local and remote function calls.
* **Composable:** Ecumene supports the idea of "composable services," where network services can be composed the same way as [`higher-order functions`](https://www.wikiwand.com/en/Higher-order_function).

# Building and Installation
Simply run `sbt compile`.

# Getting Started
MyClient.scala:
```scala
import scala.util.{ Try, Success, Failure }

import io.ecumene.client._
import io.ecumene.core.Implicits._

object MyClient {

  val greet = new EcumeneFunction1[String, String]("myapp.greet")
  
  def main(args: Array[String]): Unit = {
    
    // Synchronous call
    println(greet("Zizheng"))
    
    // Synchronous call with error handling
    Try(greet("Zizheng")) match {
      case Success(s) => println(s)
      case Failure(e) => println(s"ERROR: $e")
    }
    
    // Asynchronous call
    import scala.concurrent.ExecutionContext.Implicits.global
    
    val f = greet.future("Zizheng")
    f onSuccess { case s => println(s) }
    f onFailure { case e => println(s"ERROR: $e") }
    
    while (true) { Thread.sleep(500) }
  }
}
```

MyWorker.scala:
```scala
import io.ecumene.worker._
import io.ecumene.core.Implicits._

object MyWorker {

  val greet = new EcumeneFunctionImpl1[String, String]({ name =>
    name + ", welcome to Ecumene!"
  }, "myapp.greet", "tcp://*:5555", "tcp://127.0.0.1:5555")
  
  def main(args: Array[String]): Unit = {
    while (true) { Thread.sleep(500) }
  }
}
```

Then start `MyWorker` followed by `MyClient`.

# License
ecumene-scala is licensed under the GNU Lesser General Public License v3.0. See the [`LICENSE`](./LICENSE) file for details.
