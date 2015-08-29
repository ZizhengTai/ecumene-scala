import java.util.UUID
import org.scalatest._

import io.ecumene.core.Implicits._
import io.ecumene.client._
import io.ecumene.worker._

class ClientSpec extends FlatSpec with Matchers {

  "An EcumeneFunctionX" should "throw RuntimeException if called upon an nonexistent Ecumene Key" in {
    val f0 = new EcumeneFunction0[String](UUID.randomUUID.toString)
    a [RuntimeException] should be thrownBy {
      f0()
    }

    val f1 = new EcumeneFunction1[String, String](UUID.randomUUID.toString)
    a [RuntimeException] should be thrownBy {
      f1("")
    }

    val f7 = new EcumeneFunction7[String, String, String, String, String, String, String, String](UUID.randomUUID.toString)
    a [RuntimeException] should be thrownBy {
      f7("", "", "", "", "", "", "")
    }
  }

  it should "throw IllegalArgumentException if given wrong argument types" in {
    val ecmKey = UUID.randomUUID.toString
    val addImpl = new EcumeneFunctionImpl2[Int, Int, Int](
      { (a, b) => a + b },
      ecmKey,
      "tcp://*:5555",
      "tcp://localhost:5555"
    )

    val add = new EcumeneFunction2[Int, String, Int](ecmKey)
    an [IllegalArgumentException] should be thrownBy {
      Thread.sleep(6000) // Wait for worker to register
      add(1, "2")
    }
  }

  it should "throw org.msgpack.core.MessageTypeException if given wrong return type" in {
    val ecmKey = UUID.randomUUID.toString
    val addImpl = new EcumeneFunctionImpl2[Int, Int, Int](
      { (a, b) => a + b },
      ecmKey,
      "tcp://*:5556",
      "tcp://localhost:5556"
    )

    val add = new EcumeneFunction2[Int, Int, String](ecmKey)
    a [org.msgpack.core.MessageTypeException] should be thrownBy {
      Thread.sleep(6000) // Wait for worker to register
      add(1, 2)
    }
  }
}
