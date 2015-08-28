package io.ecumene.client

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import org.msgpack.core.MessagePack

class EcumeneFunction0[R: CanUnpack](
  val ecmKey: String,
  var timeout: Long = 15000
) extends EcumeneFunction[R] with Function0[R] {

  def apply(): R = {
    Await.result(future(), Duration.Inf)
  }

  def future(): Future[R] = {
    import EcumeneFunction._

    futureWithPacker { packer =>
      implicit val pk = packer

      packer packArrayHeader 0
    }
  }
}
