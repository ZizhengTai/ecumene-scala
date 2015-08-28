package io.ecumene.client

import scala.util.{ Try, Success, Failure }

object ClientTest {

  import io.ecumene.client.EcumeneFunction.Implicits._

  val greet = new EcumeneFunction1[String, String]("public/greet")
  val dup = new EcumeneFunction2[String, Int, Seq[String]]("public/dup")
  val sayHi = new EcumeneFunction0[String]("public/sayHi")

  def main(args: Array[String]): Unit = {
    Try(greet("タイさん")) match {
      case Success(s) => println(s)
      case Failure(e) => println(s"ERROR: $e")
    }

    Try(dup("Umaru~", 5)) match {
      case Success(s) => s foreach println
      case Failure(e) => println(s"ERROR: $e")
    }

    Try(sayHi()) match {
      case Success(s) => println(s)
      case Failure(e) => println(s"ERROR: $e")
    }

    while (!Thread.currentThread.isInterrupted) {
      Thread.sleep(500)
    }
  }
}
