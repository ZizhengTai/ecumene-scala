package io.ecumene.worker

import scala.util.{ Try, Success, Failure }

import io.ecumene.core.Implicits._

object ClientTest {

  /*
  val greet = new EcumeneFunction1[String, String]("public/greet")
  val dup = new EcumeneFunction2[String, Int, Seq[String]]("public/dup")
  val sayHi = new EcumeneFunction0[String]("public/sayHi")
  */

  val greet = new EcumeneFunctionImpl1[String, String]({ name =>
    name + ", welcome to Ecumene!"
  }, "public/greet", "tcp://*:5555", "tcp://127.0.0.1:5555")


  def main(args: Array[String]): Unit = {
    /*
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
    }*/

    while (true) {
      Thread.sleep(500)
    }
  }
}
